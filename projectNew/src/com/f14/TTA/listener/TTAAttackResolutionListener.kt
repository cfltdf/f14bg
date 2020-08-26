package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.bg.common.ParamSet

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

abstract class TTAAttackResolutionListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, protected var attackCard: AttackCard, protected var winner: TTAPlayer, protected var loser: TTAPlayer, protected var warParam: ParamSet) : TTAInterruptListener(gameMode, trigPlayer) {

    init {
        if (this.attackCard.loserEffect.isWinnerSelect) {
            this.addListeningPlayer(winner)
        } else {
            this.addListeningPlayer(loser)
        }
    }
}
