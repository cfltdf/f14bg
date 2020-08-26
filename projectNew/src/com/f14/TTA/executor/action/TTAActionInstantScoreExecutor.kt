package com.f14.TTA.executor.action

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException

/**
 * 立即得分的行动牌处理器

 * @author 吹风奈奈
 */
class TTAActionInstantScoreExecutor(param: RoundParam, card: ActionCard, private var property: TTAProperty) : TTAActionCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        param.checkActionCardEnhance(property)
        gameMode.game.playerAddPoint(player, property)
        this.completed = true
    }

}
