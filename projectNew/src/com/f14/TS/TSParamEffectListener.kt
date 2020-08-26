package com.f14.TS

import com.f14.TS.action.ActionParam
import com.f14.TS.component.TSCard
import com.f14.TS.factory.GameActionFactory
import com.f14.TS.listener.TSActionListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException


class TSParamEffectListener(gameMode: TSGameMode, private val player: TSPlayer, private val card: TSCard, private val ap: ActionParam) : TSActionListener(gameMode) {

    override fun beforeListeningCheck(player: TSPlayer): Boolean {
        return false
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        val ga = GameActionFactory.createGameAction(gameMode, player, card, ap)
        gameMode.game.executeAction(ga)
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
    }

    override val validCode: Int
        get() = 0

}
