package com.f14.TTA.component.ability

import com.f14.TTA.component.Chooser
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.EventTrigType
import com.f14.TTA.consts.EventType
import com.f14.bg.common.ParamSet
import com.google.gson.annotations.SerializedName
import net.sourceforge.jeval.EvaluationException
import net.sourceforge.jeval.Evaluator


/**
 * 事件能力
 * @author F14eagle
 */
class EventAbility : CardAbility() {
    /**
     * 事件的触发类型
     */
    var trigType: EventTrigType = EventTrigType.INSTANT
    /**
     * 文明选择器
     */
    lateinit var chooser: Chooser
    /**
     * 事件类型
     */
    lateinit var eventType: EventType
    /**
     * 总数
     */
    var amount: Int = 0
    /**
     * 选择资源/食物时用,是否只能选择一种
     */
    @SerializedName("singleSelection")
    var isSingleSelection: Boolean = false
    /**
     * 参照属性
     */
    var byProperty: CivilizationProperty? = null
    @SerializedName("produceFood")
    var isProduceFood: Boolean = false
    @SerializedName("doConsumption")
    var isDoConsumption: Boolean = false
    @SerializedName("produceResource")
    var isProduceResource: Boolean = false
    @SerializedName("doCorruption")
    var isDoCorruption: Boolean = false
    @SerializedName("byResult")
    var isByResult: Boolean = false
    var expression: String? = null
    @SerializedName("winnerSelect")
    var isWinnerSelect: Boolean = false
    var level: Int = 0


    fun eval(param:ParamSet, expression: String): Int {
        val eval = Evaluator()
        PROPS
                .filter { expression.contains(it) }
                .map { it to param.getInteger(it) }
                .forEach { (k, v) -> eval.putVariable(k, v.toString()) }
        return try {
            // 表达式值为最终结果
            eval.getNumberResult(expression).toInt()
        } catch (e: EvaluationException) {
            0
        }
    }

    /**
     * 按照入参和表达式,取得真实的amount值
     * @param param
     * @return
     */
    fun getRealAmount(param: ParamSet): Int {
        if (!this.isByResult) {
            // 如果和结果无关联,则直接返回amount
            return this.amount
        } else {
            // 如果和结果关联
            val expression = this.expression ?: return 1
            return if (expression.isEmpty()) {
                // 如果表达式为空,则返回1
                1
            } else {
                // 计算表达式中的值
                eval(param, expression)
            }
        }
    }

    /**
     * 按照入参和表达式,取得真实的属性/资源调整值对象
     * @param param
     * @return
     */
    fun getRealProperty(param: ParamSet): TTAProperty {
        if (this.isByResult) {
            // 如果和结果关联
            val expression = this.expression ?: return param.get<TTAProperty>("property") ?: TTAProperty()
            return if (expression.isEmpty()) {
                // 如果表达式为空,则返回param中的property值
                param.get<TTAProperty>("property") ?: TTAProperty()
            } else {
                // 否则计算表达式中的值
                val result = eval(param, expression)
                TTAProperty().also { it.addProperties(this.property, result) }
            }
        } else {
            // 如果和结果无关联,则直接返回property
            return this.property
        }
    }

    companion object {
        /**
         * 表达式中可能出现的参数名称
         */
        private val PROPS = arrayOf("level", "totalCost", "decrease_pop", "advantage", "unhappy")
    }
}
