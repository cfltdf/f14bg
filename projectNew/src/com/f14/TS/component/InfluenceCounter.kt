package com.f14.TS.component

import com.f14.TS.consts.SuperPower
import com.f14.bg.component.PropertyCounter


/**
 * 影响力计数器

 * @author F14eagle
 */
class InfluenceCounter : PropertyCounter<SuperPower>() {
    init {
        this.init()
    }


    @Throws(CloneNotSupportedException::class)
    override fun clone() = super.clone() as InfluenceCounter

    /**
     * 初始化
     */
    private fun init() {
        this.setMinValue(SuperPower.USSR, 0)
        this.setMinValue(SuperPower.USA, 0)
    }
}
