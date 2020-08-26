package com.f14.bg.common


import java.util.*

/**
 * 简单的参数集合
 * @author F14eagle
 */
open class ParamSet {
    protected var param: MutableMap<Any, Any> = HashMap()

    /**
     * 清除所有参数
     */
    fun clear() = this.param.clear()

    /**
     * 取得参数
     * @param key
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <C> get(key: Any) = param[key] as? C

    /**
     * 取得Boolean类型的参数
     * @param key
     * @return
     */
    fun getBoolean(key: Any) = get<Boolean>(key) ?: false

    /**
     * 取得Double类型的参数
     * @param key
     * @return
     */
    fun getDouble(key: Any) = get<Double>(key) ?: Double.NaN

    /**
     * 取得Integer类型的参数
     * @param key
     * @return
     */
    fun getInteger(key: Any) = get<Int>(key) ?: 0

    /**
     * 取得String类型的参数
     * @param key
     * @return
     */
    fun getString(key: Any) = get<String>(key) ?: ""

    /**
     * 判断参数集是否为空
     * @return
     */
    val isEmpty: Boolean
        get() = param.isEmpty()

    /**
     * 设置参数
     * @param key
     * @param value
     * @return
     */
    operator fun set(key: Any, value: Any?) {
        when (value) {
            null -> param.remove(key)
            else -> param[key] = value
        }
    }

}
