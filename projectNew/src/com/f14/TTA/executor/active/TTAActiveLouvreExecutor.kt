package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException

class TTAActiveLouvreExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {
    override fun check() {
        val wonder = card as WonderCard
        val blues = wonder.blues
        if (blues == 0) throw BoardGameException("这张牌上已经没有蓝点了!")
        super.check()
    }

    override fun active() {
        val wonder = card as WonderCard
        val blues = wonder.blues
        if (blues > 0) {
            wonder.addBlues(-1)
            player.tokenPool.addAvailableBlues(1)
            gameMode.game.sendPlayerCardToken(player, wonder, null)
            gameMode.game.playerAddPoint(player, ability.property)
            this.actived = true
        }
    }

}
