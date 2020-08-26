package com.f14.TTA.component.ability

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.consts.ActionAbilityType
import com.f14.TTA.consts.CivilizationProperty
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * 行动牌的能力
 * @author F14eagle
 */
class ActionCardAbility : CardAbility() {
    var abilityType: ActionAbilityType? = null
    /**
     * 按排名得分的能力时用来区分排名类型的字段
     */
    var rankProperty: CivilizationProperty? = null
    /**
     * 按排名得分的能力时用来区分得到的属性
     */
    var getProperty: CivilizationProperty? = null
    /**
     * 按照游戏人数分组的得分状态
     */
    var rankValue: Map<String, Int> = HashMap()
    /**
     * 二选一的属性2
     */
    @SerializedName("propertyMap2")
    var property2 = TTAProperty()

}
