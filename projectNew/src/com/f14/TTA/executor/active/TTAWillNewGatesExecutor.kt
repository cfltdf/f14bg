package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilAbilityType
import com.f14.bg.exception.BoardGameException

/**
 * 新版盖茨的处理器
 * @author 吹风奈奈
 */
class TTAWillNewGatesExecutor(param: RoundParam, card: TTACard) : TTAWillExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        card.abilities.filter { it.abilityType == CivilAbilityType.PA_NEW_GATES_ABILITY }.map { gameMode.game.playerAddPoint(player, it.property, it.getAvailableNumber(player)) }.forEach { gameMode.report.playerAddPoint(player, it) }
    }

}
