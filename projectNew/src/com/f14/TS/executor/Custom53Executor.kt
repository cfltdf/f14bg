package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.listener.Custom53Listener
import com.f14.TS.listener.Custom53Listener.InfluenceParam
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class Custom53Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = this.initiativePlayer!!
        val l = Custom53Listener(trigPlayer, gameMode, initParam)
        val param = gameMode.insertListener(l)
        val confirmString = param.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val influenceParam = param.get<InfluenceParam>("influenceParam")!!
            // 输出战报信息
            gameMode.report.playerDoAction(player, influenceParam.getAdjustParams())
            // 确定影响力调整
            influenceParam.applyAdjust()
            // 向所有玩家发送影响力的调整结果
            gameMode.game.sendCountriesInfo(influenceParam.originInfluence, null)
        }
    }

}
