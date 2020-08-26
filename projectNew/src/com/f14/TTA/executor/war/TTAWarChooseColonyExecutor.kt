package com.f14.TTA.executor.war

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.war.ChooseColonyWarListener
import com.f14.bg.exception.BoardGameException

/**
 * 夺取殖民地的战争(侵略)结果处理器

 * @author 吹风奈奈
 */
class TTAWarChooseColonyExecutor(param: RoundParam, card: AttackCard, winner: TTAPlayer, loser: TTAPlayer, advantage: Int) : TTAWarResultExecutor(param, card, winner, loser, advantage) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val l = ChooseColonyWarListener(gameMode, player, card, winner, loser, createWarParam())
        val res = gameMode.insertListener(l)
        val card = res.get<TTACard>("card")
        if (card != null) {
            // 结算战败方和战胜方效果
            gameMode.game.playerRemoveCard(loser, card)
            gameMode.game.playerAddCardDirect(winner, card)
            gameMode.report.playerAddCard(winner, card)
        }
    }

}
