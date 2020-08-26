package com.f14.innovation.utils

import com.f14.innovation.InnoPlayer
import com.f14.innovation.consts.InnoIcon
import java.util.*

/**
 * 玩家的符号比较器

 * @author F14eagle
 */
class InnoPlayerIconComparator(private var icon: InnoIcon) : Comparator<InnoPlayer> {

    override fun compare(o1: InnoPlayer, o2: InnoPlayer): Int {
        val s1 = o1.getIconCount(icon)
        val s2 = o2.getIconCount(icon)
        return when {
            s1 > s2 -> 1
            s1 < s2 -> -1
            else -> 0
        }
    }

}
