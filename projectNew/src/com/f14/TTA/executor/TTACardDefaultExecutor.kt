package com.f14.TTA.executor

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.TTA.listener.TTARequestSelectListener

class TTACardDefaultExecutor(param: RoundParam, val card: TTACard) : TTAActionExecutor(param) {
    override fun execute() {
        when (card.cardType) {
            CardType.PRODUCTION, CardType.BUILDING, CardType.UNIT -> this.buildOrUpgradeOrDestroy()
            CardType.WONDER -> this.buildWonder()
            else -> return
        }
    }

    private fun buildWonder() {
        TTAExecutorFactory.createBuildExecutor(param, card).execute()

    }

    private fun buildOrUpgradeOrDestroy() {
        val listener = TTARequestSelectListener(gameMode, player, card, TTACmdString.REQUEST_SELECT, "建造,升级,摧毁", "请选择要进行的动作")
        val res = gameMode.insertListener(listener)
        val executor: TTAActionExecutor = when (res.getInteger("sel")) {
            0 -> TTAExecutorFactory.createBuildExecutor(param, card)
            1 -> TTARequestUpgradeExecutor(param, card)
            2 -> TTADestroyExecutor(param, card as TechCard)
            else -> return
        }
        executor.execute()
    }
}
