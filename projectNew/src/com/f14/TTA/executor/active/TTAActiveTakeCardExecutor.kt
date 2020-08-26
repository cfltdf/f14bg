package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.active.ActiveGetLeaderListener
import com.f14.bg.exception.BoardGameException

/**
 * 从卡排列移除卡牌的处理器
 * @author 吹风奈奈
 */
class TTAActiveTakeCardExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val l = ActiveGetLeaderListener(param, card)
        l.addListeningPlayer(param.player)
        val res = gameMode.insertListener(l)
        val cardId = res.get<String>("cardId")
        if (cardId != null) {
            val cb = gameMode.cardBoard
            val index = cb.getCardIndex(cardId)
            card = cb.getCard(cardId)
            gameMode.game.playerRemoveBoardCard(player, card, index)

            this.actived = true
        }
    }


}
