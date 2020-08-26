package com.f14.TS.component.condition

import com.f14.TS.component.TSCard
import com.f14.TS.consts.SuperPower
import com.f14.bg.component.AbstractCondition


class TSCardCondition : AbstractCondition<TSCard>() {

    var superPower: SuperPower = SuperPower.NONE
    var tsCardNo: Int = 0
    var limitOp: Int = 0

    override fun clone(): TSCardCondition {
        return super.clone() as TSCardCondition
    }

    override fun test(obj: TSCard): Boolean = when {
        this.superPower != SuperPower.NONE && this.superPower != obj.superPower -> false
        this.limitOp > 0 && obj.op < this.limitOp -> false
        this.tsCardNo > 0 && obj.tsCardNo != this.tsCardNo -> false
        else -> true
    }

}
