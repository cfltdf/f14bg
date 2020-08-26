package com.f14.TTA.component.param

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.CardAbility
import java.util.*

/**
 * 计算付费用的参数

 * @author F14eagle
 */
class CostParam {
    /**
     * 最终需要支付的费用
     */
    val cost: TTAProperty = TTAProperty()
    /**
     * 使用到的临时能力及其资源数量
     */
    val usedAbilities: MutableMap<CardAbility, TTAProperty> = HashMap()

    /**
     * 使用临时能力中的资源
     * @param ability
     */
    fun useAbility(ability: CardAbility, property: TTAProperty) {
        val use = property.clone()
        this.usedAbilities[ability] = use
        this.cost.addProperties(use, -1)
    }

    operator fun plusAssign(that: CostParam) {
        this.cost += that.cost
        this.usedAbilities += that.usedAbilities
    }
}
