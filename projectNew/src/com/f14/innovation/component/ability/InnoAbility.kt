package com.f14.innovation.component.ability

import com.f14.bg.component.IConditionGroup
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.condition.InnoCardCondition
import com.f14.innovation.consts.InnoAbilityType
import com.f14.innovation.param.InnoInitParam
import com.google.gson.annotations.SerializedName

class InnoAbility : IConditionGroup<InnoCard, InnoCardCondition> {
    lateinit var abilityType: InnoAbilityType
    lateinit var abilityClass: String
    var initParam: InnoInitParam? = null


    @SerializedName("cardbcs")
    override var bcs: MutableList<InnoCardCondition> = mutableListOf()
    @SerializedName("cardwcs")
    override var wcs: MutableList<InnoCardCondition> = mutableListOf()
}
