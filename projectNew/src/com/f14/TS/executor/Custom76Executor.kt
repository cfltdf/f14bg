package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #76-乌苏里江冲突 的执行器

 * @author F14eagle
 */
class Custom76Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        // 美国玩家得到中国牌并且可用
        val player = gameMode.game.usaPlayer
        gameMode.game.changeChinaCardOwner(player, true)
    }

}
