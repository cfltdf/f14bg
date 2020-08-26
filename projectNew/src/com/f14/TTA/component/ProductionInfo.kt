package com.f14.TTA.component

import com.f14.TTA.component.card.CivilCard
import com.f14.TTA.consts.CivilizationProperty
import java.util.*

class ProductionInfo(val property: CivilizationProperty) : HashMap<CivilCard, Int>() {

    operator fun plusAssign(info: Map<CivilCard, Int>) {
        for ((c, v) in info) {
            this[c] = this.getOrDefault(c, 0) + v
        }
    }

    val totalValue: Int
        get() = this.entries.sumBy { (c, v) -> c.property.getProperty(property) * v }
}
