package com.f14.TS.listener.initParam

import com.f14.TS.component.TSCard
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TrigType
import com.google.gson.annotations.SerializedName
import kotlin.math.abs


/**
 * 初始化参数的基类

 * @author F14eagle
 */
abstract class InitParam : Cloneable {
    /**
     * 执行该监听器的玩家
     */
    var listeningPlayer: SuperPower = SuperPower.NONE
    /**
     * 目标的超级大国
     */
    var targetPower: SuperPower = SuperPower.NONE
    /**
     * 数量
     */
    var num: Int = 0
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
    /**
     * 是否可以不完全选择数量
     */
    @SerializedName("canLeft")
    var isCanLeft: Boolean = false
    /**
     * 使用到的相关卡牌
     */
    var card: TSCard? = null
    /**
     * 触发类型
     */
    var trigType: TrigType? = null
    var clazz: String? = null

    public override fun clone() = super.clone() as InitParam

    /**
     * 该方法将取得替换{num}值后的提示信息
     * @return
     */
    open val realMsg: String
        get() = msg.replace("""\{num\}""".toRegex(), abs(this.num).toString())
}
