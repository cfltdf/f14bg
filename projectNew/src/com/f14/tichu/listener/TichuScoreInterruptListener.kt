package com.f14.tichu.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer
import com.f14.tichu.consts.TichuGameCmd


class TichuScoreInterruptListener(gameMode: TichuGameMode, trigPlayer: TichuPlayer, score: Int) : TichuInterruptListener(gameMode, trigPlayer) {
    private var targetPlayer: TichuPlayer? = null
    private var score = 0

    init {
        this.score = score
        this.addListeningPlayer(trigPlayer)
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TichuPlayer>()
        val position = action.getAsInt("targetPosition")
        val targetPlayer = gameMode.game.getPlayer(position)
        if (gameMode.isFriendlyPlayer(player, targetPlayer)) {
            throw BoardGameException("必须把分数交给对方玩家!")
        }
        this.targetPlayer = targetPlayer
        this.setPlayerResponsed(player)
    }

    override fun createInterruptParam(): InterruptParam {
        return super.createInterruptParam().also {
            it["target"] = targetPlayer
        }
    }


    override fun getMsg(player: TichuPlayer): String {
        return "请将 $score 分交给对方玩家!"
    }

    override val validCode: Int
        get() = TichuGameCmd.GAME_CODE_GIVE_SCORE

}
