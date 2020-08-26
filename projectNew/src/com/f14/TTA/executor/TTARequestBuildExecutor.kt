package com.f14.TTA.executor

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.TTA.listener.TTARequestSelectCardListener
import com.f14.bg.exception.BoardGameException


/**
 * 提示建造的处理器
 * @author 吹风奈奈
 */
class TTARequestBuildExecutor(param: RoundParam) : TTAActionExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        do {
            val listener = TTARequestSelectCardListener(gameMode, player, null, TTACmdString.ACTION_BUILD, "请选择要建造的建筑,部队或奇迹!")
            val res = gameMode.insertListener(listener)
            if (TTACmdString.ACTION_BUILD != res.getString("subact")) {
                break
            }
            val card = res.get<TTACard>("card") ?: return
            // 暴动警告
            if (param.checkAlert(card)) {
                TTAExecutorFactory.createBuildExecutor(param, card).execute()
            }
        } while (true)
    }

}
