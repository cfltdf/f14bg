package com.f14.loveletter

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ActionListener
import com.f14.bg.listener.ListenerType
import com.f14.bg.player.Player
import com.f14.bg.report.BgReport


class LLShowCardListener(gameMode: LLGameMode, private var player: LLPlayer, private var targetPlayer: LLPlayer) : ActionListener<LLPlayer, LLConfig, BgReport<LLPlayer>>(gameMode, ListenerType.INTERRUPT) {

    init {
        this.addListeningPlayer(player)
    }


    override fun createInterruptParam() = super.createInterruptParam().also {
        it["validCode"] = this.validCode
        it["position"] = player.position
        it["targetPosition"] = targetPlayer.position
    }

    override fun createStartListenCommand(player: LLPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        res.public("actionString", this.actionString)
        res.public("cardId", targetPlayer.firstCard!!.id)
        res.public("showConfirmButton", true)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        this.setPlayerResponsed(action.getPlayer<Player>().position)
    }


    private val actionString: String
        get() = ""

    override val validCode: Int
        get() = LLGameCmd.GAME_CODE_CONFIRM_EXCHANGE

}
