package com.f14.tichu.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer
import com.f14.tichu.consts.TichuGameCmd


class TichuWishInterruptListener(gameMode: TichuGameMode, trigPlayer: TichuPlayer) : TichuInterruptListener(gameMode, trigPlayer) {
    private var wishedPoint = 0

    init {
        this.addListeningPlayer(trigPlayer)
    }

    override fun createInterruptParam() = super.createInterruptParam().also {
        it["wishedPoint"] = this.wishedPoint
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val point = action.getAsInt("point")
        this.wishedPoint = if (point in 2..14) point else 0
        this.setPlayerResponsed(action.getPlayer<TichuPlayer>())
    }


    override fun getMsg(player: TichuPlayer) = "请选择许愿的牌!"

    override val validCode: Int
        get() = TichuGameCmd.GAME_CODE_WISH_POINT

}
