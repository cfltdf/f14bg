package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.consts.CardType
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.BgUtils


/**
 * #85-星球大战的监听器

 * @author F14eagle
 */
class Custom85Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {
    private var selectedCard: TSCard? = null

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        res["card"] = selectedCard
        return res
    }

    override fun createStartListenCommand(player: TSPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 将弃牌堆中的卡牌信息发送到客户端
        res.public("cardIds", BgUtils.card2String(gameMode.cardManager.playingDeck.discards))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择卡牌!")
        val card = gameMode.cardManager.playingDeck.getDiscardCard(cardId)
        if (card.cardType == CardType.SCORING) {
            throw BoardGameException("不能选择计分牌!")
        }

        // 检查是否可以发生事件
        if (!gameMode.eventManager.canActiveCard(card)) {
            throw BoardGameException("所选牌的事件不能发生!")
        }
        this.selectedCard = card
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_VIEW_DISCARD_DECK
}
