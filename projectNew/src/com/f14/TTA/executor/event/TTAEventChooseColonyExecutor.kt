package com.f14.TTA.executor.event

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.listener.InterruptParam


/**
 * 失去殖民地的事件牌能力处理器

 * @author 吹风奈奈
 */
class TTAEventChooseColonyExecutor(param: RoundParam, ability: EventAbility) : TTAAlternateAbilityExecutor(param, ability) {

    override fun processPlayer(result: InterruptParam, p: TTAPlayer) {
        val card = result.get<TTACard>("card") ?: return
        gameMode.game.playerRemoveCard(p, card)
    }

}
