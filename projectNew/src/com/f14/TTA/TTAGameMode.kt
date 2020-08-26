package com.f14.TTA

import com.f14.TTA.component.CardBoard
import com.f14.TTA.component.Chooser
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.component.card.EventCard
import com.f14.TTA.component.param.AuctionParam
import com.f14.TTA.consts.ChooserType
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.listener.ChooseArmyTichuListener
import com.f14.TTA.listener.FirstRoundListener
import com.f14.TTA.listener.TTARoundListener
import com.f14.bg.GameMode
import com.f14.bg.exception.BoardGameException
import java.util.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.math.max

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

class TTAGameMode(override val game: TTA) : GameMode<TTAPlayer, TTAConfig, TTAReport>(game) {
    /**
     * 是否为新版模式
     */
    var isVersion2 = false
    /**
     * 游戏最后一回合的标志
     */
    var finalRound = false
    /**
     * 游戏结束的标志
     */
    var gameOver = false
    /**
     * 宗教裁判所的起始玩家
     */
    var inquisitionPosition: Int = 0
    /**
     * 奥运会的起始玩家
     */
    var olympicsPosition: Int = 0
    /**
     * 是否要移动卡牌列
     */
    var doregroup: Boolean = false
    /**
     * 地主竞价
     */
    var tichuBid: Int = 0
    /**
     * 当前世纪
     */
    var currentAge: Int = 0
        private set
    /**
     * 排名信息
     */
    lateinit var gameRank: TTAGameRank
    /**
     * 公共卡牌版
     */
    lateinit var cardBoard: CardBoard
        private set
    /**
     * 和平模式下会用到的记分牌
     */
    val bonusCards: MutableList<EventCard> = ArrayList()
    /**
     * 已经体退的玩家
     */
    val resignedPlayerPosition: MutableMap<TTAPlayer, Int> = LinkedHashMap()

    override val isGameOver: Boolean // 必须是最后一回合,而且游戏结束,才真的结束游戏
        get() = this.gameOver && this.finalRound

    /**
     * 增加世纪
     */
    fun addAge() {
        this.currentAge += 1
        for (p in realPlayers) {
            p.ageDummyCard.level = currentAge
            p.abilityManager.getAbilitiesByType(CivilAbilityType.PA_ADD_AGE_EFFECT)
                    .filter { it test p.ageDummyCard }
                    .forEach { game.playerDrawMilitaryCard(p, it.property) }
            this.game.playerRefreshProperty(p)
        }
    }

    @Throws(BoardGameException::class)
    override fun endGame() {
        super.endGame()
        // 结束时算分
        val endPhase = TTAEndPhase()
        endPhase.execute(this)
    }

    /**
     * 第一回合结束时进行的动作
     * @throws BoardGameException
     */
    private fun firstRoundEnd() {
        // 为所有玩家生产资源和分数
        for (p in this.game.players) {
            this.game.playerRoundScore(p)
        }

        // 补牌,并进入I世纪
        this.cardBoard.regroupCardRow(true)
        if (this.currentAge < 1) {
            // 只有当当前世纪为A时,才会进入I世纪...
            this.cardBoard.newAge()
        }
        this.game.sendBaseInfo(null)
        this.game.sendCardRowInfo(null)
    }

    /**
     * 取得所有玩家的能力
     * @param type
     * @return
     */
    fun getPlayerAbilities(type: CivilAbilityType): Map<CivilCardAbility, TTAPlayer> {
        return realPlayers
                .flatMap { it.abilityManager.getAbilitiesByType(type).map { a -> a to it } }
                .toMap()
    }

