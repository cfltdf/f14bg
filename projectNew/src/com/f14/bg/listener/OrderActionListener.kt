package com.f14.bg.listener

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.bg.BoardGameConfig
import com.f14.bg.GameMode
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.consts.PlayerState
import com.f14.bg.exception.BoardGameException
import com.f14.bg.player.Player
import com.f14.bg.report.BgReport

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */
abstract class OrderActionListener<P : Player, C : BoardGameConfig, R : BgReport<P>>(
        mode: GameMode<P, C, R>,
        listenerType: ListenerType = ListenerType.NORMAL
) : ActionListener<P, C, R>(mode, listenerType) {
    protected val playerIterator: Iterator<P> = sequence {
        val players = this@OrderActionListener.playersByOrder
        while (true) yieldAll(players)
    }.iterator()

    /**
     * 取得当前正在监听的玩家
     */
    var listeningPlayer: P? = null
        protected set

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<P>()
        if (player !== listeningPlayer) {
            throw BoardGameException("你还不能执行该行动!")
        }
    }

    /**
     * 取得指定玩家的下一个还未完成监的听玩家
     * @param player
     * @return
     */
    fun getNextAvailablePlayer(player: P): P? {
        val players = this.playersByOrder
        val i = players.indexOf(player)
        val count = players.size
        return (players + players)
                .drop(i + 1)
                .take(count)
                .firstOrNull { this.isActionPositionValid(it.position) && !this.isPlayerResponsed(it.position) }
    }

    /**
     * 取得玩家的行动序列
     * @return
     */
    protected abstract val playersByOrder: List<P>

    /**
     * 玩家回合开始时的行动
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun onPlayerTurn(player: P) = Unit

    override fun refreshListeningState() {
        // 当前监听玩家,则可能需要向其发送监听指令
        listeningPlayer?.let {
            val responsed = this.isPlayerResponsed(it.position)
            if (!responsed) {
                this.sendStartListenCommand(it, it)
                this.onPlayerStartListen(it)
                try {
                    this.onReconnect(it)
                } catch (e: BoardGameException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 向当前玩家发送监听指令
     */
    @Synchronized
    override fun sendListenerCommand() {
        // 将所有需要返回输入的玩家的回应状态设为false,不需要返回的设为true
        for (p in mode.game.players) {
            val valid = this.isActionPositionValid(p.position)
            this.getPlayerParamSet(p.position).responsed = !valid
        }
        // 向序列中的下一个玩家发送监听指令
        try {
            this.sendNextListenerCommand()
        } catch (e: BoardGameException) {
            log.error(e, e)
        }

    }

    /**
     * 为等待序列中的下一个玩家发送开始监听的指令
     * @throws BoardGameException
     */
    @Synchronized
    @Throws(BoardGameException::class)
    protected open fun sendNextListenerCommand() {
        listeningPlayer = null
        while (!isAllPlayerResponsed) {
            val player = this.playerIterator.next()
            if (this.isActionPositionValid(player.position) && !this.isPlayerResponsed(player.position)) {
                // 如果需要该玩家回应,则发送开始监听的指令给该玩家
                if (this.beforeListeningCheck(player)) {
                    listeningPlayer = player
                    this.sendStartListenCommand(player, null)
                    this.onPlayerStartListen(player)
                    this.setPlayerState(player, PlayerState.INPUTING)
                    this.onPlayerTurn(player)
                    this.mode.game.pushResponse()
                    return
                } else {
                    // 如果不需要回应,则设置为已回应
                    this.setResponsed(player)
                }
            }
        }
    }

    /**
     * 发送重新连接时发送给玩家的指令
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun sendReconnectResponse(player: P) {
        // 重新连接时,先向玩家发送回合开始的指令
        this.createPhaseStartCommand().send(mode, player)
        // 向玩家发送监听器的一些额外信息
        this.sendPlayerListeningInfo(player)
        // 发送所有玩家的监听状态
        this.sendAllPlayersState(player)
        // 如果玩家是当前监听玩家,并且不是旁观状态,则可能需要向其发送监听指令
        if (this.listeningPlayer === player && mode.game.isPlayingGame(player)) {
            // 需要回应的话则发送监听指令
            val responsed = this.isPlayerResponsed(player.position)
            if (!responsed) {
                this.sendStartListenCommand(player, player)
                this.onPlayerStartListen(listeningPlayer!!)
                this.onReconnect(player)
            }
        }
    }

    override fun setPlayerResponsed(player: P) {
        super.setPlayerResponsed(player)
        // 给下一位玩家发送监听消息
        try {
            this.sendNextListenerCommand()
        } catch (e: BoardGameException) {
            log.error(e, e)
        }

    }

    /**
     * 设置玩家暂时完成输入并由下一位玩家开始输入
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun setPlayerResponsedTemp(player: P) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_PLAYER_RESPONSED, player.position)
                .let(this::setListenerInfo)
                .send(mode)
        this.setPlayerState(player, PlayerState.NONE)
        this.sendNextListenerCommand()
    }
}
