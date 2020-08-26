package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.component.condition.TSCountryConditionGroup
import com.f14.TS.consts.Region
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #78-争取进步联盟 的执行器

 * @author F14eagle
 */
class Custom78Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        // 美国每控制一个中美洲或者南美洲的战场国就+1VP
        val player = gameMode.game.usaPlayer

        val conditions = TSCountryConditionGroup()
        var c = TSCountryCondition()
        c.region = Region.CENTRAL_AMERICA
        c.battleField = true
        c.controlledPower = SuperPower.USA
        conditions.wcs.add(c)
        c = TSCountryCondition()
        c.region = Region.SOUTH_AMERICA
        c.battleField = true
        c.controlledPower = SuperPower.USA
        conditions.wcs.add(c)

        val countries = gameMode.countryManager.getCountriesByCondition(conditions)
        val vp = countries.size
        gameMode.game.adjustVp(player, vp)
    }

}
