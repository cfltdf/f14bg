package com.f14.tichu.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer
import com.f14.tichu.consts.TichuGameCmd

class TichuConfirmExchangeListener(gameMode: TichuGameMode) : TichuActionListener(gameMode) {

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TichuPlayer>()
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = TichuGameCmd.GAME_CODE_CONFIRM_EXCHANGE

    override fun onPlayerStartListen(player: TichuPlayer) {
        super.onPlayerStartListen(player)
        val res = this.createSubactResponse(player, "loadParam")
        val cards = gameMode.exchangeParam.getPlayerCards(player)
        for ((o, card) in cards) {
            res.public("card" + o.position, card.id)
        }
        gameMode.game.sendResponse(player, res)
    }

}
