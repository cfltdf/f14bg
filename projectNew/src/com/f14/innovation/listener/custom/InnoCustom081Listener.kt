package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.listener.InnoChooseCardListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import java.util.*

/**
 * #081-火箭技术 监听器

 * @author F14eagle
 */
class InnoCustom081Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseCardListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {
    private var chooseNum = 0

    private var chooseCards: MutableList<InnoCard> = ArrayList()

    init {
        this.setNum()
    }

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        return !(this.getAvailableCardNum(gameMode, player) == 0 || this.initParam!!.num == 0)
    }

    @Throws(BoardGameException::class)
    private fun beforeProcessChooseCard(player: InnoPlayer, target: InnoPlayer, cards: List<InnoCard>) {
        // 发送移除牌的指令
        gameMode.game.sendPlayerRemoveChooseScoreCardsResponse(player, target, cards)
        this.chooseNum += cards.size
        this.chooseCards.addAll(cards)
    }

    /**
     * 判断是否可以结束行动
     * @param player
     * @return
     */
    private fun canEndResponse(player: InnoPlayer): Boolean {
        // 选够牌,就可以结束行动
        if (this.initParam!!.num <= this.chooseNum) {
            return true
        }
        // 如果其他玩家都没有计分牌,则可以结束行动
        return this.getAvailableCardNum(gameMode, player) == 0
    }

    /**
     * 对所选的牌进行校验
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun checkChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        if (cards.isEmpty()) {
            throw BoardGameException("请选择计分区的牌!")
        }
    }

    /**
     * 检查玩家的回应情况
     * @param player
     */
    private fun checkPlayerResponsed(player: InnoPlayer) {
        // 如果达到选择数量,则结束回应
        if (this.canEndResponse(player)) {
            this.setPlayerResponsed(player)
        }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) = Unit

    override fun createStartListenCommand(player: InnoPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        val map = gameMode.game.players.filterNot { it === player }.map { it.position to BgUtils.card2String(it.scores.getCards()) }.toMap()
        res.private("playerScoreCards", map)
        res.private("num", this.initParam!!.num)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val targetPosition = action.getAsInt("choosePosition")
        val target = gameMode.game.getPlayer(targetPosition)
        if (target === player || gameMode.game.isTeammates(target, player)) {
            throw BoardGameException("不能选择自己或队友的牌!")
        }
        val cardIds = action.getAsString("cardIds")
        val cards = target.scores.getCards(cardIds)

        this.checkChooseCard(player, cards)
        this.beforeProcessChooseCard(player, target, cards)
        this.processChooseCard(player, target, cards)
        this.afterProcessChooseCard(player, cards)
        this.checkPlayerResponsed(player)
    }


    override val actionString: String
        get() = ""

    /**
     * 取得所有可供选择牌的数量
     * @param player
     * @return
     */
    private fun getAvailableCardNum(gameMode: InnoGameMode, player: InnoPlayer): Int {
        return gameMode.game.players.filter { it !== player && !gameMode.game.isTeammates(it, player) }.sumBy { it.scores.size() }
    }

    /**
     * 取得所有可供选择牌的数量
     * @param player
     * @return
     */
    override fun getAvailableCardNum(player: InnoPlayer) = 0

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_081

    /**
     * 处理玩家选择的牌
     * @param player
     * @param target
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun processChooseCard(player: InnoPlayer, target: InnoPlayer, cards: List<InnoCard>) {
        // 从目标玩家计分区中归还牌
        cards.map { gameMode.game.playerRemoveScoreCard(target, it) }.forEach { gameMode.game.playerReturnCard(target, it) }
    }

    private fun setNum() {
        val player = this.targetPlayer
        this.initParam!!.num = player.getIconCount(InnoIcon.CLOCK) / 2
    }

}
