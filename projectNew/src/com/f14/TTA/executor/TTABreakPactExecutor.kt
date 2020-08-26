package com.f14.TTA.executor

import com.f14.TTA.component.card.PactCard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException


/**
 * 中止条约处理器

 * @author 吹风奈奈
 */
class TTABreakPactExecutor(param: RoundParam, private var card: PactCard) : TTAActionExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        gameMode.game.removeOvertimeCard(card)
        gameMode.report.playerBreakPact(player, card)
        gameMode.game.playerRequestEnd(player)
    }

}
