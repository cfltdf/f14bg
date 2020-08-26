package com.f14.TTA.consts

/**
 * TTA行动类型

 * @author F14eagle
 */
enum class ActionType {

    /**
     * 政治行动
     */
    CIVIL,

    /**
     * 军事行动
     */
    MILITARY;

    /**
     * 取得中文描述

     * @return
     */
    val chinese: String
        get() {
            return when (this) {
                CIVIL -> "内政"
                MILITARY -> "军事"
            }
        }
}
