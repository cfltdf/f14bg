package com.f14.TS.action

import com.f14.TS.component.condition.TSCountryConditionGroup
import com.f14.TS.consts.EffectType

/**
 * TS的持续效果对象
 * @author F14eagle
 */
class TSEffect : TSGameAction() {
    lateinit var effectType: EffectType
    var countryCondGroup: TSCountryConditionGroup? = null

}
