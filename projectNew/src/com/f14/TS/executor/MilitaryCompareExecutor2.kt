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
class MilitaryCompareExecutor2(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = gameMode.game.usaPlayer
        val oppositePlayer = gameMode.game.ussrPlayer

        val value = player.getProperty(TSProperty.MILITARY_ACTION)
        val oppoValue = oppositePlayer.getProperty(TSProperty.MILITARY_ACTION)

        // 如果美国在军事行动点数上落后，美国得2分
        if (value < oppoValue) {
            gameMode.game.adjustVp(-2)
        }

    }

}
