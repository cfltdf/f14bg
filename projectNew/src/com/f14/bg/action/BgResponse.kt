package com.f14.bg.action

import com.f14.bg.BGConst
import com.google.gson.GsonBuilder

/**
 * 回应
 * @author F14eagle
 */
class BgResponse(
        /**
         * 指令类型
         */
        val type: Int = BGConst.INT_NULL,
        /**
         * 指令代码
         */
        val code: Int = BGConst.INT_NULL,
        /**
         * 位置
         */
        val position: Int = BGConst.INT_NULL) {
    private val params: MutableMap<String, ParamObject> = HashMap()

    val publicString: String by lazy {
        GsonBuilder().serializeNulls().create()
                .toJson(baseParameter + params.mapValues { (_, v) -> v.publicParam })
    }

    val privateString: String by lazy {
        GsonBuilder().serializeNulls().create()
                .toJson(baseParameter + params.mapValues { (_, v) -> v.privateParam ?: v.publicParam })
    }

    /**
     * 设置基本参数
     */
    private val baseParameter: Map<String, Any> by lazy {
        mapOf<String, Any>("type" to type, "code" to code, "position" to position).filterValues { it != BGConst.INT_NULL }
    }

    /**
     * 设置私有参数
     * @param key
     * @param value
     * @return
     */
    fun private(key: String, value: Any?) = apply {
        if (value != null) this.params.computeIfAbsent(key) { ParamObject() }.privateParam = value
    }

    /**
     * 设置公共参数
     * @param key
     * @param value
     * @return
     */
    fun public(key: String, value: Any?) = apply {
        if (value != null) this.params.computeIfAbsent(key) { ParamObject() }.publicParam = value
    }

    fun send(sender: ISendable) = sender.sendResponse(this)

    @Synchronized
    fun send(senders: Collection<ISendable>) = synchronized(senders) {
        senders.forEach(this::send)
    }

    fun <T> send(sender: ISendableWith<T>, arg: T) = sender.sendResponse(arg, this)

    @Synchronized
    fun <T> send(senders: Collection<ISendableWith<T>>, arg: T) = synchronized(senders) {
        senders.forEach { this.send(it, arg) }
    }

    private data class ParamObject(var publicParam: Any? = null, var privateParam: Any? = null)
}
