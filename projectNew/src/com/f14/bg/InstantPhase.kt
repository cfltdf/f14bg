package com.f14.bg

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.bg.exception.BoardGameException


/**
 * 即时阶段
 * @param <GM>
 * @author F14eagle
</GM> */
abstract class InstantPhase<GM : GameMode<*, *, *>>(protected val mode: GM) {

    /**
     * 执行行动
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    abstract fun doAction()

    /**
     * 执行阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun execute() {
        this.sendPhaseStartCommand()
        this.doAction()
        this.sendPhaseEndCommand()
    }

    /**
     * 取得可以处理的指令code
     * @return
     */
    protected abstract val validCode: Int

    /**
     * 向所有玩家发送阶段结束的指令
     */
    @Synchronized
    protected fun sendPhaseEndCommand() {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_PHASE_END, -1)
                .public("validCode", this.validCode)
                .send(mode)
    }

    /**
     * 向所有玩家发送阶段开始的指令
     */
    @Synchronized
    protected fun sendPhaseStartCommand() {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_PHASE_START, -1)
                .public("validCode", this.validCode)
                .send(mode)
    }

}
