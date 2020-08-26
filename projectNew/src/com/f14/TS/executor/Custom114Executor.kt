package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.listener.Custom114Listener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class Custom114Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val l = Custom114Listener(trigPlayer, gameMode, initParam)
        val param = gameMode.insertListener(l)
        val confirmString = param.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val defcon = param.getInteger("defcon")
            gameMode.game.setDefcon(defcon)
        }
    }

}
