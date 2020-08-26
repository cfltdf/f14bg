package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCountry
import com.f14.TS.listener.Custom40Listener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class Custom40Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    var removed: Boolean = false

    @Throws(BoardGameException::class)
    override fun execute() {
        val card = gameMode.cardManager.getCardByCardNo(40)!!
        val l = Custom40Listener(trigPlayer, gameMode, initParam)
        val param = gameMode.insertListener(l)
        val confirmString = param.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val country = param.get<TSCountry>("country")!!
            // 从该国家移除2点影响力
            gameMode.game.adjustInfluence(country, trigPlayer.superPower, -2)
            // 移除#40-古巴导弹危机的卡牌效果
            gameMode.game.playerRemoveActivedCard(trigPlayer, card)
            this.removed = true
        }
    }

}
