package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.listener.Custom108Listener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException
import com.f14.utils.CollectionUtils

class Custom108Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = this.initiativePlayer!!
        // 从牌堆中抽5张牌
        val drawnCards = gameMode.cardManager.playingDeck.draw(5).toMutableList()
        gameMode.report.playerDrawCards(player, drawnCards)
        val l = Custom108Listener(trigPlayer, gameMode, initParam, drawnCards)
        val param = gameMode.insertListener(l)
        val confirmString = param.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val cards = param.get<List<TSCard>>("cards")
            if (cards != null) {
                drawnCards.removeAll(cards)
                gameMode.report.playerDiscardCards(player, cards)
                gameMode.game.discardCards(cards)
            }
        }
        // 其余的牌加入到牌堆后重洗
        gameMode.cardManager.playingDeck.addCards(drawnCards)
        CollectionUtils.shuffle(gameMode.cardManager.playingDeck.cards)
        gameMode.report.info("重洗了牌堆")
    }

}
