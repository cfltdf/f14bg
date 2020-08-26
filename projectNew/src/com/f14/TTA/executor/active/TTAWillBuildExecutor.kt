package com.f14.TTA.executor.active

import com.f14.TTA.component.card.CivilCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.TTAConfirmListener
import com.f14.bg.exception.BoardGameException

/**
 * 建造
 * @author 吹风奈奈
 */
class TTAWillBuildExecutor(param: RoundParam, card: TTACard) : TTAWillExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun check() {
        super.check()
        gameMode.game.playerFreeWorkerCheck(player)
    }

    @Throws(BoardGameException::class)
    override fun active() {
        val destCard = (player.getPlayedCard(ability).singleOrNull() ?: return) as CivilCard
        val listener = TTAConfirmListener(gameMode, player, "你是否建造一个 ${destCard.name} ?")
        val res = gameMode.insertListener(listener)
        if (res.getBoolean(player.position)) {
            gameMode.game.playerBuild(player, destCard)
            gameMode.report.playerBuild(player, destCard, 1)
        }
    }

}
