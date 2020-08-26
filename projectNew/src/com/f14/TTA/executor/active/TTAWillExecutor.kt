package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam

/**
 * 遗言的能力的处理器

 * @author 吹风奈奈
 */
abstract class TTAWillExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    override fun afterActived() {
        if (ability.isUseActionPoint) {
            param.useActionPoint(ability.actionType, ability.actionCost)
        }
        param.gameMode.report.playerActiveCard(player, card)
    }
}
