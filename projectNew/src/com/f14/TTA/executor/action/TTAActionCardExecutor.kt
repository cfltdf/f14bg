package com.f14.TTA.executor.action

import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.executor.TTAActionExecutor

/**
 * 行动牌处理器

 * @author 吹风奈奈
 */
abstract class TTAActionCardExecutor(param: RoundParam, var card: ActionCard) : TTAActionExecutor(param) {
    var completed = false

}
