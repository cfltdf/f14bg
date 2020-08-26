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
class Custom58Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = gameMode.game.ussrPlayer
        if (gameMode.cardManager.chinaOwner == SuperPower.USSR) {
            // 如果苏联玩家已经得到中国牌,则+1VP
            gameMode.game.adjustVp(player, 1)
        } else {
            // 否则的话,苏联玩家得到中国牌,并且可用
            gameMode.game.changeChinaCardOwner(player, true)
        }
    }

}
