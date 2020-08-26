package com.f14.TTA.executor.active

import com.f14.TTA.TTAResourceManager
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.TTAConfirmListener
import com.f14.bg.exception.BoardGameException

/**
 * 建造
 * @author 吹风奈奈
 */
class TTAWillGetWonderExecutor(param: RoundParam, card: TTACard) : TTAWillExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun check() {
        super.check()
    }

    @Throws(BoardGameException::class)
    override fun active() {
        val destCard = gameMode.game.getResourceManager<TTAResourceManager>().getCardByNo("441.0")?.firstOrNull()
                ?: return
        if (destCard is WonderCard) {
            val listener = TTAConfirmListener(gameMode, player, "你是否获得 ${destCard.reportString} ?")
            val res = gameMode.insertListener(listener)
            if (res.getBoolean(player.position)) {
                gameMode.game.playerAddCardDirect(player, destCard)
                gameMode.report.playerAddCardCache(player, destCard)
                this.actived = true
            }
        }
    }

}
