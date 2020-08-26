package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException


/**
 * #100-战争游戏的监听器

 * @author F14eagle
 */
class Custom100Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        this.setPlayerResponsed(action.getPlayer<TSPlayer>())
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }


    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_100

}
