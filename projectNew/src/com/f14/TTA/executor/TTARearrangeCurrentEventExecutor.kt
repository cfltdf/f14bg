package com.f14.TTA.executor

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.ChooseMilitaryCardListener
import com.f14.bg.exception.BoardGameException

class TTARearrangeCurrentEventExecutor(param: RoundParam, internal var cards: MutableList<TTACard>) : TTAActionExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        var index = 1
        while (cards.size > 0) {
            val al = ChooseMilitaryCardListener(gameMode, player, cards, index)
            val res = gameMode.insertListener(al)
            val cardId = res.getString("cardId")
            val event = cards.firstOrNull { it.id == cardId } ?: continue
            gameMode.cardBoard.currentEvents.add(event)
            gameMode.cardBoard.currentEventRelation[event] = player
            gameMode.game.sendAddEventResponse(player, event, true, null)
            cards.remove(event)
            index += 1
        }
    }

}
