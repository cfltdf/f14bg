package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam

class TTAActiveCatharineExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {
    override fun active() {
        val ps = gameMode.realPlayers.filter { it.defenceMilitary < player.defenceMilitary }
        ps.forEach {
            gameMode.game.playerAddToken(it, ability.property, -1)
            gameMode.report.printCache(it)
        }
        if (ps.count() >= 2 || (ps.count() == 1 && gameMode.realPlayers.count() == 2)) {
            gameMode.game.playerAddToken(player, ability.property)
        }
        this.actived = true
    }

}
