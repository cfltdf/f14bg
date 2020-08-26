package com.f14.loveletter

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ActionListener
import com.f14.bg.report.BgReport


class LLResultListener(var gameMode: LLGameMode) : ActionListener<LLPlayer, LLConfig, BgReport<LLPlayer>>(gameMode) {

    private var resultResponse: BgResponse = this.createResultResponse()


    private fun createResultResponse(): BgResponse {
        val res = this.createSubactResponse(null, "loadParam")
        gameMode.game.getPlayersByOrder(gameMode.winner).mapNotNull { it.firstCard?.to(it) }.forEach { res.public("p" + it.second.position, it.first.id) }
        val player = gameMode.game.getPlayersByOrder(gameMode.winner).max() ?: gameMode.winner
        res.public("winner", player.position)
        gameMode.game.playerWin(player)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<LLPlayer>()
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = LLGameCmd.GAME_CODE_ROUND_RESULT

    override fun sendPlayerListeningInfo(receiver: LLPlayer?) {
        super.sendPlayerListeningInfo(receiver)
        // 发送回合得分的指令
        gameMode.game.sendResponse(receiver, resultResponse)
    }

}
