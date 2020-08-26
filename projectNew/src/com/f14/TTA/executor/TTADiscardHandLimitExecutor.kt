package com.f14.TTA.executor

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.DiscardMilitaryLimitListener
import com.f14.bg.exception.BoardGameException

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class TTADiscardHandLimitExecutor(param: RoundParam) : TTAActionExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        while (player.militaryHands.size > player.militaryHandLimit) {
            // 如果军事牌超出手牌上限则需弃牌
            val param = gameMode.insertListener(DiscardMilitaryLimitListener(gameMode, player))
            val cards = param.get<List<TTACard>>("discards")!!
            gameMode.game.playerDiscardHand(player, cards)
        }
    }

}
