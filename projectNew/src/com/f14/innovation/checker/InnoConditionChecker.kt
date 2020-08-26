package com.f14.innovation.checker

import com.f14.bg.consts.ConditionResult
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.Innovation
import com.f14.innovation.command.InnoCommandList
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.consts.InnoPlayerTargetType
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

abstract class InnoConditionChecker(val gameMode: InnoGameMode, val player: InnoPlayer, val initParam: InnoInitParam?, val resultParam: InnoResultParam, val ability: InnoAbility) {
    lateinit var commandList: InnoCommandList

    /**
     * 执行校验
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun check(): Boolean

    /**
     * 执行动作
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun execute() {
        this.setExecuteResult(this.check())
    }

    val game: Innovation
        get() = this.gameMode.game

    /**
     * 取得返回结果中的牌

     * @return
     */

    val resultCards: List<InnoCard>
        get() = this.resultParam.cards.cards

    /**
     * 按照参数配置取得实际的目标玩家

     * @return
     */
    val targetPlayer: InnoPlayer
        get() = when (this.initParam?.targetPlayer) {
            InnoPlayerTargetType.MAIN_PLAYER -> this.commandList.mainPlayer
            else -> this.player
        }
//            if (this.initParam != null && this.initParam!!.targetPlayer != null) {
//                when (this.initParam!!.targetPlayer) {
//                    InnoPlayerTargetType.MAIN_PLAYER -> {
//                        if (this.commandList != null) {
//                            return this.commandList!!.mainPlayer
//                        }
//                    }
//                    else -> {
//                    }
//                }
//            }
//            return this.player

    /**
     * 设置返回参数
     */
    protected fun setExecuteResult(result: Boolean) {
        if (result) {
            this.resultParam.conditionResult = this.initParam?.conditionResult ?: ConditionResult.TRUE
//            if (this.initParam != null && this.initParam!!.conditionResult != null) {
//                this.resultParam.conditionResult = this.initParam!!.conditionResult
//            } else {
//                this.resultParam.conditionResult = ConditionResult.TRUE
//            }
        } else {
            this.resultParam.conditionResult = ConditionResult.ELSE
        }
        this.commandList.commandParam.isChecked = true
    }
}
