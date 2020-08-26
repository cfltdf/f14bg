package com.f14.TTA.executor

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.CopyTacticsListener
import com.f14.TTA.manager.TTAConstManager
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils


/**
 * 学习阵型的处理器

 * @author 吹风奈奈
 */
class TTACopyTacticsExecutor(param: RoundParam) : TTAActionExecutor(param) {
    var actionType: ActionType = ActionType.MILITARY
    var actionCost: Int = 0

    init {
        actionCost = TTAConstManager.getActionCost(player, TTACmdString.REQUEST_COPY_TAC)
    }

    @Throws(BoardGameException::class)
    override fun check() {
        CheckUtils.check(gameMode.cardBoard.publicTacticsDeck.empty, "没有可以学习的阵型!")
        CheckUtils.check(player.getAvailableActionPoint(actionType) < actionCost, "你没有足够的军事行动点数!")
        param.checkChangeTactic()
        super.check()
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!this.checked) {
            this.check()
        }
        val al = CopyTacticsListener(gameMode, player)
        val res = gameMode.insertListener(al)
        val card = res.get<TTACard>("card")
        if (card != null) {
            param.useActionPoint(actionType, actionCost)
            param.afterPlayCard(card)
            gameMode.game.playerAddCardDirect(player, card)
            gameMode.report.playerCopyTatics(player, actionType, actionCost, card)
        }
    }

}
