package com.f14.RFTG.card

import com.f14.RFTG.consts.GameState

/**
 * 额外VP的能力
 * @author F14eagle
 */
class BonusAbility : Ability() {
    var vp = 0
    var chip = 0
    var phase: GameState? = null

    override fun test(card: RaceCard) = super.test(card) && GameState.getPhaseClass(phase)?.let(card::hasAbility) != false
}
