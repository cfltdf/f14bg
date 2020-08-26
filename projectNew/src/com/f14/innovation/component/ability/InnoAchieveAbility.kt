package com.f14.innovation.component.ability

import com.f14.innovation.consts.InnoAchieveTrigType
import java.util.*

/**
 * 成就牌的能力

 * @author F14eagle
 */
class InnoAchieveAbility {

    var trigTypes: MutableList<InnoAchieveTrigType> = ArrayList()
        set(trigTypes) {
            this.trigTypes.addAll(trigTypes)
//            for (o in trigTypes) {
//                this.trigTypes.add(InnoAchieveTrigType.valueOf(o.toString()))
//            }
        }
    lateinit var achieveClass: String

    /**
     * 判断是否包含指定的触发类型
     * @param types
     * @return
     */
    fun contains(vararg types: InnoAchieveTrigType) = types.any(this.trigTypes::contains)

}
