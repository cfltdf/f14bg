package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam

class TTAWillNobelExecutor(param: RoundParam, card: TTACard) : TTAWillExecutor(param, card) {
    override fun active() {
        gameMode.cardBoard.extraCards.add(card)
    }

}
