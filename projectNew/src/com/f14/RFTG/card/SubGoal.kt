package com.f14.RFTG.card

import com.f14.RFTG.RacePlayer
import com.f14.RFTG.consts.GameState


/**
 * 目标中用到的子目标

 * @author F14eagle
 */
class SubGoal {
    var whiteCondition: Condition? = null
    var blackCondition: Condition? = null
    var goalNum: Int = 0
    var gameState: GameState? = null

    /**
     * 结合黑白条件判断该牌是否符合规则
     * @param card
     * @return
     */
    private fun test(card: RaceCard): Boolean {
        val wc = whiteCondition == null || whiteCondition!!.test(card)
        val bc = blackCondition == null || !blackCondition!!.test(card)
        return wc and bc
    }

    /**
     * 检查玩家是否达成该目标
     * @param player
     * @return 返回目标指数
     */
    fun test(player: RacePlayer): Int {
        val res = player.builtCards.count(this::test)
        return if (res >= this.goalNum) res else -1
    }
}
