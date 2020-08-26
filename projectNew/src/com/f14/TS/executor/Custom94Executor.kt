package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.consts.Region
import com.f14.TS.listener.Custom94Listener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class Custom94Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = this.initiativePlayer!!
        val l = Custom94Listener(trigPlayer, gameMode, initParam)
        val param = gameMode.insertListener(l)
        val confirmString = param.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val region = param.get<Region>("region")
            if (region != null) {
                // 将选择的区域添加到苏联玩家的参数中
                val condition = TSCountryCondition()
                condition.region = region
                val ussr = gameMode.game.ussrPlayer
                ussr.forbiddenCondition = condition
                gameMode.report.action(player, "指定了 " + Region.getChineseDescr(region))
            }
        }
    }

}
