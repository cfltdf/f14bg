package com.f14.loveletter

import com.f14.bg.GameEndPhase
import com.f14.bg.VPCounter
import com.f14.bg.VPResult
import org.apache.log4j.Logger


class LLEndPhase : GameEndPhase<LLGameMode>() {


    override fun createVPResult(gameMode: LLGameMode): VPResult {
        val result = VPResult(gameMode.game)
        for (p in gameMode.game.players) {
            log.debug("玩家 [" + p.user.name + "] 的分数:")
            val vpc = VPCounter(p)
            vpc.addVp("得分", p.score)
            result += vpc
            log.debug("总计 : " + vpc.totalVP)
        }
        return result
    }

    companion object {
        private val log = Logger.getLogger(LLEndPhase::class.java)!!
    }
}
