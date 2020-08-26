package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.active.ActiveHomerListener
import com.f14.bg.exception.BoardGameException

/**
 * 新版荷马的处理器
 * @author 吹风奈奈
 */
class TTAWillHomerExecutor(param: RoundParam, card: TTACard) : TTAWillExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        if (player.leader != null) {
            val l = ActiveHomerListener(param, card)
            l.addListeningPlayer(param.player)
            val res = gameMode.insertListener(l)
            val destCard = res.get<TTACard>("card")
            if (destCard != null) {
                gameMode.game.playerAttachCard(player, card, destCard)
                this.actived = true
            }
        }
    }

}
