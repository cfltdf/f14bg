package com.f14.bg.component

/**
 * Created by 吹风奈奈 on 2017/7/24.
 */
interface IConditionGroup<in P, C : ICondition<P>> : ICondition<P> {
    /**
     * 白名单条件
     */
    val wcs: MutableList<C>
    /**
     * 黑名单条件
     */
    val bcs: MutableList<C>

    /**
     * 清除所有条件
     */
    fun clear() {
        this.wcs.clear()
        this.bcs.clear()
    }

    override fun test(obj: P): Boolean {
        // 白黑名单中的条件均为 "或" 关系
        val black = bcs.isEmpty() or bcs.none { it test obj }
        val white = wcs.isEmpty() or wcs.any { it test obj }
        return black and white
    }

}
