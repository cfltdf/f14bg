package com.f14.PuertoRico.component

import com.f14.PuertoRico.consts.GoodType
import com.f14.bg.component.PartPool

class PrPartPool : PartPool() {

    /**
     * 取得所有配件的数据,i为修正值,将乘上原始数据
     * @return
     */
    fun getParts(i: Int): Map<String, Any> = this.parts.map { it.toString() to this.getAvailableNum(it) * i }.toMap()

    /**
     * 取得资源的字符串描述
     * @return
     */
    val resourceDescr: String
        get() = GoodType.values()
                .map { it to this.getAvailableNum(it) }
                .filter { it.second > 0 }.joinToString(",") { "${it.second}个${it.first.chinese}" }

    /**
     * 取得所有资源的数据
     * @return
     */
    val resources: Map<String, Any>
        get() = GoodType.values().map { it.toString() to this.getAvailableNum(it) }.toMap()

    /**
     * 取得所有资源的数据,i为修正值,将乘上原始数据
     * @return
     */
    fun getResources(i: Int) = GoodType.values().map { it.toString() to this.getAvailableNum(it) * i }.toMap()

}
