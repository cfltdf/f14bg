package com.f14.TTA.executor

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTARequestSelectCardListener
import com.f14.bg.exception.BoardGameException


/**
 * 提示升级的处理器

 * @author 吹风奈奈
 */
class TTARequestUpgradeExecutor(param: RoundParam, var fromCard: TTACard? = null) : TTAActionExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val isLoop = fromCard == null
        if (fromCard == null) {
            val listener = TTARequestSelectCardListener(gameMode, player, null, TTACmdString.REQUEST_UPGRADE_TO, "请选择要升级的建筑或部队!")
            val res = gameMode.insertListener(listener)
            if (TTACmdString.REQUEST_UPGRADE_TO != res.getString("subact")) {
                return
            }
            fromCard = res.get<TTACard>("card") ?: return
        }
        do {
            val listener = TTARequestSelectCardListener(gameMode, player, fromCard, TTACmdString.ACTION_UPGRADE, "请选择要升级成的建筑或部队!")
            val res = gameMode.insertListener(listener)
            if (TTACmdString.ACTION_UPGRADE != res.getString("subact")) {
                break
            }
            val toCard = res.get<TTACard>("card") ?: return
            TTAUpgradeExecutor(param, fromCard as TechCard, toCard as TechCard).execute()
        } while (isLoop)
    }

}
