package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #80-一小步 的执行器

 * @author F14eagle
 */
class Custom113Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        // 当前玩家的太空竞赛等级+2
        val player = this.initiativePlayer!!
        gameMode.game.playerAdvanceSpaceRace(player, 1)
    }

}
