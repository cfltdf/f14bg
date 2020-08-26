package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.AdjustParam
import com.f14.TS.component.TSCountry
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TrigType
import com.f14.TS.factory.InitParamFactory
import com.f14.TS.listener.TSAddInfluenceListener
import com.f14.TS.listener.TSOpActionListener
import com.f14.TS.listener.TSRealignmentListener
import com.f14.TS.listener.TSRealignmentListener.RealignmentParam
import com.f14.TS.listener.initParam.OPActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class TSOpActionExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, private var param: OPActionInitParam) : TSExecutor(trigPlayer, gameMode) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = gameMode.game.getPlayer(param.listeningPlayer)!!
        val card = param.card ?: return
        var op = param.num
        if (op == 0) {
            op = card.op
        }
        var done = false
        do {
            val l = TSOpActionListener(trigPlayer, gameMode, param)
            var res = gameMode.insertListener(l)
            var confirmString = res.getString("confirmString")
            if (ConfirmString.PASS == confirmString) {
                break
            } else {
                when (res.getString("action")) {
                    TSCmdString.ACTION_ADD_INFLUENCE -> {
                        val ip = InitParamFactory.createAddInfluenceParam(player, card, op, TrigType.ACTION, param.isFreeAction, param.conditionGroup)
                        val il = TSAddInfluenceListener(trigPlayer, gameMode, ip)
                        res = gameMode.insertListener(il)
                        confirmString = res.getString("confirmString")
                        if (ConfirmString.CONFIRM == confirmString) {
                            val adjustParams = res.get<Collection<AdjustParam>>("adjustParams")!!
                            val originInfluence = res.get<Collection<TSCountry>>("originInfluence")!!
                            // 输出战报信息
                            gameMode.report.playerDoAction(player, adjustParams)
                            // 确定影响力调整
                            adjustParams.forEach(AdjustParam::apply)
                            // 向所有玩家发送影响力的调整结果
                            gameMode.game.sendCountriesInfo(originInfluence, null)
                            done = true
                        }
                    }
                    TSCmdString.ACTION_REALIGNMENT -> {
                        var rp: RealignmentParam? = null
                        val rip = InitParamFactory.createRealignmentParam(player, card, op, TrigType.ACTION, param.isFreeAction, param.conditionGroup)
                        do {
                            val rl = TSRealignmentListener(trigPlayer, gameMode, rip)
                            rl.setRealignmentParam(rp)
                            res = gameMode.insertListener(rl)
                            confirmString = res.getString("confirmString")
                            if (ConfirmString.CONFIRM == confirmString) {
                                rp = res.get<RealignmentParam>("realignmentParam")!!
                                try {
                                    rp.realignment()
                                    // 输出战报信息
                                    gameMode.report.playerDoAction(player, rp.adjustParam!!)
                                    // 确定影响力调整
                                    rp.applyRealignment()
                                    // 向所有玩家发送影响力的调整结果
                                    gameMode.game.sendCountryInfo(rp.adjustParam!!.orgCountry, null)
                                } catch (e: BoardGameException) {
                                    trigPlayer.sendException(gameMode.game.room.id, e)
                                }
                            } else {
                                break
                            }
                        } while (rp == null || rp.leftOP > 0)
                        if (rp != null && rp.usedNum != 0) done = true
                    }
                    TSCmdString.ACTION_COUP -> {
                        val cip = InitParamFactory.createCoupParam(gameMode, player, card, op, TrigType.ACTION, param.isFreeAction, param.conditionGroup)
                        val executor = TSCoupExecutor(trigPlayer, gameMode, cip)
                        executor.execute()
                        if (executor.confirm) done = true
                    }
                }
            }
        } while (!done)
    }

}
