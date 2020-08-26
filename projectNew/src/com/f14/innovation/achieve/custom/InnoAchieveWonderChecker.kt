package com.f14.innovation.achieve.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.achieve.InnoAchieveChecker
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoSplayDirection


/**
 * 特殊成就-奇迹 检查器

 * @author F14eagle
 */
class InnoAchieveWonderChecker(gameMode: InnoGameMode) : InnoAchieveChecker(gameMode) {

    override fun check(player: InnoPlayer): Boolean {
        // 拥有5种颜色,并且都向右或向上展开
        for (color in InnoColor.values()) {
            val stack = player.getCardStack(color) ?: return false
            if (stack.splayDirection != InnoSplayDirection.RIGHT && stack.splayDirection != InnoSplayDirection.UP) {
                return false
            }
        }
        return true
    }

}
