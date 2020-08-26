package com.f14.TS

import com.f14.TS.component.TSCard
import com.f14.TS.executor.TSListenerExecutor
import com.f14.TS.listener.TakeCardListener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import java.util.*

class TakeCardExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam, private var cards: MutableCollection<TSCard>) : TSListenerExecutor(trigPlayer, gameMode, initParam) {


    var selectedCards: MutableCollection<TSCard> = ArrayList(0)

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = this.initiativePlayer!!
        val l = TakeCardListener(trigPlayer, gameMode, initParam, cards)
        val param = gameMode.insertListener(l)
        val confirmString = param.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val selectedCard = param.get<TSCard>("card")!!
            cards.remove(selectedCard)
            selectedCards.add(selectedCard)
            gameMode.report.playerGetCard(player, selectedCard)
            for (c in cards) {
                gameMode.cardManager.addCardToPlayingDeck(BgUtils.toList(c), c.phase)
            }
        }
    }

}
