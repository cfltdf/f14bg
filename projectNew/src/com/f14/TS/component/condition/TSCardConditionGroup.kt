package com.f14.TS.component.condition

import com.f14.TS.component.TSCard
import com.f14.bg.component.IConditionGroup


class TSCardConditionGroup(override var wcs: MutableList<TSCardCondition> = ArrayList(), override var bcs: MutableList<TSCardCondition> = ArrayList()) : Cloneable, IConditionGroup<TSCard, TSCardCondition> {

    public override fun clone(): TSCardConditionGroup {
        val res = super.clone() as TSCardConditionGroup
        res.wcs = this.wcs.map(TSCardCondition::clone).toMutableList()
        res.bcs = this.bcs.map(TSCardCondition::clone).toMutableList()
        return res
    }
}
