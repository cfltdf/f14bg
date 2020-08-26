package com.f14.bg

import com.f14.bg.component.Convertable

import java.util.*

class VPResult(var boardGame: BoardGame<*, *, *>) : Convertable {
    val vpCounters: MutableList<VPCounter> = ArrayList()

    /**
     * 添加玩家的VP计数器
     * @param vpc
     */
    operator fun plusAssign(vpc: VPCounter) {
        this.vpCounters.add(vpc)
    }


    /**
     * 计算玩家的排名和结果
     */
    fun sort() {
        // 倒叙,分数从大到小
        this.vpCounters.sort()
        this.vpCounters.reverse()
        // 设置所有玩家的排名和获胜的情况
        this.vpCounters.fold(Pair<VPCounter?, Int>(null, 1)){ (c, i), vpc ->
            vpc.rank = if (c?.compareTo(vpc) == 0) c.rank else i
            vpc.isWinner = vpc.rank == 1
            vpc to (i + 1)
        }
    }


    override fun toMap() = mapOf("vps" to this.vpCounters.map(VPCounter::toMap))

}
