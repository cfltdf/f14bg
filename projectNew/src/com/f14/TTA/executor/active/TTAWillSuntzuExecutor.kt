package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException

/**
 * 孙子的处理器
 * @author 吹风奈奈
 */
class TTAWillSuntzuExecutor(param: RoundParam, card: TTACard) : TTAWillExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        if (player.leader != null) {
            val destCard = player.government!!
            gameMode.game.playerAttachCard(player, card, destCard)
            this.actived = true
        }
    }

}
