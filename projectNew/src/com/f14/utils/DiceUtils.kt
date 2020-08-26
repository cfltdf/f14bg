package com.f14.utils

import org.jdice.DiceParser

/**
 * 骰子
 *
 * @author F14eagle
 */
object DiceUtils {

    /**
     * 取得掷骰结果
     *
     * @param s
     * @return
     */
    fun roll(s: String): Int {
        val r = DiceParser.parseRoll(s)
        return if (r.isEmpty()) {
            -1
        } else {
            r[0].makeRoll().total
        }
    }
}
