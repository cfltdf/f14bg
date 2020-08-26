package com.f14.bg.component

/**
 * 条件接口
 * @author F14eagle
 */
interface ICondition<in P> {

    /**
     * 判断objects是否都符合条件
     * @param objs
     * @return
     */
    infix fun test(objs: Collection<P>) = objs.all { this test it }

    /**
     * 判断object是否符合条件
     * @param `obj`
     * @return
     */
    infix fun test(obj: P): Boolean

}
