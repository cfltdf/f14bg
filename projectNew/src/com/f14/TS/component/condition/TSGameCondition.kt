package com.f14.TS.component.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TSPhase


class TSGameCondition(var phase: TSPhase? = null, var playedBy: SuperPower = SuperPower.NONE) {

    fun test(o: TSGameMode, player: TSPlayer): Boolean {
        return !(this.phase != null && this.phase != o.currentPhase) && !(this.playedBy != SuperPower.NONE && this.playedBy != player.superPower)
    }

}
