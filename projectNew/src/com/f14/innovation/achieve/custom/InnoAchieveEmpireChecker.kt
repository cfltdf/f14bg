package com.f14.innovation.achieve.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.achieve.InnoAchieveChecker
import com.f14.innovation.consts.InnoIcon


/**
 * 特殊成就-帝国 检查器

 * @author F14eagle
 */
class InnoAchieveEmpireChecker(gameMode: InnoGameMode) : InnoAchieveChecker(gameMode) {

    override fun check(player: InnoPlayer): Boolean {
        // 所有图标都达到3个则成功
        return InnoIcon.values().none { player.getIconCount(it) < 3 }
    }

}
