package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.consts.ActionType
import com.f14.TS.consts.Country
import com.f14.TS.consts.SubRegion
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #105-特殊关系 的执行器

 * @author F14eagle
 */
class Custom105Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        // 如美国控制英国
        val country = gameMode.countryManager.getCountry(Country.ENG)
        if (country.controlledPower == SuperPower.USA) {
            val usa = gameMode.game.usaPlayer
            val executor: TSAdjustInfluenceExecutor
            // #21-北大西洋公约组织
            if (gameMode.eventManager.isCardActived(21)) {
                // 且北约已发生，美国在任一西欧国家增加2点影响，并获2VP。
                gameMode.game.adjustVp(-2)

                val initParam = ActionInitParam()
                initParam.listeningPlayer = SuperPower.USA
                initParam.targetPower = SuperPower.USA
                initParam.actionType = ActionType.ADJUST_INFLUENCE
                initParam.num = 2
                initParam.countryNum = 1
                initParam.msg = "请在任一西欧国家增加2点影响力!"
                initParam.trigType = this.initParam.trigType
                val c = TSCountryCondition()
                c.subRegion = SubRegion.WEST_EUROPE
                initParam.addWc(c)

                executor = TSAdjustInfluenceExecutor(usa, gameMode, initParam)
            } else {
                // 而北约未发生，美国在任意英国邻国增加1点影响；
                val initParam = ActionInitParam()
                initParam.listeningPlayer = SuperPower.USA
                initParam.targetPower = SuperPower.USA
                initParam.actionType = ActionType.ADJUST_INFLUENCE
                initParam.num = 1
                initParam.msg = "请在英国的邻国分配 {num} 点影响力!"
                initParam.trigType = this.initParam.trigType
                val c = TSCountryCondition()
                c.adjacentTo = Country.ENG
                initParam.addWc(c)

                executor = TSAdjustInfluenceExecutor(usa, gameMode, initParam)
            }
            executor.execute()
        }
    }

}
