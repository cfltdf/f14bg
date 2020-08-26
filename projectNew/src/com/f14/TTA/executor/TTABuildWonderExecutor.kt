package com.f14.TTA.executor

import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.CivilizationProperty
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils


/**
 * 建造奇迹的处理器
 * @author 吹风奈奈
 */
class TTABuildWonderExecutor(param: RoundParam, override val card: WonderCard) : TTABuildExecutor(param, card) {
    var buildStep: Int = 0

    @Throws(BoardGameException::class)
    override fun check() {
        player.uncompletedWonder ?: throw BoardGameException("你没有在建的奇迹!")
        val wonder = card
        val leftStep = wonder.costResources.size - wonder.currentStep

        // 取得建造步骤
        if (buildStep == 0) buildStep = param.checkBuildStep(leftStep)
        buildStep = maxOf(1, minOf(buildStep, leftStep))
        cp += param.getResourceCost(wonder, buildStep, costModify)
        // 检查付了资源后,是否有足够的蓝色指示物用于奇迹的建造
        val retNum = player.getReturnedBlues(cp.cost)
        CheckUtils.check(player.tokenPool.availableBlues + retNum <= 0, "你没有足够的蓝色标志物用于建造奇迹!")

        super.check()
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!this.checked) this.check()

        // 执行消耗
        gameMode.game.playerAddPoint(player, cp.cost, -1)
        gameMode.report.playerCostPoint(player, cp.cost, "花费")
        // 调用建造奇迹主函数
        gameMode.game.playerBuildWonder(player, buildStep)
        if (card.abilities.any { it.abilityType == CivilAbilityType.PA_RETURN_COST }) {
            val num = cp.cost.getProperty(CivilizationProperty.RESOURCE)
            gameMode.game.playerAddSciencePoint(player, num)
            gameMode.report.playerAddSciencePoint(player, num)
        }
        card.abilities.singleOrNull { it.abilityType == CivilAbilityType.PA_RED_CROSS }?.let { a ->
            val res = gameMode.game.playerAddPoint(player, a.property, buildStep)
            gameMode.report.playerAddPoint(player, res)
        }

        super.execute()
    }

    override fun sendReport() {
        if (cached) {
            gameMode.report.playerBuildWonderCache(player, card, buildStep)
        } else {
            gameMode.report.playerBuildWonder(player, actionType, actionCost, card, buildStep)
        }

    }
}
