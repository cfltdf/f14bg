package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSProperty
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException

/**
 * 军备竞争行动执行器

 * @author F14eagle
 */
class MilitaryCompareExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = this.initiativePlayer!!
        val oppositePlayer = gameMode.game.getOppositePlayer(this.initiativePlayer!!.superPower)

        val value = player.getProperty(TSProperty.MILITARY_ACTION)
        val oppoValue = oppositePlayer.getProperty(TSProperty.MILITARY_ACTION)

        var vp = 0
        // 如果当前玩家的军事行动比对方高,则得1VP
        if (value > oppoValue) {
            vp += 1
            // 如果当前玩家的军事行动达到DEFCON要求,则再加2VP
            if (value >= this.gameMode.defcon) {
                vp += 2
            }
        }

        gameMode.game.adjustVp(player, vp)
    }

}
