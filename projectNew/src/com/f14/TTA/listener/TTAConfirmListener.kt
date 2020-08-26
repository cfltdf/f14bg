package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.param.ConfirmParam
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

class TTAConfirmListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, private val msg: String) : TTAInterruptListener(gameMode, trigPlayer) {

    init {
        this.addListeningPlayer(trigPlayer)
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建选择参数
        for (player in gameMode.realPlayers) {
            val param = ConfirmParam()
            this.setParam(player.position, param)
        }
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        for (player in this.listeningPlayers) {
            val cp = this.getParam<ConfirmParam>(player)
            param[player.position] = cp.confirm
        }
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirm = action.getAsBoolean("confirm")
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<ConfirmParam>(player)
        param.confirm = confirm
        this.setPlayerResponsed(player)
    }

    override fun getMsg(player: TTAPlayer) = msg

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_BUILD
}
