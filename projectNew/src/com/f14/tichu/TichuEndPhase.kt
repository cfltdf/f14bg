package com.f14.tichu

import com.f14.bg.GameEndPhase
import com.f14.bg.VPCounter
import com.f14.bg.VPResult
import org.apache.log4j.Logger


class TichuEndPhase : GameEndPhase<TichuGameMode>() {


    override fun createVPResult(gameMode: TichuGameMode): VPResult {
        val result = VPResult(gameMode.game)
        for (player in gameMode.game.players) {
            log.debug("玩家 [" + player.user.name + "] 的分数:")
            val vpc = VPCounter(player)
            result += vpc
            vpc.addVp("得分", gameMode.getPlayerGroup(player).score)
            log.debug("总计 : " + vpc.totalVP)
        }
        return result
    }

    companion object {
        private val log = Logger.getLogger(TichuEndPhase::class.java)!!
    }
}
