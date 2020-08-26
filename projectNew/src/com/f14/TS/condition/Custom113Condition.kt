package com.f14.TS.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSProperty
import com.f14.TS.listener.initParam.ConditionInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #80-一小步 的判断条件

 * @author F14eagle
 */
class Custom113Condition(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ConditionInitParam) : TSActionCondition(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun test(): Boolean {
        // 检查当前玩家的太空竞赛等级是否比对手低
        val player = this.initiativePlayer
        val opposite = gameMode.game.getOppositePlayer(player.superPower)

        return player.getProperty(TSProperty.SPACE_RACE) <= opposite.getProperty(TSProperty.SPACE_RACE)
    }

}
