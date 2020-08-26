package com.f14.TTA.executor.war

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.listener.war.ChooseScienceListener
import com.f14.bg.common.ParamSet
import com.f14.bg.exception.BoardGameException

/**
 * 夺取科技的战争(侵略)结果处理器
 * @author 吹风奈奈
 */
class TTAWarChooseScienceExecutor(param: RoundParam, card: AttackCard, winner: TTAPlayer, loser: TTAPlayer, advantage: Int) : TTAWarResultExecutor(param, card, winner, loser, advantage) {

    @Throws(BoardGameException::class)
    override fun execute() {
        var sciencePoint = advantage
        while (sciencePoint > 0) {
            val l = ChooseScienceListener(gameMode, player, card, winner, loser, sciencePoint)
            val res = gameMode.insertListener(l)
            val card = res.get<TechCard>(loser.position)
            if (card != null) {
                gameMode.game.playerRemoveCard(loser, card)
                gameMode.game.playerAddCardDirect(winner, card)
                gameMode.report.playerAddCard(winner, card)
                sciencePoint -= card.costScience
            } else {
                break
            }
        }
        if (sciencePoint > 0) {
            val sp = gameMode.game.playerAddSciencePoint(loser, -sciencePoint)
            gameMode.report.playerAddSciencePoint(loser, sp)
            gameMode.report.printCache(loser)
            val property = TTAProperty()
            property.setProperty(CivilizationProperty.SCIENCE, -sp)
            val warParam = ParamSet()
            warParam["property"] = property
            this.processWinnerEffect(warParam)
        }
    }

}
