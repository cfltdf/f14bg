package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.component.ability.TSAbility
import com.f14.TS.listener.TSCardActionListener
import com.f14.TS.listener.initParam.CardActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class TSCardActionExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, private var initParam: CardActionInitParam) : TSExecutor(trigPlayer, gameMode) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val card = initParam.card
        val player = gameMode.game.getPlayer(initParam.listeningPlayer)!!
        val l = TSCardActionListener(trigPlayer, gameMode, initParam)
        val res = gameMode.insertListener(l)
        val confirmString = res.getString("confirmString")
        val `as`: List<TSAbility>
        when (confirmString) {
            ConfirmString.CONFIRM -> {
                val c = res.get<TSCard>("card")
                if (c != null) {
                    // 所选的卡牌将被弃掉
                    gameMode.game.playerPlayCard(player, c)
                    gameMode.report.playerDiscardCard(player, c)
                    gameMode.game.discardCard(c)

                    // 触发所选卡牌中abilitiesGroup1中的能力
                    `as` = card!!.abilityGroup!!.abilitiesGroup1
                    gameMode.game.processAbilities(`as`, player, c)
                } else {
                    // 如果可以跳过,则会触发所选卡牌中abilitiesGroup2中的能力
                    `as` = card!!.abilityGroup!!.abilitiesGroup2
                    gameMode.game.processAbilities(`as`, player, card)
                }
            }
            else -> {
                // 如果可以跳过,则会触发所选卡牌中abilitiesGroup2中的能力
                `as` = card!!.abilityGroup!!.abilitiesGroup2
                gameMode.game.processAbilities(`as`, player, card)
            }
        }
    }

}
