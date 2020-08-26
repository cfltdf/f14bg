package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.executor.TTARearrangeCurrentEventExecutor
import com.f14.TTA.listener.active.ActivePlayEventListener
import com.f14.bg.exception.BoardGameException
import java.util.*

/**
 * 和平城的处理器
 * @author 吹风奈奈
 */
class TTAActiveHepingChengExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val l = ActivePlayEventListener(param, card)
        l.addListeningPlayer(param.player)
        val res = gameMode.insertListener(l)
        val cardId = res.getString("cardId")
        if (cardId.isNotEmpty()) {
            val card = player.militaryHands.getCard(cardId)
            val cards = ArrayList(gameMode.cardBoard.currentEvents)
            if (cards.size > 0) {
                for (c in cards) {
                    val p = gameMode.cardBoard.currentEventRelation[c]
                    gameMode.game.sendRemoveEventResponse(p, card, false, null)
                    gameMode.cardBoard.currentEventRelation.remove(c)
                }
                gameMode.cardBoard.currentEvents.clear()
                val exCard = cards[0]
                gameMode.report.playerExtrangeEvent(player, card, exCard)
                gameMode.game.playerRemoveHand(player, card)
                gameMode.game.playerAddHand(player, exCard)
                cards[0] = card
                TTARearrangeCurrentEventExecutor(param, cards).execute()
            }
            this.actived = true
        }
    }

}
