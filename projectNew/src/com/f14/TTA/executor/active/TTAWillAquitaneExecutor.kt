package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException

class TTAWillAquitaneExecutor(param: RoundParam, card: TTACard) : TTAWillExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        if (player.leader != null) {
            gameMode.game.playerAddAction(player, ability.property)
        }
    }

}
