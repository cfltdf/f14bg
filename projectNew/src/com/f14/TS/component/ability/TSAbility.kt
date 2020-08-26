package com.f14.TS.component.ability

import com.f14.TS.action.ActionParam
import com.f14.TS.component.condition.TSCardCondition
import com.f14.TS.component.condition.TSCardConditionGroup
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.component.condition.TSCountryConditionGroup
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.ability.TSAbilityTrigType
import com.f14.TS.consts.ability.TSAbilityType

/**
 * TS的卡牌能力

 * @author F14eagle
 */
class TSAbility {
    lateinit var abilityType: TSAbilityType
    lateinit var abilityTrigType: TSAbilityTrigType
    var trigPower: SuperPower = SuperPower.NONE
    var actionParam: ActionParam = ActionParam()
    val countryCondGroup by lazy { TSCountryConditionGroup(countrywcs, countrybcs) }
    val cardCondGroup by lazy { TSCardConditionGroup(cardwcs, cardbcs) }

    var cardbcs: MutableList<TSCardCondition> = ArrayList()
    val cardwcs: MutableList<TSCardCondition> = ArrayList()

    val countrybcs: MutableList<TSCountryCondition> = ArrayList()
    val countrywcs: MutableList<TSCountryCondition> = ArrayList()

}
