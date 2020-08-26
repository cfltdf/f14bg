package com.f14.TTA.executor

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTARequestSelectCardListener
import com.f14.bg.exception.BoardGameException


/**
 * 提示摧毁的处理器

 * @author 吹风奈奈
 */
class TTARequestDestroyExecutor(param: RoundParam) : TTAActionExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        do {
            val listener = TTARequestSelectCardListener(gameMode, player, null, TTACmdString.ACTION_DESTORY, "请选择要摧毁的建筑或部队!")
            val res = gameMode.insertListener(listener)

            if (TTACmdString.ACTION_DESTORY != res.getString("subact")) {
                break
            }
            val card = res.get<TTACard>("card") ?: return
            TTADestroyExecutor(param, card as TechCard).execute()
        } while (true)
    }

}
