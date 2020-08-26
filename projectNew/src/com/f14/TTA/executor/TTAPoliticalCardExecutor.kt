package com.f14.TTA.executor

import com.f14.TTA.component.param.RoundParam


/**
 * 政治行动打出牌的处理器

 * @author 吹风奈奈
 */
abstract class TTAPoliticalCardExecutor(param: RoundParam) : TTAActionExecutor(param) {
    var finished: Boolean = false
        private set

    init {
        this.finished = false
    }

    protected fun finish() {
        this.finished = true
    }
}
