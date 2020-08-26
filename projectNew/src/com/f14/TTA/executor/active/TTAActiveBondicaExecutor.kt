package com.f14.TTA.executor.active

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTARequestSelectListener
import com.f14.bg.exception.BoardGameException

class TTAActiveBondicaExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val listener = TTARequestSelectListener(gameMode, player, card, TTACmdString.REQUEST_SELECT, "食物,资源,科技", "请选择!")
        val res = gameMode.insertListener(listener)
        val property = TTAProperty()
        val sel = res.getInteger("sel")
        val prop = arrayOf(CivilizationProperty.FOOD, CivilizationProperty.RESOURCE, CivilizationProperty.SCIENCE).getOrNull(sel)
                ?: return
        property.addProperty(prop, 1)
        gameMode.game.playerAddPoint(player, property)
        this.actived = true
    }
}