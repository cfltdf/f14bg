package com.f14.TS.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ConditionInitParam
import com.f14.bg.exception.BoardGameException

class KitchenCondition(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ConditionInitParam) : TSActionCondition(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun test(): Boolean {
        // 检查美国控制的战场国是否比苏联多
        val c = TSCountryCondition()
        c.battleField = true
        c.controlledPower = SuperPower.USA
        val usa = gameMode.countryManager.getCountriesByCondition(c).size

        c.controlledPower = SuperPower.USSR
        val ussr = gameMode.countryManager.getCountriesByCondition(c).size

        return usa > ussr
    }

}
