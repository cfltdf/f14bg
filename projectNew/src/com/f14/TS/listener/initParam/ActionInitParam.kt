package com.f14.TS.listener.initParam

import com.f14.TS.component.AdjustParam
import com.f14.TS.component.TSCountry
import com.f14.TS.consts.ActionType
import kotlin.math.abs


/**
 * 行动相关监听器的初始化参数

 * @author F14eagle
 */
open class ActionInitParam : CountryInitParam() {
    /**
     * 调整类型
     */
    var actionType: ActionType = ActionType.ADD_INFLUENCE
    var countryNum: Int = 0
    var limitNum: Int = 0


    override fun clone() = super.clone() as ActionInitParam

    /**
     * 创建调整参数
     * @param country
     * @return
     */

    open fun createAdjustParam(country: TSCountry) = AdjustParam(this.targetPower, this.actionType, country)

    /**
     * 该方法将取得替换{num}值后的提示信息
     * @return
     */
    override val realMsg
        get() = super.realMsg.replace("""\{limitNum\}""".toRegex(), abs(this.limitNum).toString()).replace("""\{countryNum\}""".toRegex(), abs(this.countryNum).toString())
}
