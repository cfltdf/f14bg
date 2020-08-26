package com.f14.TTA.executor

import com.f14.TTA.component.card.GovermentCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTARequestSelectListener
import com.f14.bg.exception.BoardGameException


/**
 * 选择更变政府方式的处理器
 * @author 吹风奈奈
 */
class TTARequestChangeGovernmentExecutor(param: RoundParam, card: GovermentCard) : TTAChangeGovermentExecutor(param, card, 0) {

    @Throws(BoardGameException::class)
    override fun check() {

    }

    @Throws(BoardGameException::class)
    override fun execute() {
        val listener = TTARequestSelectListener(gameMode, player, card, TTACmdString.REQUEST_SELECT, "革命,和平演变", "请选择要更换政府的方式")
        val res = gameMode.insertListener(listener)
        when (val revolution = res.getInteger("sel")) {
            0, 1 -> {
                this.revolution = revolution
                super.check()
                super.execute()
            }
            else -> {
            }
        }
    }
}
