package com.f14.TS.utils

import com.f14.TS.consts.SuperPower
import com.f14.bg.common.ListMap
import com.f14.utils.DiceUtils

/**
 * TS的骰子

 * @author F14eagle
 */
object TSRoll {
    const val DICE = "D6"
    val cheatDice = ListMap<SuperPower, Int>()

    /**
     * 掷骰

     * @return
     */
    fun roll(superPower: SuperPower = SuperPower.NONE): Int {
        val list = cheatDice.getList(superPower)
        return if (list.isEmpty()) {
            DiceUtils.roll(DICE)
        } else {
            list.removeAt(0)
        }
    }

    fun cheat(superPower: SuperPower, result: Int) {
        val list = cheatDice.getList(superPower)
        list.add(result)
    }
}
