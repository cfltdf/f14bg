package com.f14.TTA.executor.event

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.executor.TTAActionExecutor
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam

/**
 * 新版文明发展的处理器

 * @author 吹风奈奈
 */
class TTAEventDevOfCivExecutor(param: RoundParam, ability: EventAbility) : TTAAlternateAbilityExecutor(param, ability) {

    override fun createEventListener(players: Collection<TTAPlayer>): TTAEventListener? {
        val l = super.createEventListener(players)!!
        for (p in players) {
            val param = this.param.listener.getParam<RoundParam>(p.position)
            l.setParam(p.position, param)
        }
        return l
    }

    override fun processPlayer(result: InterruptParam, p: TTAPlayer) {
        val executor = result.get<TTAActionExecutor>(p.position) ?: return
        try {
            executor.execute()
        } catch (e: BoardGameException) {
            e.printStackTrace()
        }
    }

}
