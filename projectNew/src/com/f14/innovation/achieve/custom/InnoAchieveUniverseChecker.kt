package com.f14.innovation.achieve.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.achieve.InnoAchieveChecker
import com.f14.innovation.consts.InnoColor


/**
 * 特殊成就-宇宙 检查器

 * @author F14eagle
 */
class InnoAchieveUniverseChecker(gameMode: InnoGameMode) : InnoAchieveChecker(gameMode) {

    override fun check(player: InnoPlayer): Boolean {
        for (color in InnoColor.values()) {
            val stack = player.getCardStack(color) ?: return false
            if (stack.topCard!!.level < 8) {
                return false
            }
        }
        return true
    }

}
