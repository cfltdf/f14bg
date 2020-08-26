package com.f14.TTA.executor.action

import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.executor.TTABuildWonderExecutor
import com.f14.bg.exception.BoardGameException

/**
 * 建造奇迹的行动牌处理器

 * @author 吹风奈奈
 */
class TTAActionBuildWonderExecutor(param: RoundParam, card: ActionCard) : TTAActionCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        param.checkActionCardEnhance(card)
        val executor = TTABuildWonderExecutor(param, param.player.uncompletedWonder!!)
        executor.costModify = card.actionAbility.property.getProperty(CivilizationProperty.RESOURCE)
        executor.buildStep = 1
        executor.cached = true
        executor.actionCost = 0
        executor.execute()
        this.completed = true
    }

}
