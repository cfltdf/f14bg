package com.f14.TS

import com.f14.bg.BoardGameConfig
import com.google.gson.annotations.SerializedName

class TSConfig : BoardGameConfig() {
    /**
     * 指定苏联玩家
     */
    var ussrPlayer = -1
    /**
     * 苏联初始的让点
     */
    var point = 0
    /**
     * 新太空条
     */
    @SerializedName("newSpaceRace")
    var isNewSpaceRace = false
}
