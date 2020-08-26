package com.f14.TTA.executor.event

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.executor.TTAActionExecutor

/**
 * 事件牌能力的处理器

 * @author 吹风奈奈
 */
abstract class TTAEventAbilityExecutor(param: RoundParam, protected var ability: EventAbility) : TTAActionExecutor(param) {

    var trigPlayer: TTAPlayer? = null
}
