package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.consts.ActionType
import com.f14.TS.consts.SubRegion
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.TS.utils.TSRoll

/**
 * Created by 吹风奈奈 on 2017/8/8.
 */
class CustomDiy04Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {
    override fun execute() {
        // 从苏联手上随机抽出一张牌
        val ussr = gameMode.game.ussrPlayer
        val d = TSRoll.roll()
        gameMode.report.playerRoll(ussr, d, 0)
        when (d) {
            1, 2 -> gameMode.game.adjustVp(ussr, 3)
            3, 4 -> {
                gameMode.game.adjustVp(ussr, 2)
                val initParam = ActionInitParam()
                initParam.listeningPlayer = SuperPower.USSR
                initParam.targetPower = SuperPower.USSR
                initParam.actionType = ActionType.ADJUST_INFLUENCE
                initParam.num = 1
                initParam.countryNum = 1
                initParam.msg = "请在任一东欧国家增加1点影响力!"
                initParam.trigType = this.initParam.trigType
                val c = TSCountryCondition()
                c.subRegion = SubRegion.EAST_EUROPE
                initParam.addWc(c)

                val executor = TSAdjustInfluenceExecutor(ussr, gameMode, initParam)
                executor.execute()
            }
            5, 6 -> {
                gameMode.game.adjustVp(ussr, 1)
                gameMode.game.adjustDefcon(-1)
            }
        }
    }
}