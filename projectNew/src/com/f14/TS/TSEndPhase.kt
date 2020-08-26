package com.f14.TS

import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TSVictoryType
import com.f14.bg.GameEndPhase
import com.f14.bg.VPCounter
import com.f14.bg.VPResult
import com.f14.bg.exception.BoardGameException

/**
 * TS的结束阶段
 * @author F14eagle
 */
class TSEndPhase : GameEndPhase<TSGameMode>() {

    /**
     * 取得中盘获胜的VP结果
     * @param gameMode
     * @return
     */
    private fun createHalfWinResult(gameMode: TSGameMode): VPResult {
        val result = VPResult(gameMode.game)
        val winner = gameMode.winner
        val victoryType = gameMode.victoryType ?: throw BoardGameException("未知胜利方式!")
        val victoryText = TSVictoryType.getChinese(victoryType)

        if (winner == null) {
            // 如果没有获胜的玩家,则为平局
            val vpUssr = VPCounter(gameMode.game.ussrPlayer)
            vpUssr.addVp(victoryText, 0)
            result += (vpUssr)
            val vpcUsa = VPCounter(gameMode.game.ussrPlayer)
            vpcUsa.addVp(victoryText, 0)
            result += (vpcUsa)
        } else {
            gameMode.report.info(winner.reportString + victoryText)
            val vpcWinner = VPCounter(winner)
            vpcWinner.addVp(victoryText, 20)
            result += (vpcWinner)
            val vpcLoser = VPCounter(gameMode.game.getOppositePlayer(winner.superPower))
            vpcLoser.addVp("被秒杀", 0)
            result += (vpcLoser)
        }
        return result
    }

    /**
     * 取得正常结束的VP结果
     * @param gameMode
     * @return
     */
    private fun createNormalResult(gameMode: TSGameMode): VPResult {
        val result = VPResult(gameMode.game)
        // 终局计分
        val params = gameMode.scoreManager.executeFinalScore()
        val vpUssr = VPCounter(gameMode.game.ussrPlayer)
        val vpUsa = VPCounter(gameMode.game.usaPlayer)
        vpUssr.addDisplayVp("VP", gameMode.vp)
        vpUsa.addDisplayVp("VP", -gameMode.vp)
        for ((r, p) in params) {
            gameMode.report.info(r.chinese)
            gameMode.report.playerRegionScore(gameMode.game.ussrPlayer, p.ussr)
            gameMode.report.playerRegionScore(gameMode.game.usaPlayer, p.usa)
            vpUssr.addDisplayVp(r.chinese, p.vp)
            vpUsa.addDisplayVp(r.chinese, -p.vp)
        }
        val strChina = "中国牌"
//        when (gameMode.cardManager.chinaOwner) {
//            SuperPower.USSR -> {
//                vpUssr.addDisplayVp(strChina, 1)
//                vpUsa.addDisplayVp(strChina, -1)
//            }
//            SuperPower.USA -> {
//                vpUssr.addDisplayVp(strChina, -1)
//                vpUsa.addDisplayVp(strChina, 1)
//            }
//            else -> Unit
//        }
        val chinaCardOwner = gameMode.game.getPlayer(gameMode.cardManager.chinaOwner)
        if (chinaCardOwner != null) {
            gameMode.report.playerOwnChinaCard(chinaCardOwner)
            val chinaVp = if (SuperPower.USSR === gameMode.cardManager.chinaOwner) 1 else -1
            vpUssr.addDisplayVp(strChina, chinaVp)
            vpUsa.addDisplayVp(strChina, -chinaVp)
        }
        vpUssr.addVp("总计", vpUssr.totalDisplayVP)
        result += (vpUssr)
        vpUsa.addVp("总计", vpUsa.totalDisplayVP)
        result += (vpUsa)
        return result
    }


    override fun createVPResult(gameMode: TSGameMode): VPResult {
        return if (gameMode.victoryType === null) {
            this.createNormalResult(gameMode)
        } else {
            // 中途获胜
            this.createHalfWinResult(gameMode)
        }
    }

}
