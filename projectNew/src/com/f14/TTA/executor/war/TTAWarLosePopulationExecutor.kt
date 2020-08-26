package com.f14.TTA.executor.war

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.param.PopParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.war.LosePopulationWarListener
import com.f14.bg.common.ParamSet
import com.f14.bg.exception.BoardGameException

/**
 * 失去人口的战争(侵略)结果处理器

 * @author 吹风奈奈
 */
class TTAWarLosePopulationExecutor(param: RoundParam, card: AttackCard, winner: TTAPlayer, loser: TTAPlayer, advantage: Int) : TTAWarResultExecutor(param, card, winner, loser, advantage) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val l = LosePopulationWarListener(gameMode, player, card, winner, loser, createWarParam())
        val res = gameMode.insertListener(l)
        val pp = res.get<PopParam>(loser.position)
        if (pp != null && pp.selectedPopulation > 0) {
            if (pp.loseFirst != 0) {
                gameMode.game.playerDecreasePopulation(loser, pp.loseFirst)
            }
            gameMode.game.playerDecreasePopulation(loser, pp.detail)
            gameMode.report.playerDestroy(loser, pp.detail)
            gameMode.report.playerDecreasePopulation(loser, pp.selectedPopulation)

            // 结算战胜方效果
            val warParam = ParamSet()
            warParam["decrease_pop"] = pp.selectedPopulation
            this.processWinnerEffect(warParam)
        }

    }

}
