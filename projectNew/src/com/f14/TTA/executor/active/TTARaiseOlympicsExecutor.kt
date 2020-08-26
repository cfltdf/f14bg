package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam

class TTARaiseOlympicsExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {
    override fun active() {
        gameMode.report.addAction(player, "宣布举办奥运会")
        gameMode.game.playerAddCulturePoint(player, 5)
        gameMode.report.playerAddCulturePoint(player, 5)
        gameMode.olympicsPosition = player.position
        this.actived = true
    }

}
