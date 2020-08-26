package com.f14.innovation

import com.f14.bg.GameEndPhase
import com.f14.bg.VPCounter
import com.f14.bg.VPResult
import com.f14.innovation.consts.InnoVictoryType


/**
 * Innovation的结束阶段
 * @author F14eagle
 */
class InnoEndPhase : GameEndPhase<InnoGameMode>() {

    /**
     * 创建成就胜利的结果
     * @param gm
     * @return
     */
    private fun createAchieveVictoryResult(gm: InnoGameMode): VPResult {
        val result = VPResult(gm.game)
        for (player in gm.game.players) {
            val vpc = VPCounter(player)
            vpc.addVp("成就", gm.getTeamAchieveCardNum(player))
            result += vpc
        }
        return result
    }

    /**
     * 创建分数胜利的结果
     * @param
     * @return
     */
    private fun createScoreVictoryResult(gm: InnoGameMode): VPResult {
        val result = VPResult(gm.game)
        for (player in gm.game.players) {
            val vpc = VPCounter(player)
            vpc.addVp("分数", gm.getTeamScore(player))
            vpc.addSecondaryVp("成就", gm.getTeamAchieveCardNum(player))
            result += vpc
        }
        return result
    }

    /**
     * 创建特殊胜利的结果
     * @param gm
     * @return
     */
    private fun createSpecialVictoryResult(gm: InnoGameMode): VPResult {
        val result = VPResult(gm.game)
        val winner = gm.victoryPlayer!!
        val label = gm.victoryObject!!.name + " 的特殊效果"
        for (player in gm.game.players) {
            val vpc = VPCounter(player)
            if (player === winner || gm.game.isTeammates(winner, player)) {
                vpc.addVp(label, 1)
            } else {
                vpc.addVp(label, 0)
            }
            result += vpc
        }
        return result
    }


    override fun createVPResult(gameMode: InnoGameMode): VPResult {
        // 按照获胜的类型,创建对应的参数
        return when (gameMode.victoryType) {
            InnoVictoryType.ACHIEVE_VICTORY -> this.createAchieveVictoryResult(gameMode)
            InnoVictoryType.SCORE_VICTORY -> this.createScoreVictoryResult(gameMode)
            InnoVictoryType.SPECIAL_VICTORY -> this.createSpecialVictoryResult(gameMode)
        }
    }

}
