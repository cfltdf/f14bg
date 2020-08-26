package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.TS.utils.TSRoll
import com.f14.bg.exception.BoardGameException


/**
 * 奥林匹克运动会行动执行器

 * @author F14eagle
 */
class OlympicExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val oppositePlayer = gameMode.game.getOppositePlayer(this.initiativePlayer!!.superPower)
        // 双方掷骰,主动玩家结果+2，平局时重掷
        var winner: TSPlayer? = null
        while (winner == null) {
            winner = this.roll(initiativePlayer!!, oppositePlayer)
        }
        // 胜者VP+2
        val num = gameMode.game.convertVp(winner, 2)
        gameMode.game.adjustVp(num)
    }

    /**
     * 模拟双方掷骰

     * @param p1

     * @param p2

     * @return
     */

    private fun roll(p1: TSPlayer, p2: TSPlayer): TSPlayer? {
        val r1 = TSRoll.roll()
        val r2 = TSRoll.roll()
        // p1有+2修正值
        val b1 = 2
        val b2 = 0
        gameMode.report.playerRoll(p1, r1, b1)
        gameMode.report.playerRoll(p2, r2, b2)
        val res1 = r1 + b1
        val res2 = r2 + b2
        return when {
            res1 > res2 -> p1
            res1 < res2 -> p2
            else -> null
        }
    }

}
