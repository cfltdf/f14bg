package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.bg.action.BgAction
import java.util.*

class TTAShowCardListener(gameMode: TTAGameMode, player: TTAPlayer, card: TTACard) : TTAActionRequestListener(gameMode, player, card, "PLAY_CARD", "") {

    override fun doAction(action: BgAction.GameAction) {
        this.setPlayerResponsed(action.getPlayer<TTAPlayer>())
    }

    override fun createParam(): MutableMap<String, Any> {
        return HashMap()
    }

}
