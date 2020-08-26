package com.f14.loveletter

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ActionListener
import com.f14.bg.listener.ListenerType
import com.f14.bg.report.BgReport


class LLWishListener(private val gameMode: LLGameMode, private val player: LLPlayer) : ActionListener<LLPlayer, LLConfig, BgReport<LLPlayer>>(gameMode, ListenerType.INTERRUPT) {
    private var point = 0.0

    init {
        this.addListeningPlayer(player)
    }

    override fun createInterruptParam() = super.createInterruptParam().apply { set("point", point) }


    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val point = action.getAsDouble("point")
        if (point == 1.0) throw BoardGameException("不能选择红美铃!")
        this.point = point
        gameMode.game.playerWish(player, point)
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = LLGameCmd.GAME_CODE_WISH_POINT

}
