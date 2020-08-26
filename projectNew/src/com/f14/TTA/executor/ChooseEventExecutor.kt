package com.f14.TTA.executor

import com.f14.TTA.component.card.EventCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.TTA.listener.TTAChooseEventListener

class ChooseEventExecutor(param: RoundParam, val events: List<TTACard>) : TTAActionExecutor(param) {
    override fun execute() {
        val listener = TTAChooseEventListener(param.gameMode, param.player, events)
        val result = gameMode.insertListener(listener)
        val card = result.get<TTACard>("selected") as? EventCard ?: return
        for (ea in card.eventAbilities) {
            val executor = TTAExecutorFactory.createEventAbilityExecutor(param, ea)
            executor.trigPlayer = player
            executor.execute()
        }
    }
}
