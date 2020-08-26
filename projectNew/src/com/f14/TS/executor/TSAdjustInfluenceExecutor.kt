package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.AdjustParam
import com.f14.TS.component.TSCountry
import com.f14.TS.listener.TSAdjustInfluenceListener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class TSAdjustInfluenceExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, private val initParam: ActionInitParam) : TSExecutor(trigPlayer, gameMode) {
    var adjustNum = 0

    @Throws(BoardGameException::class)
    override fun execute() {
        val l = TSAdjustInfluenceListener(trigPlayer, gameMode, initParam)
        val player = gameMode.game.getPlayer(initParam.listeningPlayer)!!
        val res = gameMode.insertListener(l)
        val confirmString = res.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val adjustParams = res.get<Collection<AdjustParam>>("adjustParams")!!
            val originInfluence = res.get<Collection<TSCountry>>("originInfluence")!!
            // 输出战报信息
            gameMode.report.playerDoAction(player, adjustParams)
            // 确定影响力调整
            adjustParams.forEach(AdjustParam::apply)
            // 向所有玩家发送影响力的调整结果
            gameMode.game.sendCountriesInfo(originInfluence, null)

            this.adjustNum = res.getInteger("adjustNum")
        }
    }

}
