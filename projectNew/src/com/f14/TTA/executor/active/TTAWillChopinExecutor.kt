package com.f14.TTA.executor.active

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.listener.ChoosePlayerScoreListener
import com.f14.bg.exception.BoardGameException

/**
 * 肖邦的处理器
 * @author 吹风奈奈
 */
class TTAWillChopinExecutor(param: RoundParam, card: TTACard) : TTAWillExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val l = ChoosePlayerScoreListener(gameMode, player, card)
        l.addListeningPlayer(player)
        val res = gameMode.insertListener(l)
        val target = res.get<TTAPlayer>("target")
        if (target != null) {
            val num = target.getProperty(CivilizationProperty.CULTURE)
            gameMode.game.playerAddCulturePoint(player, num)
            gameMode.report.playerAddCulturePoint(player, num)
            this.actived = true
        }
    }

}
