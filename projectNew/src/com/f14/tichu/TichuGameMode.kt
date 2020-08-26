package com.f14.tichu

import com.f14.bg.GameMode
import com.f14.bg.common.ListMap
import com.f14.bg.exception.BoardGameException
import com.f14.bg.player.PlayerGroup
import com.f14.bg.utils.BgUtils
import com.f14.tichu.componet.TichuCard
import com.f14.tichu.componet.TichuCardDeck
import com.f14.tichu.consts.AbilityType
import com.f14.tichu.listener.*
import com.f14.tichu.param.ExchangeParam

class TichuGameMode(override val game: Tichu) : GameMode<TichuPlayer, TichuConfig, TichuReport>(game) {
    var wishedPoint: Int = 0
    var exchangeParam: ExchangeParam = ExchangeParam()

    // 初始化分组信息,该游戏只会有2组玩家
    val groups: List<TichuPlayerGroup> = listOf(TichuPlayerGroup(), TichuPlayerGroup())
    private val deck: TichuCardDeck = TichuCardDeck()

    /**
     * 为所有玩家发指定数量的牌
     * @param num
     */
    private fun dealCard(num: Int) {
        val cardMap = ListMap<TichuPlayer, TichuCard>()
        for (i in 0 until num) for (player in game.players) cardMap.getList(player).add(this.deck.draw()!!)
        // 设置完牌后,添加给玩家
        for (player in game.players) {
            val cards = cardMap.getList(player)
            game.playerGetCards(player, cards)
            game.report.playerGetCards(player, cards)
        }
    }

    @Throws(BoardGameException::class)
    override fun endGame() {
        super.endGame()
        // 结束时算分
        val endPhase = TichuEndPhase()
        endPhase.execute(this)
    }

    /**
     * 取得玩家组
     * @param index
     * @return
     */
    fun getGroup(index: Int): PlayerGroup<TichuPlayer> {
        return this.groups[index]
    }


    /**
     * 取得指定玩家的对家组
     * @param player
     * @return
     */
    fun getOppositeGroup(player: TichuPlayer) = this.groups.first { !it.containPlayers(player) }

    /**
     * 取得指定玩家的玩家组
     * @param player
     * @return
     */
    fun getPlayerGroup(player: TichuPlayer) = this.groups.first { it.containPlayers(player) }


    override fun initRound() {
        super.initRound()
        this.wishedPoint = 0
        // 回合开始时,清空玩家的手牌
        game.players.forEach(TichuPlayer::reset)
        // 清空玩家组的回合得分
        this.groups.forEach(TichuPlayerGroup::resetRoundScore)

        // 创建牌堆
        this.deck.reshuffle()

        // 刷新全部玩家的信息
        game.sendGameBaseInfo(null)
        game.sendPlayerPlayingInfo(null)

    }

    /**
     * 判断所有玩家是否在同一个组中
     * @param players
     * @return
     */
    fun isFriendlyPlayer(vararg players: TichuPlayer) = this.groups.any { it.containPlayers(*players) }

    override val isGameOver: Boolean  // 如果玩家组的分数大于等于设定的分数,并且双方不同分,则游戏结束
        get() = this.groups.map(TichuPlayerGroup::score).any { it >= game.config.score } && !isTieScore

    /**
     * 判断玩家是否平分
     * @return
     */
    private val isTieScore: Boolean
        get() = this.groups.map(TichuPlayerGroup::score).distinct().count() == 1

    @Throws(BoardGameException::class)
    override fun round() {
        // 回合开始时,先为所有玩家发8张牌
        this.dealCard(8)
        // 等待玩家是否叫大地主
        this.waitForBigTichuPhase()
        // 然后发完所有的牌
        this.dealCard(6)
        // 开始换牌阶段
        this.waitForRegroupPhase()
        // 确认换牌信息
        this.waitForConfirmExchangePhase()
        // 正式开始之前,刷新一下所有玩家的button情况
        game.refreshPlayerButton(null)
        // 正式开始回合的出牌阶段,直到剩余最后1个玩家
        this.waitForRoundPhase()
        // 回合结束时,显示玩家得分,并等待玩家确认
        this.waitForResultPhase()
    }

    @Throws(BoardGameException::class)
    override fun setupGame() {
        // 按照顺位将玩家分组
        game.players.forEach {
            val i = it.position % 2
            groups[i].addPlayer(it)
            it.groupIndex = i
        }
        // 创建卡牌实例
        val rm = game.getResourceManager<TichuResourceManager>()
        this.deck.defaultCards = BgUtils.cloneList(rm.allCardsInstance)
        this.deck.init()
    }

    /**
     * 等待执行大地主阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForBigTichuPhase() {
        this.addListener(TichuBigListener(this))
    }

    /**
     * 等待确认换牌阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForConfirmExchangePhase() {
        this.addListener(TichuConfirmExchangeListener(this))
    }

    /**
     * 等待执行换牌阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForRegroupPhase() {
        this.addListener(TichuRegroupListener(this))
    }

    /**
     * 等待执行回合结果确认阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForResultPhase() {
        this.addListener(TichuResultListener(this))
    }

    /**
     * 等待执行普通回合阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForRoundPhase() // 设置起始玩家,起始玩家为拥有"雀"的玩家
    {
        val firstPlayer = game.players.first { it.hasCard(AbilityType.MAH_JONG) }
        this.addListener(TichuRoundListener(this, firstPlayer))
    }

}
