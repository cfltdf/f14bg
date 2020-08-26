package com.f14.tichu.listener

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.bg.consts.PlayerState
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.OrderActionListener
import com.f14.tichu.TichuConfig
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer
import com.f14.tichu.TichuReport

/**
 * tichu的顺序行动监听器
 * @author F14eagle
 */
abstract class TichuOrderListener(protected val gameMode: TichuGameMode, protected val startPlayer: TichuPlayer) : OrderActionListener<TichuPlayer, TichuConfig, TichuReport>(gameMode) {
    override val playersByOrder
        get() = gameMode.game.getPlayersByOrder(this.startPlayer)

    /**
     * 设置当前监听的玩家,原监听玩家暂时结束行动
     * @param gameMode
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun setCurrentListeningPlayer(player: TichuPlayer) {
        listeningPlayer?.let {
            CmdFactory.createGameResponse(CmdConst.GAME_CODE_PLAYER_RESPONSED, it.position)
                    .let(this::setListenerInfo)
                    .send(gameMode)
            this.setPlayerState(it, PlayerState.NONE)
        }
        while (true){
            val nextPlayer = this.playerIterator.next()
            if (player === nextPlayer){
                // 将当前监听玩家调整成指定的玩家
                this.listeningPlayer = player
                return
            }
        }
    }

}
