package com.f14.RFTG.mode

import com.f14.RFTG.RFTG
import com.f14.RFTG.consts.GameState
import com.f14.RFTG.consts.RaceActionType
import com.f14.RFTG.listener.DevelopActionListener
import com.f14.RFTG.listener.SettleActionListener
import com.f14.bg.exception.BoardGameException

/**
 * RFTG - 2人用的游戏模式

 * @author F14eagle
 */
class RaceGame2P(game: RFTG) : RaceGameMode(game) {
    override val actionNum = 2
    override val validActions = arrayOf(
            RaceActionType.EXPLORE_1,
            RaceActionType.EXPLORE_2,
            RaceActionType.DEVELOP,
            RaceActionType.DEVELOP_2,
            RaceActionType.SETTLE,
            RaceActionType.SETTLE_2,
            RaceActionType.CONSUME_1,
            RaceActionType.CONSUME_2,
            RaceActionType.PRODUCE
    )

    @Throws(BoardGameException::class)
    override fun round() {
        this.waitForAction()
        this.waitForExplore()
        this.waitForDevelop()
        this.waitForDevelop2()
        this.waitForSettle()
        this.waitForSettle2()
        this.waitForConsume()
        this.waitForProduce()
        this.waitForRoundDiscard()
    }

    /**
     * 等待玩家执行开发2阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForDevelop2() {
        log.info("进入开发2阶段...")
        this.setGameState(GameState.ACTION_DEVELOP_2)
        this.waitForPhase(GameState.ACTION_DEVELOP_2, ::DevelopActionListener, RaceActionType.DEVELOP_2)
        log.info("开发2阶段结束!")
    }

    /**
     * 等待玩家执行扩张2阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForSettle2() {
        log.info("进入扩张2阶段...")
        this.waitForPhase(GameState.ACTION_SETTLE_2, ::SettleActionListener, RaceActionType.SETTLE_2)
        log.info("扩张2阶段结束!")
    }
}
