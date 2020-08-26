package com.f14.TTA.executor.active

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTARequestSelectListener
import com.f14.bg.exception.BoardGameException

/**
 * 邓小平的处理器
 * @author 吹风奈奈
 */
class TTAActiveXiaoPingExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val listener = TTARequestSelectListener(gameMode, player, card, TTACmdString.REQUEST_SELECT, "食物,资源,科技,文化", "不管黑猫白猫,捉住老鼠就是好猫!")
        val res = gameMode.insertListener(listener)
        val property = TTAProperty()
        val sel = res.getInteger("sel")
        val prop = arrayOf(CivilizationProperty.FOOD, CivilizationProperty.RESOURCE, CivilizationProperty.SCIENCE, CivilizationProperty.CULTURE).getOrNull(sel)
                ?: return
        property.addProperty(prop, 3)
        gameMode.game.playerAddPoint(player, property)
        this.actived = true
    }

}
