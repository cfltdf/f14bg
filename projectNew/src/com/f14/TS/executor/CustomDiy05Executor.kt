package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCountry
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.component.condition.TSCountryConditionGroup
import com.f14.TS.consts.TrigType
import com.f14.TS.factory.InitParamFactory
import com.f14.TS.listener.TSCountryActionListener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString

/**
 * Created by 吹风奈奈 on 2017/8/8.
 */
class CustomDiy05Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {
    override fun execute() {
        val player = this.initiativePlayer!!
        val param = initParam.clone()
        param.num = 1
        param.msg = "请选择一个你控制的非欧洲国家!"
        val l = TSCountryActionListener(trigPlayer, gameMode, initParam)
        val res = gameMode.insertListener(l)
        val confirmString = res.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val country = res.get<Collection<TSCountry>>("countries")?.single()!!
            val coupgroup = TSCountryConditionGroup()
            val adjCon = TSCountryCondition()
            adjCon.adjacentTo = country.country
            coupgroup.wcs.add(adjCon)
            val cp = InitParamFactory.createCoupParam(gameMode, player, card, card.op, TrigType.ACTION, true, coupgroup)
            cp.msg = "请选择 ${country.name} 的一个邻国!"
            val executor = TSCoupExecutor(player, gameMode, cp)
            executor.execute()
            val target = executor.targetCountry ?: return
            if (target.controlledPower == player.superPower) {
                gameMode.game.adjustVp(player, 1)
            } else {
                gameMode.game.adjustInfluence(country, player.superPower, -1)
            }
        }
    }
}