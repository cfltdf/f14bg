package com.f14.F14bgClient.vo

import java.util.Date

/**
 * 系统代码对象

 * @author F14eagle
 */
class CodeDetail {
    var id: Long? = null
    lateinit var codeType: String
    lateinit var label: String
    lateinit var value: String
    var descr: String? = null
    var codeIndex: Int? = null
    var activeInd: Boolean? = null
    var createTime: Date? = null
    var updateTime: Date? = null
}
