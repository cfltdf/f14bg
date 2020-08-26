package com.f14.TTA.executor.war

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.ChooseTakeHandListener

class TTAWarChooseCardExecutor(param: RoundParam, card: AttackCard, winner: TTAPlayer, loser: TTAPlayer, advantage: Int) : TTAWarResultExecutor(param, card, winner, loser, advantage) {
    override fun execute() {
        val l = ChooseTakeHandListener(param, loser.civilHands)
        val res = gameMode.insertListener(l)
        val card = res.get<TTACard>("card")
        if (card != null) {
            // 结算战败方和战胜方效果
            gameMode.game.playerRemoveHand(loser, card)
            gameMode.report.playerRemoveHand(loser, card)
            gameMode.game.playerAddHand(winner, card)
            gameMode.report.playerAddHand(winner, card)
        }
    }

}
