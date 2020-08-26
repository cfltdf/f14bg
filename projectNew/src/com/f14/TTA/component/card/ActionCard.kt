package com.f14.TTA.component.card

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.ActionCardAbility


/**
 * 行动牌
 * @author F14eagle
 */
class ActionCard : CivilCard() {
    lateinit var actionAbility: ActionCardAbility


    override fun clone(): ActionCard {
        return super.clone() as ActionCard
    }

    /**
     * 按照排名和人数取得最终调整数据的结果
     * @param rank
     * @param playerNum
     * @return
     */
    fun getFinalRankValue(rank: Int, playerNum: Int): TTAProperty {
        val value = this.actionAbility.rankValue.getValue(playerNum.toString())
        val finalValue = (rank - 1) * value
        val property = TTAProperty()
        property.setProperty(this.actionAbility.getProperty!!, finalValue)
        return property
    }
}
