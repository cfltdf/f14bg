package com.f14.innovation.component.ability

import com.f14.bg.consts.ConditionResult
import com.f14.innovation.consts.InnoActiveType
import java.util.*
import kotlin.collections.HashMap

class InnoAbilityGroup {
    var repeat: Int = 0

    var activeType: InnoActiveType? = null

    var abilities: MutableList<InnoAbility> = ArrayList()

    private var conditionAbilities: MutableMap<ConditionResult, InnoAbilityGroup> = HashMap()

    /**
     * 取得判断结果对应的InnoAbilityGroup
     * @param conditionResult
     * @return
     */
    fun getConditionAbilityGroup(conditionResult: ConditionResult) = this.conditionAbilities[conditionResult]

}
