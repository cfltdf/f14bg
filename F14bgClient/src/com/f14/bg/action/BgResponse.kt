package com.f14.bg.action

import com.f14.bg.BGConst
import net.sf.json.JSONObject
import java.util.*

/**
 * 回应
 * @author F14eagle
 */
class BgResponse(type: Int, code: Int, position: Int, result: Boolean) {
    /**
     * 指令类型
     */
    var type = BGConst.INT_NULL
    /**
     * 指令代码
     */
    var code = BGConst.INT_NULL
    /**
     * 位置
     */
    var position = BGConst.INT_NULL

    var publicParams: MutableMap<String, Any> = HashMap<String, Any>()
        protected set
    var privateParams: MutableMap<String, Any> = HashMap()
        protected set

    init {
        this.type = type
        this.code = code
        this.position = position
    }

    /**
     * 设置私有参数

     * @param key
     * *
     * @param value
     * *
     * @return
     */
    fun setPrivateParameter(key: String, value: Any): BgResponse {
        this.privateParams.put(key, value)
        return this
    }

    /**
     * 设置公共参数

     * @param key
     * *
     * @param value
     * *
     * @return
     */
    fun setPublicParameter(key: String, value: Any): BgResponse {
        this.publicParams.put(key, value)
        return this
    }

    /**
     * 仅将所有参数转换成字符串

     * @return
     */
    fun toPrivateString(): String {
        val res = JSONObject.fromObject(this.privateParams)
        res.accumulateAll(this.publicParams)
        this.setBaseParameter(res)
        return res.toString()
    }

    /**
     * 仅将公共参数转换成字符串

     * @return
     */
    fun toPublicString(): String {
        val res = JSONObject.fromObject(this.publicParams)
        this.setBaseParameter(res)
        return res.toString()
    }

    /**
     * 设置基本参数

     * @param o
     */
    protected fun setBaseParameter(o: JSONObject) {
        if (this.type != BGConst.INT_NULL) {
            o.put("type", this.type)
        }
        if (this.code != BGConst.INT_NULL) {
            o.put("code", this.code)
        }
        if (this.position != BGConst.INT_NULL) {
            o.put("position", this.position)
        }
    }
}
