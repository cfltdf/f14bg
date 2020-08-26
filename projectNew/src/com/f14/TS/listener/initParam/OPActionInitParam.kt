package com.f14.TS.listener.initParam

/**
 * OP行动的初始化参数

 * @author F14eagle
 */
open class OPActionInitParam : ActionInitParam() {
    var canAddInfluence: Boolean = false
    var canCoup: Boolean = false
    var canRealignment: Boolean = false
    var isFreeAction: Boolean = false
}
