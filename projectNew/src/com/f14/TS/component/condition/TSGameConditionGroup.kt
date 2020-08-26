package com.f14.TS.component.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import java.util.*

class TSGameConditionGroup {
    /**
     * 白名单条件
     */
    var wcs: MutableList<TSGameCondition> = ArrayList(0)
    /**
     * 黑名单条件
     */
    var bcs: MutableList<TSGameCondition> = ArrayList(0)

    /**
     * 添加黑条件
     * @param c
     */
    fun addBcs(c: TSGameCondition) {
        this.bcs.add(c)
    }

    /**
     * 添加白条件
     * @param c
     */
    fun addWcs(c: TSGameCondition) {
        this.wcs.add(c)
    }


    fun test(o: TSGameMode, player: TSPlayer): Boolean {
        // 白黑名单中的条件均为 "或" 关系
        val black = this.bcs.isEmpty() || this.bcs.none { it.test(o, player) }
        val white = this.wcs.isEmpty() || this.wcs.any { it.test(o, player) }
        return black and white
    }


}
