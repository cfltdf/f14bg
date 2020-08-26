package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #71-尼克松打出中国牌 的执行器

 * @author F14eagle
 */
class Custom71Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = gameMode.game.usaPlayer
        if (gameMode.cardManager.chinaOwner == SuperPower.USA) {
            // 如果美国玩家已经得到中国牌,则+2VP
            gameMode.game.adjustVp(player, 2)
        } else {
            // 否则的话,美国玩家得到中国牌,不可用
            gameMode.game.changeChinaCardOwner(player, false)
        }
    }

}
