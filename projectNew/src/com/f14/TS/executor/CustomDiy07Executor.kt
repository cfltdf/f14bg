package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.listener.initParam.ExecutorInitParam

/**
 * Created by 吹风奈奈 on 2017/8/8.
 */
class CustomDiy07Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {
    override fun execute() {
        gameMode.game.changeChinaCardOwner(null, false)
    }
}