package com.f14.TS.action

import com.f14.TS.component.TSCard
import com.f14.TS.component.TSCountry
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.ability.ActionParamType

/**
 * TS的行动参数

 * @author F14eagle
 */
open class TSGameAction {
    var paramType: ActionParamType? = null
    var targetPower: SuperPower = SuperPower.NONE
    var num: Int = 0
    var country: TSCountry? = null
    var limitNum: Int = 0
    var relateCard: TSCard? = null
    var card: TSCard? = null
    var includeSelf: Boolean = false
}
