package com.f14.bg.listener

/**
 * 监听器类型
 * @author F14eagle
 */
enum class ListenerType {
    /**
     * 正常监听器
     */
    NORMAL,
    /**
     * 中断型监听器
     */
    INTERRUPT,
    /**
     * 等待执行的监听器
     */
    BLOCK
}
