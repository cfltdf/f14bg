package com.f14.TS.action

import com.f14.TS.consts.ActionType
import com.f14.TS.consts.Country
import com.f14.TS.consts.EffectType
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.ability.ActionParamType
import com.f14.TS.consts.ability.ExpressionSession
import com.google.gson.annotations.SerializedName

/**
 * 行动参数

 * @author F14eagle
 */
class ActionParam {
    /**
     * 参数类型
     */
    var paramType: ActionParamType? = null
    /**
     * 行动初始化参数的类型
     */
    var actionType: ActionType = ActionType.ADD_INFLUENCE
    /**
     * 效果类型
     */
    var effectType: EffectType? = null
    /**
     * 目标超级大国
     */
    var targetPower: SuperPower = SuperPower.NONE
    var num: Int = 0
    var expression: String? = null
    var expressionSession: ExpressionSession? = null
    var countryNum: Int = 0
    var limitNum: Int = 0
    var country: Country? = null
    var descr: String = ""
    @SerializedName("canPass")
    var isCanPass: Boolean = false
    @SerializedName("canCancel")
    var isCanCancel: Boolean = false
    var clazz: String? = null
    @SerializedName("canLeft")
    var isCanLeft: Boolean = false

    @SerializedName("canAddInfluence")
    var isCanAddInfluence = true
    @SerializedName("canCoup")
    var isCanCoup = true
    @SerializedName("canRealignment")
    var isCanRealignment = true
    var isFreeAction: Boolean = false
    @SerializedName("includeSelf")
    var isIncludeSelf = false

    var trigPower: SuperPower = SuperPower.NONE

}
