package com.f14.TS.component.ability

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.action.ActionParam
import com.f14.TS.component.condition.TSGameCondition
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.ability.TSAbilityGroupType
import java.util.*

/**
 * TS能力组合

 * @author F14eagle
 */
class TSAbilityGroup {
    var groupType: TSAbilityGroupType = TSAbilityGroupType.NORMAL

    var abilities: MutableList<TSAbility> = ArrayList()
    var abilitiesGroup1: MutableList<TSAbility> = ArrayList()
    var abilitiesGroup2: MutableList<TSAbility> = ArrayList()
    var descr1: String = ""
    var descr2: String = ""
    var trigPower: SuperPower = SuperPower.NONE
    var activeParam: ActionParam? = null

    var gamebcs: List<TSGameCondition> = arrayListOf()

    var gamewcs: List<TSGameCondition> = arrayListOf()

    fun test(o: TSGameMode, player: TSPlayer): Boolean {
        // 白黑名单中的条件均为 "或" 关系
        val black = this.gamebcs.isEmpty() || this.gamebcs.none { it.test(o, player) }
        val white = this.gamewcs.isEmpty() || this.gamewcs.any { it.test(o, player) }
        return black and white
    }

}
