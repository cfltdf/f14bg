package com.f14.TTA.executor

import com.f14.TTA.component.card.MilitaryCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CardType
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.bg.exception.BoardGameException

/**
 * 打出事件牌的处理器

 * @author 吹风奈奈
 */
class TTAPlayEventExecutor(param: RoundParam, private val card: MilitaryCard) : TTAPoliticalCardExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val currCard = gameMode.game.playerAddEvent(player, card)
        TTAExecutorFactory.createEventCardExecutor(param, currCard).execute()
        param.afterPlayCard(card.clone().also { it.cardType = CardType.EVENT })
        this.finish()
    }

}
