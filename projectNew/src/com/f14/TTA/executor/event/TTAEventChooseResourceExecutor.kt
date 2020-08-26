package com.f14.TTA.executor.event

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.listener.InterruptParam


/**
 * 得到或失去资源的事件牌能力处理器

 * @author 吹风奈奈
 */
class TTAEventChooseResourceExecutor(param: RoundParam, ability: EventAbility) : TTAAlternateAbilityExecutor(param, ability) {

    override fun processPlayer(result: InterruptParam, p: TTAPlayer) {
        val property = result.get<TTAProperty>(p.position) ?: return
        gameMode.game.playerAddPoint(p, property)
    }

}
