package com.f14.TS.component.condition

import com.f14.TS.component.TSCountry
import com.f14.bg.component.IConditionGroup


class TSCountryConditionGroup(override var wcs: MutableList<TSCountryCondition> = ArrayList(), override var bcs: MutableList<TSCountryCondition> = ArrayList()) : Cloneable, IConditionGroup<TSCountry, TSCountryCondition> {

    public override fun clone(): TSCountryConditionGroup {
        val res = super.clone() as TSCountryConditionGroup
        res.wcs = this.wcs.map(TSCountryCondition::clone).toMutableList()
        res.bcs = this.bcs.map(TSCountryCondition::clone).toMutableList()
        return res
    }

}
