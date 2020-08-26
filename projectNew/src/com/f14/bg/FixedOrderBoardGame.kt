package com.f14.bg

import com.f14.bg.player.Player
import com.f14.bg.report.BgReport

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

abstract class FixedOrderBoardGame<P : Player, C : BoardGameConfig, R : BgReport<P>> : BoardGame<P, C, R>() {
    /**
     * 起始玩家
     */
    var startPlayer: P? = null
        protected set
    /**
     * 当前回合玩家
     */
    var currentPlayer: P? = null
        protected set

    /**
     * 取得指定玩家的下一个玩家
     * @param player
     * @return
     */
    fun getNextPlayer(player: P?): P? {
        return when (val i = this.players.indexOf(player)) {
            -1 -> null
            this.players.size - 1 -> this.players[0]
            else -> this.players[i + 1]
        }
    }

    /**
     * 前进到下一玩家
     * @return
     */
    fun nextPlayer(): P? {
        this.currentPlayer = this.getNextPlayer(this.currentPlayer)
        return this.currentPlayer
    }

    /**
     * 打乱玩家顺序并决定起始玩家
     */
    override fun regroupPlayers() {
        super.regroupPlayers()
        this.startPlayer = this.getPlayer(0)
        this.currentPlayer = this.startPlayer
    }
}
