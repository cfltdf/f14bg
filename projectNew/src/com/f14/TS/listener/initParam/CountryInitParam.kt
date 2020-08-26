package com.f14.TS.listener.initParam

import com.f14.TS.component.TSCountry
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.component.condition.TSCountryConditionGroup
import com.f14.bg.component.ICondition

/**
 * 国家相关的初始化参数

 * @author F14eagle
 */
abstract class CountryInitParam : InitParam(), ICondition<TSCountry> {
    var conditionGroup = TSCountryConditionGroup()

    fun addBc(o: TSCountryCondition) {
        this.conditionGroup.bcs.add(o)
    }

    fun addWc(o: TSCountryCondition) {
        this.conditionGroup.wcs.add(o)
    }

    /**
     * 清除所有条件
     */
    fun clearConditionGroup() {
        this.conditionGroup.clear()
    }


    override fun clone(): CountryInitParam {
        val res = super.clone() as CountryInitParam
        res.conditionGroup = this.conditionGroup.clone()
        return res
    }

    override infix fun test(obj: TSCountry) = this.conditionGroup test obj
}
