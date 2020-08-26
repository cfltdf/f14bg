package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.listener.Custom43Listener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class Custom43Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val l = Custom43Listener(trigPlayer, gameMode, initParam)
        val param = gameMode.insertListener(l)
        val confirmString = param.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val card = param.get<TSCard>("selectedCard")
            if (card != null) {
                // 从弃牌堆中移除该牌
                gameMode.game.takeDiscardCard(card)
                // 添加给玩家
                gameMode.game.playerGetCard(this.initiativePlayer!!, card)
            }
        }
    }

}
