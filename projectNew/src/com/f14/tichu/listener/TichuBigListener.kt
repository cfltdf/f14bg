package com.f14.tichu.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer
import com.f14.tichu.consts.TichuGameCmd
import com.f14.tichu.consts.TichuType


/**
 * 叫大地主的监听器

 * @author F14eagle
 */
class TichuBigListener(gameMode: TichuGameMode) : TichuActionListener(gameMode) {

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val subact = action.getAsString("subact")
        val player = action.getPlayer<TichuPlayer>()
        if ("confirm" == subact) {
            // 叫大地主
            gameMode.game.playerCallTichu(player, TichuType.BIG_TICHU)
            this.setPlayerResponsed(player)
        } else if ("pass" == subact) {
            // 不叫
            this.setPlayerResponsed(player)
        }
    }

    override val validCode: Int
        get() = TichuGameCmd.GAME_CODE_BIG_TICHU_PHASE

}
