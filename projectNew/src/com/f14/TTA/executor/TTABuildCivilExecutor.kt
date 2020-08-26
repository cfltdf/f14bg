package com.f14.TTA.executor

import com.f14.TTA.component.card.CivilCard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CardType
import com.f14.bg.exception.BoardGameException


/**
 * 建造内政的处理器
 * @author 吹风奈奈
 */

open class TTABuildCivilExecutor(param: RoundParam, override val card: TechCard) : TTABuildExecutor(param, card) {
    init {
        needWorker = true
    }

    @Throws(BoardGameException::class)
    override fun check() {
        if (card.cardType == CardType.BUILDING) {
            // 只有建筑才会受建筑数量的限制
            gameMode.game.playerBuildLimitCheck(player, card)
        }
        cp += param.getResourceCost(card, costModify)
        super.check()
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!checked) this.check()

        // 执行消耗
        gameMode.game.playerAddPoint(player, cp.cost, -1)
        gameMode.report.playerCostPoint(player, cp.cost, "花费")
        // 调用建造主函数
        gameMode.game.playerBuild(player, card as CivilCard)

        super.execute()
    }
}
