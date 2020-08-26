package com.f14.TS.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSProperty
import com.f14.TS.listener.initParam.ConditionInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #85-星球大战 的判断条件

 * @author F14eagle
 */
class Custom85Condition(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ConditionInitParam) : TSActionCondition(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun test(): Boolean {
        // 检查美国玩家的太空竞赛是否领先
        val usa = gameMode.game.usaPlayer
        val ussr = gameMode.game.ussrPlayer

        return usa.getProperty(TSProperty.SPACE_RACE) > ussr.getProperty(TSProperty.SPACE_RACE)
    }

}
