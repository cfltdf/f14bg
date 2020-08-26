package com.f14.innovation.achieve.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.achieve.InnoAchieveChecker
import com.f14.innovation.consts.InnoIcon


/**
 * 特殊成就-世界 检查器

 * @author F14eagle
 */
class InnoAchieveWorldChecker(gameMode: InnoGameMode) : InnoAchieveChecker(gameMode) {

    override fun check(player: InnoPlayer): Boolean {
        // 达到12个时钟图标
        return player.getIconCount(InnoIcon.CLOCK) >= 12
    }

}
