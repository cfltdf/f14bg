package com.f14.innovation.achieve.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.achieve.InnoAchieveChecker


/**
 * 特殊成就-纪念碑 检查器
 * @author F14eagle
 */
class InnoAchieveMemorialChecker(gameMode: InnoGameMode) : InnoAchieveChecker(gameMode) {
    override fun check(player: InnoPlayer): Boolean {
        // 回合计分牌或垫底牌达到6张
        return player.roundScoreCount >= 6 || player.roundTuckCount >= 6
    }

}
