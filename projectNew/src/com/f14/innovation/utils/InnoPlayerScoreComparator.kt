package com.f14.innovation.utils

import com.f14.innovation.InnoPlayer
import java.util.*

/**
 * 玩家的得分比较器

 * @author F14eagle
 */
class InnoPlayerScoreComparator : Comparator<InnoPlayer> {

    override fun compare(o1: InnoPlayer, o2: InnoPlayer): Int {
        val s1 = o1.score
        val s2 = o2.score
        return when {
            s1 > s2 -> 1
            s1 < s2 -> -1
            else -> 0
        }
    }

}
