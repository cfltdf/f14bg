package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException

/**
 * 立即获得分数/资源/指示物的处理器

 * @author 吹风奈奈
 */
class TTAActiveInstantScoreTokenExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        gameMode.game.playerAddPoint(player, ability.property)
        gameMode.game.playerAddToken(player, ability.property)
        gameMode.game.playerAddAction(player, ability.property)
        this.actived = true
    }

}
