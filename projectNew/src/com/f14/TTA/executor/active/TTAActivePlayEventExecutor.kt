package com.f14.TTA.executor.active

import com.f14.TTA.component.card.EventCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.TTA.listener.active.ActivePlayEventListener
import com.f14.bg.exception.BoardGameException

/**
 * 直接打出事件牌的处理器

 * @author 吹风奈奈
 */
class TTAActivePlayEventExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val l = ActivePlayEventListener(param, card)
        l.addListeningPlayer(param.player)
        val res = gameMode.insertListener(l)
        val cardId = res.getString("cardId")
        if (cardId.isNotEmpty()) {
            val card = player.militaryHands.getCard(cardId) as EventCard
            gameMode.game.playerRemoveHand(player, card)
            gameMode.cardBoard.lastEvent = card
            gameMode.report.playerPlayCard(player, null, 0, card)
            TTAExecutorFactory.createEventCardExecutor(param, card).execute()
            this.actived = true
        }
    }

}
