package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #58-文化大革命 的执行器

 * @author F14eagle
 */
class Custom117Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        if (gameMode.cardManager.chinaOwner == SuperPower.USSR) {
            // 如果苏联持有中国牌，将美国的军事行动点数设为0
            gameMode.game.playerSetMilitaryAction(gameMode.game.usaPlayer, 0)
        }
    }

}
