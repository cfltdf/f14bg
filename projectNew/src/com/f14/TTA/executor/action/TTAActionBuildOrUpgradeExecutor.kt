package com.f14.TTA.executor.action

import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTARequestSelectListener
import com.f14.bg.exception.BoardGameException

/**
 * 建造或升级的行动牌的处理器

 * @author 吹风奈奈
 */
class TTAActionBuildOrUpgradeExecutor(param: RoundParam, card: ActionCard) : TTAActionCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val listener = TTARequestSelectListener(gameMode, player, card, TTACmdString.REQUEST_SELECT, "建造,升级", "请选择要进行的动作")
        val res = gameMode.insertListener(listener)
        val sel = res.getInteger("sel")
        var executor: TTAActionCardExecutor? = null
        when (sel) {
            0 -> executor = TTAActionRequestBuildExecutor(param, card)
            1 -> executor = TTAActionRequestUpgradeExecutor(param, card)
            -1 -> return
        }
        executor!!.execute()
        this.completed = executor.completed
    }

}
