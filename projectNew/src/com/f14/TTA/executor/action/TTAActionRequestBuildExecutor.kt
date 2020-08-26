package com.f14.TTA.executor.action

import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.TTA.listener.TTARequestSelectCardListener
import com.f14.bg.exception.BoardGameException

/**
 * 提示建造的行动牌处理器

 * @author 吹风奈奈
 */
class TTAActionRequestBuildExecutor(param: RoundParam, card: ActionCard) : TTAActionCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val listener = TTARequestSelectCardListener(gameMode, player, card, TTACmdString.ACTION_BUILD, "请选择要建造的建筑,部队或奇迹!")
        listener.condition = card.actionAbility
        val res = gameMode.insertListener(listener)
        val subact = res.getString("subact")
        if (TTACmdString.ACTION_BUILD == subact) {
            val buildCard = res.get<TTACard>("card")!!
            param.checkActionCardEnhance(card)
            val executor = TTAExecutorFactory.createBuildExecutor(param, buildCard)
            executor.actionCost = 0
            executor.costModify = card.actionAbility.property.getProperty(CivilizationProperty.RESOURCE)
            executor.cached = true
            executor.check()
            executor.execute()
            this.completed = true
        }
    }

}
