package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException

class TTAWillSuntzu2Executor(param: RoundParam, card: TTACard) : TTAWillExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val destCard = player.tactics ?: return
        gameMode.game.playerAttachCard(player, card, destCard)
        this.actived = true
    }

}
