package com.f14.TTA.executor.event

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.CivilCard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.listener.InterruptParam


/**
 * 建造的事件牌能力处理器
 * @author 吹风奈奈
 */
class TTAEventBuildExecutor(param: RoundParam, ability: EventAbility) : TTAAlternateAbilityExecutor(param, ability) {

    override fun processPlayer(result: InterruptParam, p: TTAPlayer) {
        val card = result.get<CivilCard>(p.position) ?: return
        gameMode.game.playerBuild(p, card)
        gameMode.report.playerBuild(p, card, 1)
    }

}
