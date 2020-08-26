package com.f14.utils


import java.util.*

object SequenceUtils {

    private val map = HashMap<Class<*>, Int>()

    /**
     * 生成id
     * @param clazz
     * @return
     */
    @Synchronized
    fun generateId(clazz: Class<*>): String {
        val i = (map[clazz] ?: 0) + 1
        map[clazz] = i
        return i.toString()
    }
}
