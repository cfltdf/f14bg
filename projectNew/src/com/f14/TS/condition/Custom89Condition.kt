package com.f14.TS.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.Country
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ConditionInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #89-苏联击落 KAL-007 的判断条件

 * @author F14eagle
 */
class Custom89Condition(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ConditionInitParam) : TSActionCondition(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun test(): Boolean {
        // 检查美国是否控制南韩
        val country = gameMode.countryManager.getCountry(Country.KOR)
        return country.controlledPower == SuperPower.USA
    }

}
