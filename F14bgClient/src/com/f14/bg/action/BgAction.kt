package com.f14.bg.action

import net.sf.json.JSONArray
import net.sf.json.JSONException
import net.sf.json.JSONObject

import com.f14.bg.BGConst

class BgAction {
    /**
     * 取得行动类型

     * @return
     */
    var type: Int = 0
        protected set
    /**
     * 取得行动代码

     * @return
     */
    var code: Int = 0
        protected set
    /**
     * 取得所有参数

     * @return
     */
    var parameters: JSONObject
        protected set

    constructor(type: Int, code: Int) {
        this.type = type
        this.code = code
        this.parameters = JSONObject()
    }

    constructor(jstr: String) {
        this.parameters = JSONObject.fromObject(jstr)
        this.type = this.getAsInt("type")
        this.code = this.getAsInt("code")
    }

    /**
     * 设置参数

     * @param key
     * *
     * @param value
     */
    fun setParameter(key: String, value: Any) {
        this.parameters.put(key, value)
    }

    /**
     * 取得字符串类型参数

     * @param key
     * *
     * @return
     */
    fun getAsString(key: String): String? {
        try {
            return this.parameters.getString(key)
        } catch (e: JSONException) {
            return null
        }

    }

    /**
     * 取得int类型参数

     * @param key
     * *
     * @return
     */
    fun getAsInt(key: String): Int {
        try {
            return this.parameters.getInt(key)
        } catch (e: JSONException) {
            return BGConst.INT_NULL
        }

    }

    /**
     * 取得long类型参数

     * @param key
     * *
     * @return
     */
    fun getAsLong(key: String): Long {
        try {
            return this.parameters.getLong(key)
        } catch (e: JSONException) {
            return BGConst.INT_NULL.toLong()
        }

    }

    /**
     * 取得boolean类型参数

     * @param key
     * *
     * @return
     */
    fun getAsBoolean(key: String): Boolean {
        try {
            return this.parameters.getBoolean(key)
        } catch (e: JSONException) {
            return false
        }

    }

    /**
     * 按照类型取得对象

     * @param <C>
     * *
     * @param key
     * *
     * @param clazz
     * *
     * @return
    </C> */
    fun <C> getAsObject(key: String, clazz: Class<C>): C? {
        val obj = this.parameters.getJSONObject(key)
        if (obj == null || obj.isNullObject) {
            return null
        } else {
            return JSONObject.toBean(obj, clazz) as C
        }
    }

    /**
     * 按照类型取得对象

     * @param key
     * *
     * @return
     */
    fun getAsObject(key: String): JSONObject? {
        val obj = this.parameters.getJSONObject(key) as JSONObject
        if (obj == null || obj.isNullObject) {
            return null
        } else {
            return obj
        }
    }

    /**
     * 按照类型取得对象

     * @param key
     * *
     * @return
     */
    fun getAsArray(key: String): JSONArray? {
        val obj = this.parameters.getJSONArray(key) as JSONArray
        if (obj == null) {
            return null
        } else {
            return obj
        }
    }

    /**
     * 取得参数内容的JSON字符串

     * @return
     */
    val jsonString: String
        get() = this.parameters.toString()
}
