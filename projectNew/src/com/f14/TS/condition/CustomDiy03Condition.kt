package com.f14.TS.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.Country
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ConditionInitParam
import com.f14.bg.exception.BoardGameException

/**
 * 五月风暴

 * @author F14eagle
 */
class CustomDiy03Condition(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ConditionInitParam) : TSActionCondition(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun test(): Boolean {
        // 检查苏联是否控制法国
        val country = gameMode.countryManager.getCountry(Country.FRA)
        return country.controlledPower == SuperPower.USSR
    }

}
