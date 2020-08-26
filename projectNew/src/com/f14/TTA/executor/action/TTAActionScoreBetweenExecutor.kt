package com.f14.TTA.executor.action

import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTARequestSelectListener
import com.f14.bg.exception.BoardGameException

/**
 * 提示2选1的行动牌处理器

 * @author 吹风奈奈
 */
class TTAActionScoreBetweenExecutor(param: RoundParam, card: ActionCard) : TTAActionCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val listener = TTARequestSelectListener(gameMode, player, card, TTACmdString.REQUEST_SELECT, "食物,资源", "请选择食物或资源")
        val res = gameMode.insertListener(listener)
        when (res.getInteger("sel")) {
            0 -> TTAActionInstantScoreExecutor(param, card, card.actionAbility.property).execute()
            1 -> TTAActionInstantScoreExecutor(param, card, card.actionAbility.property2).execute()
            -1 -> return
        }
        this.completed = true
    }

}
