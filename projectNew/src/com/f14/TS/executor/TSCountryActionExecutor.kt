package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.action.ActionParam
import com.f14.TS.component.TSCountry
import com.f14.TS.factory.GameActionFactory
import com.f14.TS.listener.TSCountryActionListener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class TSCountryActionExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, private var initParam: ActionInitParam, private var actionParam: ActionParam) : TSExecutor(trigPlayer, gameMode) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val card = initParam.card
        val player = gameMode.game.getPlayer(initParam.listeningPlayer)!!
        val l = TSCountryActionListener(trigPlayer, gameMode, initParam)
        val res = gameMode.insertListener(l)
        val confirmString = res.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val countries = res.get<Collection<TSCountry>>("countries")!!
            // 对所选的国家遍历并执行行动
            for (country in countries) {
                actionParam.country = country.country
                val ga = GameActionFactory.createGameAction(gameMode, player, card, actionParam)
                gameMode.game.executeAction(ga)
            }
        }
    }

}
