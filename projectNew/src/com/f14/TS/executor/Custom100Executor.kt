package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSVictoryType
import com.f14.TS.listener.Custom100Listener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

/**
 * #100-战争游戏 的执行器

 * @author F14eagle
 */
class Custom100Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = this.initiativePlayer!!
        val l = Custom100Listener(trigPlayer, gameMode, initParam)
        val param = gameMode.insertListener(l)
        val confirmString = param.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            // 给对方6VP,然后结束游戏
            gameMode.game.adjustVp(player, -6)

            when {
                gameMode.vp > 0 -> // 正分则苏联获胜
                    gameMode.game.playerWin(gameMode.game.ussrPlayer, TSVictoryType.VP_VICTORY)
                gameMode.vp < 0 -> // 负分则美国获胜
                    gameMode.game.playerWin(gameMode.game.usaPlayer, TSVictoryType.VP_VICTORY)
                else -> // 0为平局
                    gameMode.game.playerWin(null, TSVictoryType.VP_VICTORY)
            }
        }

    }

}
