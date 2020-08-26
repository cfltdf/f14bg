package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.listener.TSChoiceListener
import com.f14.TS.listener.initParam.ChoiceInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class TSChoiceExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, private var initParam: ChoiceInitParam) : TSExecutor(trigPlayer, gameMode) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val card = initParam.card!!
        val player = gameMode.game.getPlayer(initParam.listeningPlayer)!!
        val l = TSChoiceListener(trigPlayer, gameMode, initParam)
        val res = gameMode.insertListener(l)
        when (res.getString("confirmString")) {
            ConfirmString.CONFIRM -> {
                // 执行所选的能力
                val abilities = when (res.getInteger("choice")) {
                    1 -> {
                        gameMode.report.playerSelectChoice(player, card.abilityGroup!!.descr1)
                        card.abilityGroup!!.abilitiesGroup1
                    }
                    2 -> {
                        gameMode.report.playerSelectChoice(player, card.abilityGroup!!.descr2)
                        card.abilityGroup!!.abilitiesGroup2
                    }
                    else -> return
                }
                // 处理所选择的能力,被插入的监听器为本监听器中断的监听器
                // 执行的玩家为targetPower
                val target = gameMode.game.getPlayer(initParam.targetPower)!!
                gameMode.game.processAbilities(abilities, target, card)
            }
        }
    }

}
