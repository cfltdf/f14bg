package com.f14.TTA.executor

import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

abstract class TTAActionExecutor(protected var param: RoundParam) {
    protected val gameMode = param.gameMode
    protected val player = param.player
    protected var checked = false

    /**
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    open fun check() {
        this.checked = true
    }

    /**
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    abstract fun execute()

}
