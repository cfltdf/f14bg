package com.f14.TS.factory

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.action.ActionParam
import com.f14.TS.action.TSEffect
import com.f14.TS.action.TSGameAction
import com.f14.TS.component.TSCard
import com.f14.TS.component.TSCountry
import com.f14.TS.component.ability.TSAbility
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.ability.ExpressionSession
import com.f14.bg.exception.BoardGameException
import net.sourceforge.jeval.Evaluator
import org.apache.log4j.Logger
import java.util.*

/**
 * TS的行动参数工厂

 * @author F14eagle
 */
object GameActionFactory {
    private var log = Logger.getLogger(GameActionFactory::class.java)

    /**
     * 创建持续效果对象
     * @param gameMode
     * @param relatePlayer
     * @param relateCard
     * @param param
     * @param ability
     * @return
     */
    fun createEffect(gameMode: TSGameMode, relatePlayer: TSPlayer, relateCard: TSCard, param: ActionParam, ability: TSAbility): TSEffect {
        val action = TSEffect()
        val context = ActionContext(gameMode, relatePlayer, param)
        action.paramType = param.paramType
        action.targetPower = context.targetSuperPower
        action.num = context.num
        action.country = context.country
        action.limitNum = param.limitNum
        action.relateCard = relateCard
        action.effectType = param.effectType!!
        // 复制能力中对国家的限制参数
        action.countryCondGroup = ability.countryCondGroup
        return action
    }

    /**
     * 创建行动参数对象
     * @param gameMode
     * @param relatePlayer
     * @param relateCard
     * @param param
     * @return
     */
    fun createGameAction(gameMode: TSGameMode, relatePlayer: TSPlayer, relateCard: TSCard?, param: ActionParam): TSGameAction {
        val action = TSGameAction()
        val context = ActionContext(gameMode, relatePlayer, param)
        action.paramType = param.paramType
        action.targetPower = context.targetSuperPower
        action.num = context.num
        action.country = context.country
        action.limitNum = param.limitNum
        action.relateCard = relateCard
        action.includeSelf = param.isIncludeSelf
        return action
    }

    /**
     * 行动参数上下文对象

     * @author F14eagle
     */
    private class ActionContext(var gameMode: TSGameMode, var relatePlayer: TSPlayer, var actionParam: ActionParam) {

        var country: TSCountry? = null

        private var expressionParam: MutableMap<String, Number> = HashMap()

        init {
            this.country = this.initCountry()
            this.createExpressionParam()
        }

        /**
         * 按照范围设置表达式中用到的参数
         */
        private fun createExpressionParam() {
            this.expressionParam.clear()
            when (actionParam.expressionSession) {
                ExpressionSession.GLOBAL // 全局参数
                -> {
                    this.expressionParam["defcon"] = gameMode.defcon
                    this.expressionParam["vp"] = gameMode.vp
                }
                ExpressionSession.COUNTRY // 国家参数
                -> country?.let {
                    this.expressionParam["country.stabilization"] = it.stabilization
                    this.expressionParam["country.ussr"] = it.ussrInfluence
                    this.expressionParam["country.usa"] = it.usaInfluence
                }
            }
        }

        /**
         * 取得国家
         * @return
         */
        fun initCountry(): TSCountry? {
            if (actionParam.country != null) {
                try {
                    return gameMode.countryManager.getCountry(actionParam.country!!)
                } catch (e: BoardGameException) {
                    log.error("获取国家对象时发生错误!", e)
                }
            }
            return null
        }

        /**
         * 读取表达式的值
         * @param expression
         * @return
         */
        private fun getExpressionValue(expression: String): Int {
            val eval = Evaluator()
            return try {
                // 设置参数
                for (key in this.expressionParam.keys) {
                    eval.putVariable(key, expressionParam[key].toString())
                }
                eval.evaluate(expression).toDouble().toInt()
            } catch (e: Exception) {
                log.error("读取表达式发生错误!", e)
                0
            }
        }

        /**
         * 取得实际num值
         * @return
         */
        val num: Int
            get() = if (actionParam.expression.isNullOrEmpty()) {
                actionParam.num
            } else {
                this.getExpressionValue(actionParam.expression!!)
            }

        /**
         * 取得目标玩家
         * @return
         */

        val targetSuperPower: SuperPower
            get() = when (actionParam.targetPower) {
                SuperPower.USSR, SuperPower.USA -> actionParam.targetPower
                SuperPower.CURRENT_PLAYER -> gameMode.turnPlayer!!.superPower
                SuperPower.PLAYED_CARD_PLAYER -> this.relatePlayer.superPower
                SuperPower.OPPOSITE_PLAYER -> this.relatePlayer.superPower.oppositeSuperPower
                else -> SuperPower.NONE
            }
    }
}
