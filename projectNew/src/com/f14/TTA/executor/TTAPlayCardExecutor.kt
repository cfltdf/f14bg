package com.f14.TTA.executor

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.manager.TTAConstManager
import com.f14.bg.exception.BoardGameException


/**
 * 打出手牌的处理器
 * @author 吹风奈奈
 */
open class TTAPlayCardExecutor(param: RoundParam, protected open val card: TTACard) : TTAActionExecutor(param) {
    // 设置行动需要的动作类型
    var actionType: ActionType = ActionType.CIVIL
    var actionCost: Int = TTAConstManager.getActionCost(player, TTACmdString.ACTION_PLAY_CARD)
    var cached = false

    @Throws(BoardGameException::class)
    override fun check() {
        // 检查玩家是否有足够的内政行动点
        player.checkActionPoint(actionType, actionCost)
        super.check()
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        // 完成出牌后触发的方法
        param.afterPlayCard(card)
        this.sendReport()
    }

    protected open fun sendReport() {
        if (cached) {
            gameMode.report.playerPlayCardCache(player, card)
        } else {
            gameMode.report.playerPlayCard(player, actionType, actionCost, card)
        }
    }
}
