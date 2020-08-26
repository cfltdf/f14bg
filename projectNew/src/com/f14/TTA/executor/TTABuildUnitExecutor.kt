package com.f14.TTA.executor

import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ActionType

/**
 * 建造部队的处理器
 * @author 吹风奈奈
 */
class TTABuildUnitExecutor(param: RoundParam, card: TechCard) : TTABuildCivilExecutor(param, card) {

    init {
        // 建造部队用的是军事行动点
        actionType = ActionType.MILITARY
    }
}
