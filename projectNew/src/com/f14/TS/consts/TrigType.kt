package com.f14.TS.consts


/**
 * 触发类型

 * @author F14eagle
 */
enum class TrigType {
    /**
     * 触发类型 - 行动操作
     */
    ACTION,
    /**
     * 触发类型 - 发生事件
     */
    EVENT;

    val chinese
        get() = when (this) {
            ACTION -> "行动"
            EVENT -> "事件"
        }
}
