package com.f14.TS.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ConditionInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #76-乌苏里江冲突 的判断条件

 * @author F14eagle
 */
class Custom76Condition(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ConditionInitParam) : TSActionCondition(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun test(): Boolean {
        // 美国是否拥有中国牌
        return gameMode.cardManager.chinaOwner == SuperPower.USA
    }

}
