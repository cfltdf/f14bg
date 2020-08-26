package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.active.ActiveGetLeaderListener
import com.f14.bg.exception.BoardGameException

/**
 * 拿取领袖并替换的处理器
 * @author 吹风奈奈
 */
class TTAActiveGetLeaderExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val l = ActiveGetLeaderListener(param, card)
        l.addListeningPlayer(param.player)
        val res = gameMode.insertListener(l)
        val cardId = res.get<String>("cardId")
        if (cardId != null) {
            val cb = gameMode.cardBoard
            val index = cb.getCardIndex(cardId)
            val newcard = cb.getCard(cardId)
            player.checkTakeCard(newcard)
            val oldCard = player.leader
            gameMode.game.playerTakeCard(player, newcard, index)
            gameMode.report.playerTakeCard(player, 0, index, newcard)
            gameMode.game.playerAddCard(player, newcard)
            gameMode.report.playerRemoveCardCache(player, oldCard!!)
            gameMode.report.playerAddCardCache(player, newcard)
            this.actived = true
        }
    }

}
