package com.f14.TTA.component.ability

import com.f14.TTA.component.Condition
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.TTACard
import com.f14.bg.component.IConditionGroup
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * 卡牌的能力
 * @author F14eagle
 */
open class CardAbility : IConditionGroup<TTACard?, Condition> {
    /**
     * 白名单条件
     */
    override var wcs: MutableList<Condition> = ArrayList(0)
    /**
     * 黑名单条件
     */
    override var bcs: MutableList<Condition> = ArrayList(0)

    @SerializedName("propertyMap")
    var property = TTAProperty()
    var descr: String = ""

    /**
     * 在给定列表中筛选出符合条件的卡牌
     * @return
     */
    fun getAvailableCards(objects: Collection<TTACard>) = objects.filter(this::test)

    fun testGroup(cards: Collection<TTACard>, card: TTACard): Boolean {
        val cardp = this.wcs.map { it test card }
        val groupp = this.wcs.map { cards.any(it::test) }
        val total = cardp zip groupp
        return cardp.any() and groupp.any() and total.all { (a, b) -> a or b }
    }
}
