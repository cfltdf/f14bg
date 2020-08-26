package com.f14.innovation.executor

import com.f14.bg.anim.AnimType
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.Innovation
import com.f14.innovation.command.InnoCommandList
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoActiveType
import com.f14.innovation.consts.InnoPlayerTargetType
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

abstract class InnoActionExecutor(val gameMode: InnoGameMode, val player: InnoPlayer, val initParam: InnoInitParam?, val resultParam: InnoResultParam, val ability: InnoAbility?, val abilityGroup: InnoAbilityGroup?) {
    lateinit var commandList: InnoCommandList

    /**
     * 行动实现
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun doAction()

    /**
     * 执行动作
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun execute() {
        this.doAction()
        this.setExecuteResult()
    }

    /**
     * 取得当前针对处理效果的玩家
     * @return
     */
    val currentPlayer: InnoPlayer
        get() = this.commandList.currentPlayer

    val game: Innovation
        get() = this.gameMode.game

    /**
     * 取得触发能力的主要玩家
     * @return
     */
    protected val mainPlayer: InnoPlayer
        get() = this.commandList.mainPlayer

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
            InnoPlayerTargetType.CURRENT_PLAYER -> this.currentPlayer
            else -> this.player
        }

    /**
     * 设置返回参数
     */
    protected fun setExecuteResult() {
        this.resultParam.animType = this.initParam?.animType ?: AnimType.DIRECT
        if (this.initParam?.isSetActived == true) {
            this.setPlayerActived(this.currentPlayer)
//            when (this.abilityGroup?.activeType) {
//                InnoActiveType.DEMAND // 如果是被要求的能力,则设置为被要求执行过能力
//                -> this.commandList.setPlayerDomanded(this.currentPlayer)
//                else // 否则就设置为触发过能力
//                -> this.commandList.setPlayerActived(this.currentPlayer)
//            }
        }
        // 设置是否再次执行AbilityGroup的参数
        if (this.initParam?.isSetActiveAgain == true) {
            this.commandList.commandParam.isSetActiveAgain = true
        }
    }

    /**
     * 设置玩家触发过行动
     * @param player
     */
    protected fun setPlayerActived(player: InnoPlayer) {
        when (this.abilityGroup?.activeType) {
            InnoActiveType.DEMAND // 如果是被要求的能力,则设置为被要求执行过能力
            -> this.commandList.setPlayerDomanded(player)
            else // 否则就设置为触发过能力
            -> this.commandList.setPlayerActived(player)
        }
    }

}
