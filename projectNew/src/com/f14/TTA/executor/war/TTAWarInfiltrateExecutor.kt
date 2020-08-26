package com.f14.TTA.executor.war

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.war.InfiltrateListener
import com.f14.bg.common.ParamSet
import com.f14.bg.exception.BoardGameException

/**
 * 秘密行动的战争(侵略)结果处理器

 * @author 吹风奈奈
 */
class TTAWarInfiltrateExecutor(param: RoundParam, card: AttackCard, winner: TTAPlayer, loser: TTAPlayer, advantage: Int) : TTAWarResultExecutor(param, card, winner, loser, advantage) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val l = InfiltrateListener(gameMode, player, card, winner, loser, createWarParam())
        val res = gameMode.insertListener(l)
        val cardId = res.getString("cardId")
        val level: Int
        when (cardId) {
            loser.uncompletedWonder?.id -> {
                level = loser.uncompletedWonder!!.level
                gameMode.game.playerRemoveUncompleteWonder(loser)
            }
            loser.leader?.id -> {
                level = loser.leader!!.level
                gameMode.game.playerRemoveCard(loser, loser.leader!!)
            }
            else -> return
        }
        // 结算战胜方效果
        val warParam = ParamSet()
        warParam["level"] = level
        this.processWinnerEffect(warParam)
        param.checkWillCard(loser)
    }

}
