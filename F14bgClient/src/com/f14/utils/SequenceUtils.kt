package com.f14.utils

import java.util.HashMap

object SequenceUtils {
    private val map = HashMap<Class<*>, Int>()

    /**
     * 生成id

     * @param clazz
     * *
     * @return
     */
    @Synchronized fun generateId(clazz: Class<*>): String {
        var i: Int? = map[clazz]
        if (i == null) {
            i = 0
        }
        i++
        map.put(clazz, i)
        return i.toString() + ""
    }
}
