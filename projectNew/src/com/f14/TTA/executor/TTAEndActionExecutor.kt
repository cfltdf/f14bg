package com.f14.TTA.executor

import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.TTAEndTurnListener
import com.f14.bg.exception.BoardGameException


/**
 * 结束行动的处理器

 * @author 吹风奈奈
 */
class TTAEndActionExecutor(param: RoundParam) : TTAActionExecutor(param) {
    var endPhase: Boolean = false

    init {
        this.endPhase = false
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        val l = TTAEndTurnListener(gameMode, player)
        if (l.needResponse()) {
            val res = gameMode.insertListener(l)
            if (!res.getBoolean("confirm")) return
        }
        this.endPhase = true
    }

}
