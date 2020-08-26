package com.f14.TTA.executor.action

import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.executor.TTAIncreasePopExecutor
import com.f14.bg.exception.BoardGameException

/**
 * 扩张人口的行动牌处理器

 * @author 吹风奈奈
 */
class TTAActionIncreasePopExecutor(param: RoundParam, card: ActionCard) : TTAActionCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val executor = TTAIncreasePopExecutor(param)
        executor.actionCost = 0
        executor.cached = true
        executor.execute()
        param.checkActionCardEnhance(card)
        gameMode.game.playerAddPoint(player, card.actionAbility.property)
        this.completed = true
    }

}
