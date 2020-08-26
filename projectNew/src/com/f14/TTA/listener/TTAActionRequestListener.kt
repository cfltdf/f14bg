package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.TTAGameCmd

abstract class TTAActionRequestListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, protected var showCard: TTACard?, override var actionString: String, protected var msg: String) : TTAInterruptListener(gameMode, trigPlayer) {

    init {
        this.addListeningPlayer(trigPlayer)
    }

    protected abstract fun createParam(): MutableMap<String, Any>

    override fun getMsg(player: TTAPlayer): String {
        return this.msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_ACTION_REQUEST

    override fun onPlayerResponsed(player: TTAPlayer) {
        gameMode.game.playerRequestEnd(player)
    }

    override fun sendStartListenCommand(player: TTAPlayer, receiver: TTAPlayer?) {
        super.sendStartListenCommand(player, receiver)
        val param = this.createParam()
        param["code"] = this.validCode
        gameMode.game.sendPlayerActionRequestResponse(player, this.actionString, this.getMsg(player), showCard, param)
    }

}
