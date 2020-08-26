package com.f14.TTA.executor

import com.f14.TTA.component.param.CostParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.RoundStep
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.manager.TTAConstManager
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils


/**
 * 扩张人口的处理器

 * @author 吹风奈奈
 */
class TTAIncreasePopExecutor(param: RoundParam) : TTAActionExecutor(param) {
    var actionType: ActionType = ActionType.CIVIL
    var actionCost: Int = 0
    var costModify: Int = 0
    var cp: CostParam
    var cached: Boolean = false

    init {
        actionCost = TTAConstManager.getActionCost(player, TTACmdString.ACTION_POPULATION)
        cp = CostParam()
        cached = false
    }

    @Throws(BoardGameException::class)
    override fun check() {
        // 检查玩家是否有足够的内政行动点
        player.checkActionPoint(actionType, actionCost)
        CheckUtils.check(player.tokenPool.availableWorkers <= 0, "你已经没有可用的人口了!")
        val foodCost = TTAConstManager.getPopulationCost(player, costModify)
        cp.cost.addProperty(CivilizationProperty.FOOD, foodCost)
        if (foodCost > 0 && param.currentStep == RoundStep.NORMAL) {
            gameMode.game.checkTemplateResource(player, null, cp, CivilizationProperty.FOOD)
        }
        player.checkEnoughResource(cp.cost)
        super.check()
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!this.checked) {
            this.check()
        }
        // 玩家消耗食物,消耗内政行动点,扩张人口
        param.useActionPoint(actionType, actionCost)
        gameMode.game.playerIncreasePopulation(player, 1)
        // 执行消耗
        gameMode.game.playerAddPoint(player, cp.cost, -1)
        gameMode.report.playerCostPoint(player, cp.cost, "花费")
        gameMode.game.executeTemplateResource(player, cp)
        if (!cached) {
            gameMode.report.playerIncreasePopulation(player, actionCost, 1)
        } else {
            gameMode.report.playerIncreasePopulationCache(player, 1)
        }
    }

}
