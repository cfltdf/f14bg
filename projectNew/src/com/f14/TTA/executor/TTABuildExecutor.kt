package com.f14.TTA.executor

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.CostParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.manager.TTAConstManager
import com.f14.bg.exception.BoardGameException


/**
 * 建造的处理器
 * @author 吹风奈奈
 */
open class TTABuildExecutor(param: RoundParam, protected open val card: TTACard) : TTAActionExecutor(param) {
    val cp: CostParam = CostParam()
    var needWorker: Boolean = false
    var actionType: ActionType = ActionType.CIVIL
    var actionCost: Int = TTAConstManager.getActionCost(player, TTACmdString.ACTION_BUILD)
    var costModify = 0
    var cached = false

    @Throws(BoardGameException::class)
    override fun check() {
        // 检查玩家是否拥有行动点数
        player.checkActionPoint(actionType, actionCost)
        if (this.needWorker) {
            // 检查玩家是否拥有空闲人口
            gameMode.game.playerFreeWorkerCheck(player)
        }
        // 检测资源是否足够
        player.checkEnoughResource(cp.cost)
        super.check()
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!this.checked) this.check()
        // 扣除行动点
        param.useActionPoint(actionType, actionCost)
        // 调整临时资源的值
        gameMode.game.executeTemplateResource(player, cp)

        // 完成建造后触发的方法
        param.afterBuild(card)
        // 发送战报
        this.sendReport()

    }

    protected open fun sendReport() {
        if (this.cached) {
            gameMode.report.playerBuildCache(player, card, 1)
        } else {
            gameMode.report.playerBuild(player, actionType, actionCost, card, 1)
        }
    }

}
