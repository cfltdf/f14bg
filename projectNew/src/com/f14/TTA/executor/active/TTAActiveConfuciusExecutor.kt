package com.f14.TTA.executor.active

import com.f14.TTA.component.card.MilitaryCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.executor.TTAPlayEventExecutor
import com.f14.TTA.listener.active.ActivePlayEventListener
import com.f14.bg.exception.BoardGameException

class TTAActiveConfuciusExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val l = ActivePlayEventListener(param, card)
        l.addListeningPlayer(param.player)
        val res = gameMode.insertListener(l)
        val cardId = res.getString("cardId")
        if (cardId.isNotEmpty()) {
            val card = player.militaryHands.getCard(cardId) as MilitaryCard
            TTAPlayEventExecutor(param, card).execute()
            this.actived = true
        }
    }

}