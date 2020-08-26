package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCountry
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TrigType
import com.f14.TS.factory.InitParamFactory
import com.f14.TS.listener.TSCountryActionListener
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.consts.ConfirmString

/**
 * Created by 吹风奈奈 on 2017/8/8.
 */
class CustomDiy06Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {
    override fun execute() {
        val card = initParam.card
        val player = gameMode.game.usaPlayer
        val param = InitParamFactory.createActionInitParam(player, card, TrigType.EVENT)
        val con = TSCountryCondition()
        con.hasUsaInfluence = true
        param.addBc(con)
        param.countryNum = 1
        param.msg = "选择一个没有美国影响力的国家，移除2点苏联影响力并增加1点美国影响力！"
        val l = TSCountryActionListener(trigPlayer, gameMode, param)
        val res = gameMode.insertListener(l)
        val confirmString = res.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val countries = res.get<Collection<TSCountry>>("countries")!!
            // 对所选的国家遍历并执行行动
            for (country in countries) {
                gameMode.game.adjustInfluence(country, SuperPower.USSR, -2)
                gameMode.game.adjustInfluence(country, SuperPower.USA, 1)
            }
        }
        gameMode.game.playerAdjustMilitaryAction(player, 2)
    }
}