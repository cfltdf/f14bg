package com.f14.F14bgClient

import java.util.HashMap

/**
 * 版本对象

 * @author F14eagle
 */
class Version {
    val version = "v0.9.0"

    fun toMap(): Map<String, String> {
        val map = HashMap<String, String>()
        map.put("version", this.version)
        return map
    }
}
