package com.f14.TTA.executor.active

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.ProductionTradeListener
import com.f14.bg.exception.BoardGameException

class TTAActiveTradeResourceChoiceExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val listener = ProductionTradeListener(gameMode, player, card.activeAbility!!)
        val res = gameMode.insertListener(listener)
        val property = res.get<TTAProperty>("property")
        if (property != null) {
            gameMode.game.playerAddPoint(player, property)
            this.actived = true
        }
    }

}
