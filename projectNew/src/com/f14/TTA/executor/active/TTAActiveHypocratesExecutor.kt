package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException

class TTAActiveHypocratesExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {
    override fun check() {
        val toCard = player.government!!
        if (toCard.workers >= 3) throw BoardGameException("你不能放更多指示物了!")
        super.check()
    }

    override fun active() {
        val toCard = player.government!!
        toCard.addWorkers(1)
        gameMode.game.sendPlayerCardToken(player, toCard, null)
        this.actived = true
    }

}
