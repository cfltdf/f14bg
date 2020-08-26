package com.f14.bg.listener

import com.f14.F14bg.network.CmdFactory
import com.f14.bg.BoardGameConfig
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.player.Player
import com.f14.bg.report.BgReport

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

abstract class ActionStep<P : Player, C : BoardGameConfig, R : BgReport<P>>(val listener: ActionListener<P, C, R>) {
    /**
     * 判断步骤是否已经完成
     */
    var isOver: Boolean = false
        protected set
    /**
     * 是否在结束时删除剩余的步骤
     */
    var clearOtherStep = false
        protected set

    /**
     * 步骤执行完成后是否自动结束
     */
    fun autoOver(): Boolean = true

    /**
     * 创建步骤结束时的消息
     * @param player
     * @return
     */
    protected fun createStepOverResponse(player: P): BgResponse {
        val res = CmdFactory.createGameResponse(this.actionCode, player.position)
        res.public("stepCode", this.stepCode)
        res.public("ending", true)
        return res
    }

    /**
     * 创建步骤开始时的消息
     * @param player
     * @return
     */
    protected open fun createStepStartResponse(player: P): BgResponse {
        val res = CmdFactory.createGameResponse(this.actionCode, player.position)
        res.public("stepCode", this.stepCode)
        return res
    }

    /**
     * 步骤中的行动
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun doAction(action: GameAction)

    /**
     * 执行步骤
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun execute(action: GameAction) {
        val stepCode = action.getAsString("stepCode")
        if (this.stepCode != stepCode) {
            throw BoardGameException("你还不能执行这个行动!")
        }
        this.doAction(action)
        if (this.autoOver()) {
            this.isOver = true
        }
    }

    /**
     * 取得行动代码
     * @return
     */
    abstract val actionCode: Int

    /**
     * 取得步骤代码
     * @return
     */
    abstract val stepCode: String

    /**
     * 步骤结束时触发的事件
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    open fun onStepOver(player: P) {
        this.createStepOverResponse(player).send(listener.mode, player)
    }

    /**
     * 步骤开始时触发的事件
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    open fun onStepStart(player: P) {
        this.createStepStartResponse(player).send(listener.mode, player)
    }
}