    /**
     * 按照选择器取得玩家列表
     * @param chooser
     * @return
     */
    fun getPlayersByChooser(chooser: Chooser): List<TTAPlayer> {
        val count = gameRank.playerNumber
        // 如果是2人游戏,则只会取一名玩家
        val num = if (count <= 2) 1 else chooser.num
        when (chooser.type) {
            ChooserType.ALL -> { // 所有玩家
                return ArrayList(realPlayers)
            }
            ChooserType.RANK -> {
                // 按照排名取得玩家
                if (chooser.isWeakest && chooser.byProperty === CivilizationProperty.MILITARY) {
                    gameRank.players
                            .filter { it.abilityManager.hasAbilitiy(CivilAbilityType.PA_NOSTRADAMUS) }
                            .forEach { it.properties.addProperty(CivilizationProperty.MILITARY, 2) }
                }
                val ranklist = gameRank.getPlayersByRank(chooser.byProperty!!)
                if (chooser.isWeakest && chooser.byProperty === CivilizationProperty.MILITARY) {
                    gameRank.players
                            .filter { it.abilityManager.hasAbilitiy(CivilAbilityType.PA_NOSTRADAMUS) }
                            .forEach { it.properties.addProperty(CivilizationProperty.MILITARY, -2) }
                }
                return if (chooser.isWeakest) {
                    // 如果要求选择最弱的,则取最后的num名玩家
                    ranklist.takeLast(num)
                } else {
                    // 否则就取前面的num名玩家
                    ranklist.take(num)
                }
            }
            ChooserType.MOST -> {
                // 最多属性,允许并列
                val ranklist = gameRank.getPlayersByRank(chooser.byProperty!!)
                val most = max(chooser.num, ranklist[0].getProperty(chooser.byProperty!!))
                return ranklist.filter { it.getProperty(chooser.byProperty!!) == most }
            }
            ChooserType.FOR_BARBARIAN -> {
                // 野蛮人入侵事件专用选择器
                // 如果文明点数最高的玩家是军事力最弱的前2名,则返回该玩家
                // 如果是2人游戏,则是最弱的一名
                val ranklist = gameRank.getPlayersByRank(CivilizationProperty.CULTURE)
                return ranklist.take(1).filter { it.getRank(CivilizationProperty.MILITARY, true) <= num }
            }
            ChooserType.CURRENT_PLAYER -> {
                // 当前玩家
                return listOf(gameRank.currentPlayer)
            }
            else -> return emptyList()
        }
    }

    /**
     * 还没体退的玩家
     * @return
     */
    val realPlayers: Collection<TTAPlayer>
        get() = gameRank.getPlayers()

    /**
     * 体退的玩家数量
     * @return
     */
    val resignedPlayerNumber: Int
        get() = this.resignedPlayerPosition.size

    /**
     * 体退的玩家顺序
     * @param player
     * @return
     */
    fun getResignedPlayerPosition(player: TTAPlayer): Int {
        return this.resignedPlayerPosition[player] ?: 0
    }

    @Throws(BoardGameException::class)
    override fun round() {
        this.waitForPlayerRound()
    }

    @Throws(BoardGameException::class)
    override fun setupGame() {
        val config = this.game.config
        this.gameRank = TTAGameRank(game.players.toMutableList(), game.currentPlayer!!)
        this.isVersion2 = config.isNewAgeUsed
        val rm = this.game.getResourceManager<TTAResourceManager>()

        // 起始世纪为0
        this.currentAge = 0
        this.bonusCards.clear()
        this.resignedPlayerPosition.clear()

        // 初始化摸牌区面板
        this.cardBoard = CardBoard(this)
        this.inquisitionPosition = -1
        this.olympicsPosition = -1
        this.doregroup = false

        // 初始化玩家信息
        for (player in this.game.players) {
            player.reset()
            player.init(this)
            val cards = rm.getStartDeck(config, player)
            cards.forEach { player.addCard(it) }
            player.refreshProperties()
            // 重置玩家的行动点
            player.resetActionPoint()
        }
    }

    @Throws(BoardGameException::class)
    override fun startGame() {
        super.startGame()
        this.game.sendCardRowReport()
        // 地主竞价
        if (game.isTichuMode) {
            this.waitForTichu()
            this.report.system("地主竞价结束!")
        }
        // 开始游戏
        this.report.system("第 $round 回合开始!")
        this.waitForFirstRound()
        this.firstRoundEnd()
        round++
    }

    /**
     * 第一回合
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForFirstRound() {
        this.addListener(FirstRoundListener(this))
    }

    /**
     * 等待执行玩家
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForPlayerRound() {
        this.addListener(TTARoundListener(this))
    }

    /**
     * 地主竞价
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForTichu() {
        this.report.system("地主竞价开始!")
        game.sendAlertToAll("当前为地主竞价步骤，竞得地主的玩家在最终计分时减去竞价数值的分数，再与其他玩家中分数最高的比较")
        val tichuCard = cardBoard.tichuCard ?: throw BoardGameException("找不到地主卡!")
        val l = ChooseArmyTichuListener(this, game.startPlayer!!, tichuCard)
        val res = this.insertListener(l)
        // 结算拍卖结果
        val topPlayer = res.get<TTAPlayer>("topPlayer")
        if (topPlayer != null) {
            val ap = res.get<AuctionParam>(topPlayer.position)!!
            this.report.bidTichu(topPlayer, ap.getTotalValue())
            this.tichuBid = ap.getTotalValue()
            this.game.setTichu(topPlayer, tichuCard)
        } else {
            throw BoardGameException("没有人愿意做地主!")
        }
    }

}
