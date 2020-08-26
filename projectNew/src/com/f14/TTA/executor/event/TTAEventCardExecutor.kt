package com.f14.TTA.executor.event

import com.f14.TTA.component.card.MilitaryCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.executor.TTAActionExecutor

/**
 * 事件牌的处理器

 * @author 吹风奈奈
 */
abstract class TTAEventCardExecutor(param: RoundParam, protected val card: MilitaryCard) : TTAActionExecutor(param)
