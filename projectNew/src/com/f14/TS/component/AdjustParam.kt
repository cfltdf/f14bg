package com.f14.TS.component

import com.f14.TS.consts.ActionType
import com.f14.TS.consts.SuperPower
import com.f14.bg.component.Convertable
import com.f14.bg.report.Printable
import kotlin.math.abs

/**
 * 调整的参数

 * @author F14eagle
 */
open class AdjustParam(var adjustPower: SuperPower, var actionType: ActionType, var orgCountry: TSCountry) : Printable, Convertable {

    // 克隆一个临时的国家对象用来存放调整的临时值
    var tempCountry: TSCountry = orgCountry.clone()
    var num: Int = 0
    var op: Int = 0
    var modify: Int = 0
    var pastCoup: Int = 0

    /**
     * 将临时影响力应用到正式的影响力
     */
    fun apply() {
        this.orgCountry.customSetInfluence(SuperPower.USSR, this.tempCountry.customGetInfluence(SuperPower.USSR))
        this.orgCountry.customSetInfluence(SuperPower.USA, this.tempCountry.customGetInfluence(SuperPower.USA))
    }

    override // 按照操作类型输出不同的字符串
    val reportString: String
        get() {
            val sb = StringBuilder()
            when (this.actionType) {
                ActionType.ADD_INFLUENCE, // 使用OP放置影响力
                ActionType.ADJUST_INFLUENCE // 调整影响力
                -> sb.append("在").append(orgCountry.reportString).append(if (num >= 0) "添加" else "移除").append("了").append(abs(num)).append("点").append(adjustPower.chinese).append("影响力 ")
                ActionType.COUP -> { // 政变
                    sb.append("在").append(orgCountry.reportString).append("发动政变,掷骰结果为 ")
                    if (pastCoup != 0) sb.append(pastCoup).append(" ,选择重新掷骰,重掷结果为 ")
                    sb.append(num)
                    if (modify != 0) sb.append(if (modify > 0) "+$modify" else modify)
                    sb.append("+").append(op).append("-2x").append(orgCountry.stabilization).append(" = ").append(num + modify + op - 2 * orgCountry.stabilization).append(", ")
                }
                ActionType.SET_INFLUENCE -> // 设置影响力
                    sb.append("在").append(orgCountry.reportString).append("调整了").append(adjustPower.chinese).append("的影响力 ")
                else -> {
                }
            }
            sb.append(orgCountry.influenceString).append(" => ").append(tempCountry.influenceString)
            return sb.toString()
        }


    override fun toMap(): Map<String, Any> = mapOf("countryName" to this.orgCountry.name, "num" to this.num, "op" to this.op)
}
