package com.f14.TS.listener.initParam

import com.f14.TS.component.RealignmentAdjustParam
import com.f14.TS.component.TSCountry


class RealignmentInitParam : OPActionInitParam() {

    /**
     * 创建调整参数
     * @param country
     * @return
     */

    override fun createAdjustParam(country: TSCountry): RealignmentAdjustParam {
        return RealignmentAdjustParam(this.targetPower, this.actionType, country)
    }

}
