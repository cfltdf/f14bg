package com.f14.TS.component

import com.f14.TS.consts.ActionType
import com.f14.TS.consts.SuperPower
import java.util.*
import kotlin.math.max

/**
 * 调整阵营的调整参数

 * @author F14eagle
 */
class RealignmentAdjustParam(adjustPower: SuperPower, actionType: ActionType, country: TSCountry) : AdjustParam(adjustPower, actionType, country) {

    var info: MutableMap<SuperPower, RealignmentInfo> = HashMap()

    init {
        this.init()
    }

    /**
     * 取得调整结果参数
     * @param power
     * @return
     */
    fun getRealignmentInfo(power: SuperPower): RealignmentInfo {
        return this.info[power]!!
    }


    override val reportString: String
        get() {
            val ussr = this.getRealignmentInfo(SuperPower.USSR)
            val usa = this.getRealignmentInfo(SuperPower.USA)
            return "在${orgCountry.reportString}调整阵营,掷骰结果为 $usa:$ussr ${orgCountry.influenceString} => ${tempCountry.influenceString}"
        }

    /**
     * 初始化参数
     */
    private fun init() {
        info[SuperPower.USSR] = RealignmentInfo()
        info[SuperPower.USA] = RealignmentInfo()
    }

    /**
     * 设置调整阵营的加值
     * @param bonus
     */
    fun setRealignmentBonus(bonus: Map<SuperPower, Int>) {
        for (power in bonus.keys) {
            this.getRealignmentInfo(power).bonus = bonus.getValue(power)
        }
    }

    inner class RealignmentInfo {
        var roll: Int = 0
        var bonus: Int = 0
        var modify: Int = 0

        /**
         * 取得总值,但是不能小于0
         * @return
         */
        val total: Int
            get() = max(this.roll + this.bonus + this.modify, 0)


        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(this.total).append("(").append(this.roll)
            if (this.bonus != 0) {
                sb.append(if (this.bonus > 0) "+" + this.bonus else this.bonus)
            }
            if (this.modify != 0) {
                sb.append(if (this.modify > 0) "+" + this.modify else this.modify)
            }
            sb.append(")")
            return sb.toString()
        }
    }

}
