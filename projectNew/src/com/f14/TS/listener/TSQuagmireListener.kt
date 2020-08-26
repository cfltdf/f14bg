package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.consts.CardType
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.consts.TrigType
import com.f14.TS.listener.initParam.InitParam
import com.f14.TS.utils.TSRoll
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException


/**
 * 困境的监听器

 * @author F14eagle
 */
class TSQuagmireListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    override fun cannotPass(action: GameAction): Boolean {
        val player = action.getPlayer<TSPlayer>()
        // 如果玩家有必须要出的牌,则不允许跳过
        if (this.getForceCard(player) != null) {
            return true
        }
        // 检查玩家是否手上还有大于等于2OP的牌,如果有则不允许跳过
        return player.maxOpValue >= 2
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createStartListenCommand(player: TSPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        res.public("title", card!!.reportString)
        // 检查是否有需要强制出的牌,如果有则设置该牌的id
        val card = this.getForceCard(player)
        if (card != null) {
            res.public("forceCardId", card.id)
        }
        return res
    }

    /**
     * 弃牌并掷骰

     * @param gameMode

     * @param action

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun discardAndRoll(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        // 先检查玩家是否有必须要出的牌
        var card = this.getForceCard(player)
        if (card == null) {
            // 如果没有必须要出的牌,则从选择的参数中取得
            val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                    ?: throw BoardGameException("请选择要弃掉的牌!")
            card = player.getCard(cardId)
            // 检查是否可以弃掉该牌
            if (player.getOp(card) < 2) {
                throw BoardGameException("选择的牌OP点数不够!")
            }
            // 弃牌
            gameMode.game.playerRemoveHand(player, card)
            gameMode.report.playerRemoveCard(player, card)
        } else {
            // 如果有,则移除玩家该牌的效果
            gameMode.game.playerRemoveHand(player, card)
            gameMode.report.playerRemoveCard(player, card)
            gameMode.game.playerRemoveActivedCard(player, card)
        }
        // 将该牌放入弃牌堆
        gameMode.game.discardCard(card)
        // 执行掷骰
        val r = TSRoll.roll()
        gameMode.report.playerRoll(player, r, 0)
        // 如果掷骰结果小于5，则移除困境的效果
        if (r < 5) {
            gameMode.game.playerRemoveActivedCard(player, this.card!!)
            gameMode.report.playerRemoveActiveCard(player, this.card!!)
        }
        // 设置玩家行动结束
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {

    }

    /**
     * 打出记分牌事件

     * @param gameMode

     * @param action

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doScoreEvent(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()

        // 检查玩家是否可以出计分牌
        // 如果玩家有必须要出的牌,则不允许跳过
        if (this.getForceCard(player) != null) {
            throw BoardGameException("现在还不能打计分牌!")
        }
        // 检查玩家是否手上还有大于等于2OP的牌,如果有则不允许出计分牌
        if (player.maxOpValue >= 2) {
            throw BoardGameException("现在还不能打计分牌!")
        }

        val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择计分牌!")
        val card = player.getCard(cardId)
        if (card.cardType != CardType.SCORING) {
            throw BoardGameException("选择的牌不是计分牌!")
        }
        // 输出战报信息
        val type = TrigType.EVENT
        gameMode.game.playerPlayCard(player, card)
        gameMode.report.playerPlayCard(player, card, type)
        // 执行触发事件
        gameMode.game.activeCardEvent(player, card)
        // 设置玩家行动结束
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {
        when (action.getAsString("subact")) {
            "DISCARD" -> // 弃牌并掷骰
                this.discardAndRoll(action)
            "SCORE" -> // 打出记分牌
                this.doScoreEvent(action)
            else -> throw BoardGameException("无效的行动指令!")
        }
    }


    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_CARD

    /**
     * 取得必须要出的牌

     * @param player

     * @return
     */
    private fun getForceCard(player: TSPlayer): TSCard? {
        val card = player.forcePlayCard
        // 如果取得强制出牌,并且该牌的OP大于等于2,则返回该牌
        if (card != null && player.getOp(card) >= 2) {
            return card
        }
        return card
    }

    override fun getMsg(player: TSPlayer): String {
        return "你必须弃掉一张OP大于等于2的牌,并且掷骰结果小于5;或者打出计分牌!"
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_QUAGMIRE
}
