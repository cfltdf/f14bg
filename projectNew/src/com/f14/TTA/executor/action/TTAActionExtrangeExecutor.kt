package com.f14.TTA.executor.action

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.param.CostParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTARequestSelectListener
import com.f14.bg.exception.BoardGameException

/**
 * 交换资源的行动牌处理器

 * @author 吹风奈奈
 */
class TTAActionExtrangeExecutor(param: RoundParam, card: ActionCard) : TTAActionCardExecutor(param, card) {
    init {
        this.completed = false
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        val listener = TTARequestSelectListener(gameMode, player, card, TTACmdString.REQUEST_SELECT, "资源换食物,食物换资源", "请选择交换方式")
        val res = gameMode.insertListener(listener)
        val cp = CostParam()
        val property: TTAProperty = when (res.getInteger("sel")) {
            0 -> {
                cp.cost.addProperty(CivilizationProperty.RESOURCE, 2)
                gameMode.game.checkTemplateResource(player, card, cp, CivilizationProperty.RESOURCE)
                card.actionAbility.property
            }
            1 -> {
                cp.cost.addProperty(CivilizationProperty.FOOD, 2)
                gameMode.game.checkTemplateResource(player, card, cp, CivilizationProperty.FOOD)
                card.actionAbility.property2
            }
            else -> return
        }
        player.checkEnoughResource(cp.cost)
        // 执行消耗
        gameMode.game.playerAddPoint(player, cp.cost, -1)
        gameMode.report.playerCostPoint(player, cp.cost, "花费")
        TTAActionInstantScoreExecutor(param, card, property).execute()
        this.completed = true
    }

}
