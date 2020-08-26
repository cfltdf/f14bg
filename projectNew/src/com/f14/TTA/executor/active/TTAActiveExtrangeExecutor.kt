package com.f14.TTA.executor.active

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.CostParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTARequestSelectListener
import com.f14.bg.exception.BoardGameException

/**
 * 交易处理器

 * @author 吹风奈奈
 */
class TTAActiveExtrangeExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val listener = TTARequestSelectListener(gameMode, player, card, TTACmdString.REQUEST_SELECT, "资源换食物,食物换资源", "请选择交换方式")
        val res = gameMode.insertListener(listener)
        val cp = CostParam()
        val property = TTAProperty()
        when (res.getInteger("sel")) {
            0 -> {
                cp.cost.addProperty(CivilizationProperty.RESOURCE, 1)
                property.addProperty(CivilizationProperty.FOOD, 2)
                gameMode.game.checkTemplateResource(player, card, cp, CivilizationProperty.RESOURCE)
            }
            1 -> {
                cp.cost.addProperty(CivilizationProperty.FOOD, 1)
                property.addProperty(CivilizationProperty.RESOURCE, 2)
                gameMode.game.checkTemplateResource(player, card, cp, CivilizationProperty.FOOD)
            }
            -1 -> return
        }
        player.checkEnoughResource(cp.cost)
        // 执行消耗
        gameMode.game.playerAddPoint(player, cp.cost, -1)
        gameMode.report.playerCostPoint(player, cp.cost, "花费")
        gameMode.game.playerAddPoint(player, property)
        gameMode.report.playerAddPoint(player, property)
        this.actived = true
    }

}
