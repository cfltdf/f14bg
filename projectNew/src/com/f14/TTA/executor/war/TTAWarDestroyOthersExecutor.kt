package com.f14.TTA.executor.war

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.param.PopParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.war.DestroyOthersWarListener
import com.f14.bg.common.ParamSet
import com.f14.bg.exception.BoardGameException

/**
 * 摧毁的战争(侵略)结果处理器

 * @author 吹风奈奈
 */
open class TTAWarDestroyOthersExecutor(param: RoundParam, card: AttackCard, winner: TTAPlayer, loser: TTAPlayer, advantage: Int) : TTAWarResultExecutor(param, card, winner, loser, advantage) {

    protected open fun createListener(): DestroyOthersWarListener {
        return DestroyOthersWarListener(gameMode, player, card, winner, loser, createWarParam())
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        val l = this.createListener()
        val res = gameMode.insertListener(l)
        val pp = res.get<PopParam>(loser.position)
        if (pp != null) {
            val totalCost = pp.detail.keys.sumBy { it.costResource * pp.detail[it]!! }
            gameMode.game.playerDestroy(loser, pp.detail)
            gameMode.report.playerDestroy(loser, pp.detail)
            gameMode.report.printCache(loser)
            // 结算战胜方效果
            val warParam = ParamSet()
            warParam["totalCost"] = totalCost
            this.processWinnerEffect(warParam)
        }
    }

}
