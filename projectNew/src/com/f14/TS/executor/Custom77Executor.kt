package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.listener.Custom77Listener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class Custom77Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = this.initiativePlayer!!
        val l = Custom77Listener(trigPlayer, gameMode, initParam)
        val param = gameMode.insertListener(l)
        val confirmString = param.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val cards = param.get<List<TSCard>>("cards")
            if (cards != null) {

                // 弃掉几张牌,就能摸几张牌
                val drawNum = cards.size

                gameMode.game.playerRemoveHands(player, cards)
                gameMode.report.playerDiscardCards(player, cards)
                gameMode.game.discardCards(cards)
                gameMode.game.playerDrawCard(player, drawNum)
            }
        }
    }

}
