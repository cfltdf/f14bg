package com.f14.TS.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.listener.initParam.ConditionInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #100-战争游戏 的判断条件

 * @author F14eagle
 */
class Custom100Condition(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ConditionInitParam) : TSActionCondition(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun test(): Boolean {
        // 检查当前DEFCON是否为2
        return gameMode.defcon == 2
    }

}
