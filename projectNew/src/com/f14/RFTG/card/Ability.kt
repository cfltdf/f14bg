package com.f14.RFTG.card

import com.f14.RFTG.consts.ActiveType
import com.f14.RFTG.consts.Skill
import com.f14.bg.component.AbstractCondition
import com.f14.bg.component.IConditionGroup
import com.google.gson.annotations.SerializedName


/**
 * 卡牌的能力
 * @author F14eagle
 */
abstract class Ability : AbstractCondition<RaceCard>(), IConditionGroup<RaceCard, Condition> {
    @SerializedName("active")
    var isActive = false
    var skill: Skill? = null
    @SerializedName("discardAfterActived")
    var isDiscardAfterActived = false
    var maxNum = 1
    var activeType: ActiveType? = null

    var whiteCondition: Condition? = null
    var blackCondition: Condition? = null

    override val wcs: MutableList<Condition>
        get() = listOfNotNull(whiteCondition).toMutableList()

    override val bcs: MutableList<Condition>
        get() = listOfNotNull(blackCondition).toMutableList()

}
