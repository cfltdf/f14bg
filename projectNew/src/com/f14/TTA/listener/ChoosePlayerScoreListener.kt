package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.bg.exception.BoardGameException

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class ChoosePlayerScoreListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, card: TTACard) : ChoosePlayerListener(gameMode, trigPlayer, card) {

    @Throws(BoardGameException::class)
    override fun choosePlayer(player: TTAPlayer, target: TTAPlayer) = Unit

}
