package com.f14.innovation.param

import com.f14.bg.BGConst
import com.f14.bg.anim.AnimType
import com.f14.bg.consts.ConditionResult
import com.f14.innovation.component.InnoCard
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoPlayerTargetType
import com.f14.innovation.consts.InnoSplayDirection
import com.google.gson.annotations.SerializedName
import kotlin.math.abs

class InnoInitParam {
    var num: Int = BGConst.INT_NULL
    var maxNum: Int = BGConst.INT_NULL
    var level: Int = BGConst.INT_NULL
    var type: String? = null
    var color: InnoColor? = null
    var splayDirection: InnoSplayDirection = InnoSplayDirection.NULL
    var card: InnoCard? = null
    var animType: AnimType? = null
    @SerializedName("setActived")
    var isSetActived: Boolean = false
    @SerializedName("setActiveAgain")
    var isSetActiveAgain: Boolean = false
    /**
     * 提示信息
     */
    var msg = ""
    /**
     * 是否可以跳过执行
     */
    @SerializedName("canPass")
    var isCanPass: Boolean = false
    /**
     * 是否可以取消执行
     */
    @SerializedName("canCancel")
    var isCanCancel: Boolean = false
    var conditionResult: ConditionResult? = null
    /**
     * 是否显示确认按钮,默认为true
     */
    @SerializedName("showConfirmButton")
    var isShowConfrimButton = true
    /**
     * 目标玩家类型
     */
    var targetPlayer: InnoPlayerTargetType? = null
    /**
     * 触发玩家类型
     */
    var trigPlayer: InnoPlayerTargetType? = null
    var isCanChooseSelf: Boolean = false
    /**
     * 是否检查成就
     */
    var isCheckAchieve: Boolean = false

    /**
     * 该方法将取得替换{num}值后的提示信息
     * @return
     */
    val realMsg: String
        get() = msg.replace("""\{num\}""".toRegex(), abs(this.num).toString()).replace("""\{maxNum\}""".toRegex(), abs(this.maxNum).toString())
}
