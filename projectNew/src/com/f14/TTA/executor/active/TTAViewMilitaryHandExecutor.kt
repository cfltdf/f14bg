package com.f14.TTA.executor.active

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ActiveAbilityType
import com.f14.TTA.listener.ChoosePlayerScoreListener
import com.f14.TTA.listener.ViewCardListener
import com.f14.bg.exception.BoardGameException

class TTAViewMilitaryHandExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {
    override fun active() {
        val listener = ChoosePlayerScoreListener(gameMode, player, card)
        val res = gameMode.insertListener(listener)
        val target = res.get<TTAPlayer>("target")
        if (target != null) {
            if (target === player.params.getParameter<TTAPlayer>(ActiveAbilityType.VIEW_MILITARY_HAND)) throw BoardGameException("不能连续两次查看同一名玩家")
            player.roundTempParam[ActiveAbilityType.VIEW_MILITARY_HAND] = target
            val cards = target.militaryHands.cards
            val l = ViewCardListener(gameMode, player, cards)
            gameMode.insertListener(l)
            this.actived = true
        }
    }

}
