package com.f14.bg

import com.f14.bg.consts.BgVersion
import com.f14.bg.consts.TeamMode
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

abstract class BoardGameConfig {
    var playerNumber: Int = 0
    var versions: MutableSet<String> = HashSet()
    var randomSeat = true
    @SerializedName("teamMatch")
    var isTeamMatch = false
    var teamMode = TeamMode.RANDOM

    /**
     * 判断该配置是否拥有指定的扩充
     * @param expName
     * @return
     */
    fun hasExpansion(expName: String): Boolean {
        return this.versions.contains(expName)
    }

    /**
     * 判断该配置是否是基础版游戏
     * @return
     */
    val isBaseGame: Boolean
        get() = versions.size == 1 && versions.contains(BgVersion.BASE)
}
