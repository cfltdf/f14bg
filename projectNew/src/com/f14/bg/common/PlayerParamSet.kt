package com.f14.bg.common

/**
 * 玩家的参数集
 * @author F14eagle
 */
class PlayerParamSet : ParamSet() {
    var responsed: Boolean by param
    var needResponse: Boolean by param

    init {
        responsed = false
        needResponse = false
    }
}
