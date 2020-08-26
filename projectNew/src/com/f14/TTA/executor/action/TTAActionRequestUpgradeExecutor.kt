package com.f14.TTA.executor.action

import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.executor.TTAUpgradeExecutor
import com.f14.TTA.listener.TTARequestSelectCardListener
import com.f14.bg.exception.BoardGameException

/**
 * 提示升级的行动牌处理器

 * @author 吹风奈奈
 */
class TTAActionRequestUpgradeExecutor(param: RoundParam, card: ActionCard) : TTAActionCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        var listener = TTARequestSelectCardListener(gameMode, player, card, TTACmdString.REQUEST_UPGRADE_TO, "请选择要升级的建筑或部队!")
        listener.condition = card.actionAbility
        var res = gameMode.insertListener(listener)
        var subact = res.getString("subact")
        if (TTACmdString.REQUEST_UPGRADE_TO == subact) {
            val fromCard = res.get<TTACard>("card")!!
            param.checkUpgrade(fromCard)
            listener = TTARequestSelectCardListener(gameMode, player, fromCard, TTACmdString.ACTION_UPGRADE, "请选择要升级成的建筑或部队!")
            listener.condition = card.actionAbility
            res = gameMode.insertListener(listener)
            subact = res.getString("subact")
            if (TTACmdString.ACTION_UPGRADE == subact) {
                val toCard = res.get<TTACard>("card")
                param.checkActionCardEnhance(card)
                val executor = TTAUpgradeExecutor(param, fromCard as TechCard, toCard as TechCard)
                executor.actionCost = 0
                executor.costModify = card.actionAbility.property.getProperty(CivilizationProperty.RESOURCE)
                executor.cached = true
                executor.execute()
                this.completed = true
            }
        }
    }

}
