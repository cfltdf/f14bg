package com.f14.TS.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.consts.Region
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ConditionInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #108-我们在伊朗有人 的判断条件

 * @author F14eagle
 */
class Custom108Condition(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ConditionInitParam) : TSActionCondition(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun test(): Boolean {
        // 美国控制至少1个中东国家
        val condition = TSCountryCondition()
        condition.controlledPower = SuperPower.USA
        condition.region = Region.MIDDLE_EAST
        return gameMode.countryManager.getCountriesByCondition(condition).isNotEmpty()
    }

}
