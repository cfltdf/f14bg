package com.f14.bg

import com.f14.bg.component.Convertable
import com.f14.bg.player.Player
import net.sf.json.JSONObject
import net.sf.json.JsonConfig
import java.util.*

/**
 * VP计算器

 * @author F14eagle
 */
class VPCounter(var player: Player) : Comparable<VPCounter>, Convertable {
    val primaryVps: MutableList<VpObj> = ArrayList()
    val secondaryVps: MutableList<VpObj> = ArrayList()
    val displayVps: MutableList<VpObj> = ArrayList()
    var rank: Int = 0
    var isWinner: Boolean = false
    var score: Long = 0
    var rankPoint: Long = 0

    /**
     * 添加显示用的VP
     * @param label
     * @param vp
     */
    fun addDisplayVp(label: String, vp: Int) {
        this.displayVps.add(VpObj(label, vp))
    }

    /**
     * 添加次要的VP
     * @param label
     * @param vp
     */
    fun addSecondaryVp(label: String, vp: Int) {
        this.secondaryVps.add(VpObj(label, vp))
    }

    /**
     * 添加VP
     * @param label
     * @param vp
     */
    fun addVp(label: String, vp: Int) {
        this.primaryVps.add(VpObj(label, vp))
    }

    /**
     * 清除所有的VP
     */
    fun clearVps() {
        this.primaryVps.clear()
        this.secondaryVps.clear()
        this.displayVps.clear()
    }

    override fun compareTo(other: VPCounter): Int {
        val vp1 = this.totalVP
        val vp2 = other.totalVP
        when {
            vp1 > vp2 -> return 1
            vp1 < vp2 -> return -1
            else -> {
                // 总分相同的情况下,需要判断次要VP大小
                for (i in this.secondaryVps.indices) {
                    val v1 = this.secondaryVps[i]
                    val v2 = other.secondaryVps[i]
                    when {
                        v1.vp > v2.vp -> return 1
                        v1.vp < v2.vp -> return -1
                        else -> Unit
                    }
                }
                return 0
            }
        }
    }

    /**
     * 取得所有的VP,包括次要VP,和显示用的VP
     * @return
     */

    val allVps: List<VpObj>
        get() = this.primaryVps + this.secondaryVps + this.displayVps

    /**
     * 取得总显示的VP
     * @return
     */
    val totalDisplayVP: Int
        get() = displayVps.sumBy(VpObj::vp)

    /**
     * 取得总VP
     * @return
     */
    val totalVP: Int
        get() = primaryVps.sumBy(VpObj::vp)

    /**
     * 生成JSON字符串
     * @return
     */
    fun toJSONString(): String {
        val cfg = JsonConfig()
        cfg.excludes = arrayOf("player", "allVps", "totalVP")
        return JSONObject.fromObject(this, cfg).toString()
    }


    override fun toMap(): Map<String, Any> = mapOf(
            "userName" to this.player.name,
            "position" to this.player.position,
            "vs" to this.allVps,
            "totalVP" to this.totalVP,
            "rank" to this.rank,
            "rankPoint" to this.rankPoint,
            "score" to this.score
    )

    inner class VpObj(var label: String, var vp: Int)
}
