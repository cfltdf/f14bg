package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.CostParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.listener.active.ActiveIncreaesUnitListener
import com.f14.TTA.manager.TTAConstManager
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils

/**
 * 扩张人口并建造部队的处理器
 * @author 吹风奈奈
 */
class TTAActiveIncreaesUnitExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {
    private val cp: CostParam = CostParam()

    @Throws(BoardGameException::class)
    override fun active() {
        CheckUtils.check(player.tokenPool.availableWorkers <= 0, "你已经没有可用的人口了!")
        val l = ActiveIncreaesUnitListener(param, card)
        l.addListeningPlayer(param.player)
        val res = gameMode.insertListener(l)
        val buildCard = res.get<TechCard>(player.position)
        if (buildCard != null) {
            val foodCost = TTAConstManager.getPopulationCost(player)
            val resourceCost = player.getBuildResourceCost(buildCard)
            gameMode.game.playerRequestEnd(player)
            cp.cost.addProperty(CivilizationProperty.FOOD, foodCost)
            cp.cost.addProperty(CivilizationProperty.RESOURCE, resourceCost)
            cp.cost.addProperties(ability.property)
            gameMode.game.checkTemplateResource(player, buildCard, cp, CivilizationProperty.FOOD)
            gameMode.game.checkTemplateResource(player, buildCard, cp, CivilizationProperty.RESOURCE)
            player.checkEnoughResource(cp.cost)
            // 执行消耗
            gameMode.game.playerAddPoint(player, cp.cost, -1)
            gameMode.report.playerCostPoint(player, cp.cost, "花费")
            gameMode.game.executeTemplateResource(player, cp)
            gameMode.game.playerIncreasePopulation(player, 1)
            gameMode.report.playerIncreasePopulationCache(player, 1)
            gameMode.game.playerBuild(player, buildCard)
            gameMode.report.playerBuildCache(player, buildCard, 1)

            this.actived = true
        }
    }

}
