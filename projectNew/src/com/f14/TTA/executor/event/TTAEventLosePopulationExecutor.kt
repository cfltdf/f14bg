package com.f14.TTA.executor.event

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.param.PopParam
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.listener.InterruptParam


/**
 * 失去人口的事件牌能力处理器

 * @author 吹风奈奈
 */
class TTAEventLosePopulationExecutor(param: RoundParam, ability: EventAbility) : TTAAlternateAbilityExecutor(param, ability) {

    override fun processPlayer(result: InterruptParam, p: TTAPlayer) {
        val pp = result.get<PopParam>(p.position)
        if (pp != null && pp.selectedPopulation > 0) {
            if (pp.loseFirst != 0) {
                gameMode.game.playerDecreasePopulation(p, pp.loseFirst)
            }
            if (pp.detail.isNotEmpty()) {
                gameMode.game.playerDecreasePopulation(p, pp.detail)
                gameMode.report.playerDestroy(p, pp.detail)
            }
            gameMode.report.playerDecreasePopulation(p, pp.selectedPopulation)
        }
    }

}
