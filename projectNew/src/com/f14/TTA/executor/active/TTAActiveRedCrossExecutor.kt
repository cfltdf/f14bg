package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilAbilityType

class TTAActiveRedCrossExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {
    override fun check() {
        player.checkEnoughResource(ability.property, -1)
        super.check()
    }

    override fun active() {
        gameMode.game.playerAddPoint(player, ability.property)
        val targetPlayer = gameMode.realPlayers.single {
            it.uncompletedWonder?.abilities?.any {
                it.abilityType == CivilAbilityType.PA_RED_CROSS
            } == true
        }
        gameMode.game.playerBuildWonder(targetPlayer, 1)
        gameMode.report.playerBuildWonderCache(player, card as WonderCard, 1)
        gameMode.report.playerAddPoint(player, ability.property)
        this.actived = true
    }

}
