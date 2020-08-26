package com.f14.TTA.executor.war

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.war.ChooseResourceWarListener
import com.f14.bg.common.ParamSet
import com.f14.bg.exception.BoardGameException

/**
 * 夺取资源的战争(侵略)结果处理器

 * @author 吹风奈奈
 */
class TTAWarChooseResourceExecutor(param: RoundParam, card: AttackCard, winner: TTAPlayer, loser: TTAPlayer, advantage: Int) : TTAWarResultExecutor(param, card, winner, loser, advantage) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val l = ChooseResourceWarListener(gameMode, player, card, winner, loser, createWarParam())
        val res = gameMode.insertListener(l)
        // 结算战败方玩家的效果
        val property = res.get<TTAProperty>(loser.position)
        if (property != null) {
            val resprop = gameMode.game.playerAddPoint(loser, property, -1)
            gameMode.report.playerAddPoint(this.loser, resprop)
            gameMode.report.printCache(this.loser)
            resprop.multi(-1)
            // 结算战胜方玩家的效果
            val warParam = ParamSet()
            warParam["property"] = resprop
            this.processWinnerEffect(warParam)
        }
    }

}
