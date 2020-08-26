package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.PactCard
import com.f14.bg.exception.BoardGameException

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class ChoosePlayerPactListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, card: PactCard) : ChoosePlayerListener(gameMode, trigPlayer, card) {

    @Throws(BoardGameException::class)
    override fun choosePlayer(player: TTAPlayer, target: TTAPlayer) = Unit
}
