package com.f14.TS.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.listener.initParam.ConditionInitParam
import com.f14.bg.exception.BoardGameException

/**
 * 检查对方是否有手牌 的判断条件
 * @author F14eagle
 */
class CheckHandCondition(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ConditionInitParam) : TSActionCondition(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun test(): Boolean {
        val opposite = gameMode.game.getOppositePlayer(this.initiativePlayer.superPower)
        return !opposite.hands.empty
    }

}
