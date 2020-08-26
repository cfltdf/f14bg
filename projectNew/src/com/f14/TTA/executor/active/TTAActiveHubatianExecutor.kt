package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException

/**
 * 翔霸天的处理器

 * @author 吹风奈奈
 */
class TTAActiveHubatianExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val leader = player.leader ?: return
        gameMode.game.playerRemoveCardDirect(player, leader)
        gameMode.report.playerRemoveCardCache(player, leader)
        this.actived = true
    }

}
