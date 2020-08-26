package com.f14.bg.listener

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.bg.BoardGameConfig
import com.f14.bg.GameMode
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.common.PlayerParamSet
import com.f14.bg.consts.PlayerState
import com.f14.bg.exception.BoardGameException
import com.f14.bg.player.Player
import com.f14.bg.report.BgReport
import com.f14.bg.utils.CheckUtils
import org.apache.log4j.Logger
import java.awt.event.ActionListener
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

abstract class ActionListener<P : Player, C : BoardGameConfig, R : BgReport<P>>(
        val mode: GameMode<P, C, R>,
        val listenerType: ListenerType = ListenerType.NORMAL
) {
    /**
     * 判断是否关闭监听
     */
    var closed: Boolean = false
        protected set
    protected val params: MutableMap<Int, PlayerParamSet> = HashMap()
    protected val actionSteps: MutableMap<P, MutableList<ActionStep<P, C, R>>> = LinkedHashMap()
    protected val listeningPlayers: MutableCollection<P> = LinkedHashSet()
    /**
     * 玩家的输入状态
     */
    protected var playerStates: MutableMap<P, PlayerState> = HashMap()


    /**
     * 为玩家添加行动步骤
     * @param player
     * @param step
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun addActionStep(player: P, step: ActionStep<P, C, R>) {
        val steps = this.getActionSteps(player)
        // 如果当前没有步骤,则触发该新增的步骤
        steps.add(step)
        if (steps.size == 1) step.onStepStart(player)
    }

    /**
     * 添加玩家到监听列表中
     * @param player
     */
    fun addListeningPlayer(player: P) {
        this.listeningPlayers.add(player)
    }

    /**
     * 添加玩家到监听列表中
     * @param players
     */
    fun addListeningPlayers(players: Collection<P>) {
        this.listeningPlayers.addAll(players)
    }

    /**
     * 在玩家开始监听前的检验方法
     * @param player
     * @return 如果返回true, 则需要玩家回应, false则不需玩家回应
     */
    protected open fun beforeListeningCheck(player: P) = true

    /**
     * 开始监听前执行的动作
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun beforeStartListen() = Unit

    /**
     * 检查该监听器是否完成监听
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun checkResponsed() {
        if (this.isAllPlayerResponsed) {
            // 如果所有玩家都回应了,则设置状态为完成
            this.onAllPlayerResponsed()
            this.endListen()
        }
    }

    /**
     * 关闭并移除监听
     */
    fun close() {
        this.closed = true
    }

    /**
     * 创建中断监听器的回调参数
     * @return
     */
    open fun createInterruptParam() = InterruptParam()

    /**
     * 创建阶段结束的指令
     * @return
     */
    protected fun createPhaseEndCommand(): BgResponse {
        return CmdFactory.createGameResponse(CmdConst.GAME_CODE_PHASE_END, -1)
                .let(this::setListenerInfo)
    }

    /**
     * 创建阶段开始的指令
     * @return
     */
    protected open fun createPhaseStartCommand(): BgResponse {
        return CmdFactory.createGameResponse(CmdConst.GAME_CODE_PHASE_START, -1)
                .let(this::setListenerInfo)
    }

    /**
     * 创建玩家回应的指令
     * @param player
     * @return
     */
    protected fun createPlayerResponsedCommand(player: P): BgResponse {
        return CmdFactory.createGameResponse(CmdConst.GAME_CODE_PLAYER_RESPONSED, player.position)
                .let(this::setListenerInfo)
    }

    /**
     * 创建开始监听的指令
     * @param player
     * @return
     */
    protected open fun createStartListenCommand(player: P): BgResponse {
        return CmdFactory.createGameResponse(CmdConst.GAME_CODE_START_LISTEN, player.position)
                .let(this::setListenerInfo)
    }

    /**
     * 创建监听器的行动指令
     * @param player
     * @param subact
     * @return
     */
    protected fun createSubactResponse(player: P?, subact: String): BgResponse {
        return CmdFactory.createGameResponse(this.validCode, player?.position ?: -1)
                .public("subact", subact)
    }

    /**
     * 执行行动
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun doAction(action: GameAction)

    /**
     * 结束监听
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun endListen() {
        this.createPhaseEndCommand().send(mode)
        this.close()
    }

    /**
     * 执行行动
     * @param action
     * @throws
     */
    @Throws(BoardGameException::class)
    fun execute(action: GameAction) {
        // 正常处理该指定
        CheckUtils.check(!isActionPositionValid(action), "你不能进行这个行动!")
        CheckUtils.check(isPlayerResponsed(action.getPlayer<Player>().position), "不能重复进行行动!")
        CheckUtils.check(action.code != this.validCode, "不能处理该行动代码!")
        val player = action.getPlayer<P>()
        when (val step = this.getCurrentActionStep(player)) {
            null -> this.doAction(action)
            else -> {
                // 如果存在步骤则需要处理步骤
                step.execute(action)
                if (step.isOver) {
                    // 如果步骤结束,则从步骤序列中移除
                    step.onStepOver(player)
                    this.removeCurrentActionStep(player)
                }
            }
        } // 如果不存在步骤,则执行以下代码
        this.checkResponsed()
    }

    /**
     * 取得玩家的所有行动步骤
     * @param player
     * @return
     */
    protected fun getActionSteps(player: P) = this.actionSteps.computeIfAbsent(player) { LinkedList() }

    /**
     * 取得玩家当前的行动步骤
     * @param player
     * @return
     */
    protected fun getCurrentActionStep(player: P) = this.getActionSteps(player).firstOrNull()

    /**
     * 取得玩家的参数
     * @param position
     * @return
     */

    fun <R> getParam(position: Int) = this.getPlayerParamSet(position).get<R>(PARAM_KEY)
            ?: throw BoardGameException("参数错误!")

    /**
     * 取得玩家的参数
     * @return
     */
    fun <R> getParam(player: P): R = this.getParam(player.position)

    /**
     * 取得玩家的参数集
     * @param position
     * @return
     */
    protected fun getPlayerParamSet(position: Int) = this.params.computeIfAbsent(position) { PlayerParamSet() }

    /**
     * 取得玩家的状态
     * @param player
     * @return
     */
    fun getPlayerState(player: P) = this.playerStates[player] ?: PlayerState.NONE

    /**
     * 取得可以处理的指令code
     * @return
     */
    protected abstract val validCode: Int

    /**
     * 初始化监听玩家
     */
    protected open fun initListeningPlayers() {
        val players = when {
            this.listeningPlayers.isEmpty() -> mode.game.players // 如果监听玩家列表为空,则允许所有玩家输入
            else -> this.listeningPlayers // 否则只允许在监听列表中的玩家输入
        }
        players.forEach { this.setNeedPlayerResponse(it.position, true) }
    }

    /**
     * 判断该行动的位置是否可以进行
     * @param action
     * @return
     */
    protected fun isActionPositionValid(action: GameAction) = this.isActionPositionValid(action.getPlayer<Player>().position)

    /**
     * 判断该行动的位置是否可以进行
     * @return
     */
    protected fun isActionPositionValid(position: Int) = this.getPlayerParamSet(position).needResponse

    /**
     * 判断是否所有玩家都已经回应
     * @return
     */
    val isAllPlayerResponsed: Boolean
        get() = this.params.values.all(PlayerParamSet::responsed)

    /**
     * 将所有玩家都设为已经回应
     */
    protected fun setAllPlayerResponsed() {
        this.params.keys.forEach(this::setPlayerResponsed)
    }

    /**
     * 判断是否需要玩家回应
     * @param position
     * @return
     */
    fun isNeedPlayerResponse(position: Int) = this.getPlayerParamSet(position).needResponse

    /**
     * 判断玩家是否回应
     * @param position
     * @return
     */
    fun isPlayerResponsed(position: Int) = this.getPlayerParamSet(position).responsed

    /**
     * 当所有玩家都回应后执行的动作
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    open fun onAllPlayerResponsed() = Unit

    /**
     * 玩家回应后执行的动作
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun onPlayerResponsed(player: P) = Unit

    /**
     * 玩家开始监听时触发的方法
     * @param player
     */
    protected open fun onPlayerStartListen(player: P) = Unit

    /**
     * 重新连接时处理的一些事情
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun onReconnect(player: P) {
        this.getCurrentActionStep(player)?.onStepStart(player)
    }


    /**
     * 开始监听时执行的动作
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun onStartListen() = Unit

    /**
     * 重新发送玩家监听信息
     */
    open fun refreshListeningState() {
        // 向所有不在旁观,并且需要回应的玩家,发送监听指令
        for (player in mode.game.players.filter(mode.game::isPlayingGame)) {
            // 如果玩家不是旁观状态,则可能需要向其发送监听指令
            if (!this.isPlayerResponsed(player.position)) {
                // 需要回应的话则发送监听指令
                this.sendStartListenCommand(player, player)
                this.onPlayerStartListen(player)
                try {
                    this.onReconnect(player)
                } catch (e: BoardGameException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 移除玩家的所有行动步骤
     * @param player
     */
    protected fun removeAllActionSteps(player: P) {
        this.actionSteps[player]?.clear()
    }

    /**
     * 移除当前步骤,开始下一步骤
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun removeCurrentActionStep(player: P) {
        val steps = this.getActionSteps(player)
        if (steps.isNotEmpty()) {
            val s = steps.removeAt(0)
            if (s.clearOtherStep) {
                // 如果该步骤需要移除所有剩余步骤,则移除
                this.removeAllActionSteps(player)
            } else {
                // 移除后如果还有步骤,则触发该步骤
                steps.firstOrNull()?.onStepStart(player)
            }
        }
    }

    /**
     * 向指定玩家发送所有玩家的状态
     * @param receiver
     */
    fun sendAllPlayersState(receiver: P?) {
        val map = mode.game.players.map { it to this.getPlayerState(it) }.toMap()
        mode.game.sendPlayerState(map, receiver)
    }

    /**
     * 发送当前监听器中,所有玩家的监听状态
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendCurrentPlayerListeningResponse() {
        // 重新连接时,先向玩家发送回合开始的指令
        this.createPhaseStartCommand().send(mode, null)
        // 向玩家发送监听器的一些额外信息
        this.sendPlayerListeningInfo(null)
        // 发送所有玩家的监听状态
        this.sendAllPlayersState(null)

        this.refreshListeningState()
    }

    /**
     * 向所有玩家发送开始监听的指令
     */
    open fun sendListenerCommand() {
        // 将所有需要返回输入的玩家的回应状态设为false,不需要返回的设为true
        for (p in mode.game.players) {
            val valid = this.isActionPositionValid(p.position)
            this.getPlayerParamSet(p.position).responsed = !valid
            if (valid) {
                if (this.beforeListeningCheck(p)) {
                    // 需要回应的话则发送监听指令
                    this.sendStartListenCommand(p, null)
                    this.onPlayerStartListen(p)
                    this.setPlayerState(p, PlayerState.INPUTING)
                } else {
                    // 不需要则直接设置为回应完成
                    this.setResponsed(p)
                }
            }
        }
        if (!isAllPlayerResponsed) mode.game.pushResponse()
    }

    /**
     * 向指定玩家发送监听器提供的一些额外信息,如果receiver为空,则向所有玩家发送
     * @param receiver
     */
    protected open fun sendPlayerListeningInfo(receiver: P?) = Unit

    /**
     * 发送重新连接时发送给玩家的指令
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    open fun sendReconnectResponse(player: P) {
        // 重新连接时,先向玩家发送回合开始的指令
        this.createPhaseStartCommand().send(mode, player)
        // 向玩家发送监听器的一些额外信息
        this.sendPlayerListeningInfo(player)
        // 发送所有玩家的监听状态
        this.sendAllPlayersState(player)
        // 如果玩家不是旁观状态,则可能需要向其发送监听指令
        if (mode.game.isPlayingGame(player)) {
            // 需要回应的话则发送监听指令
            if (!this.isPlayerResponsed(player.position)) {
                this.sendStartListenCommand(player, player)
                this.onPlayerStartListen(player)
                this.onReconnect(player)
            }
        }
    }

    /**
     * 向receiver发送player开始回合的指令,如果receiver为空,则向所有玩家发送
     * @param player
     * @param receiver
     */
    protected open fun sendStartListenCommand(player: P, receiver: P?) {
        this.createStartListenCommand(player).send(mode, receiver)
    }

    /**
     * 设置所有玩家的状态
     * @param state
     */
    fun setAllPlayersState(state: PlayerState) {
        mode.game.players.forEach { this.setPlayerState(it, state) }
    }

    /**
     * 为消息设置监听器的信息
     * @param res
     */
    protected open fun setListenerInfo(res: BgResponse) = res
            .public("listenerType", this.listenerType)
            .public("validCode", this.validCode)

    /**
     * 设置是否需要玩家回应
     * @param position
     * @param needResponse
     */
    protected fun setNeedPlayerResponse(position: Int, needResponse: Boolean) {
        this.getPlayerParamSet(position).also {
            it.needResponse = needResponse
            // 需要回应的玩家设置响应状态为false,不需要的,设为true
            it.responsed = !needResponse
        }
    }

    /**
     * 设置玩家的参数
     * @param position
     * @param param
     */
    fun <R : Any> setParam(position: Int, param: R) {
        this.setPlayerParam(position, PARAM_KEY, param)
    }

    /**
     * 设置玩家的参数
     * @param player
     * @param param
     */
    fun <R : Any> setParam(player: P, param: R) {
        this.setParam(player.position, param)
    }

    /**
     * 设置玩家的参数
     * @param position
     * @param key
     * @param value
     */
    protected fun setPlayerParam(position: Int, key: Any, value: Any) {
        this.getPlayerParamSet(position)[key] = value
    }

    /**
     * 设置玩家的回应状态为完成回应并将该信息返回到客户端
     * @param position
     */
    fun setPlayerResponsed(position: Int) {
        this.setPlayerResponsed(mode.game.getPlayer(position))
    }

    /**
     * 设置玩家的回应状态为完成回应并将该信息返回到客户端
     * @param player
     */
    open fun setPlayerResponsed(player: P) {
        this.createPlayerResponsedCommand(player).send(mode)
        this.setResponsed(player)
    }

    /**
     * 设置玩家的状态
     * @param player
     * @param state
     */
    fun setPlayerState(player: P, state: PlayerState) {
        this.playerStates[player] = state
        mode.game.sendPlayerState(mapOf(player to state), null)
    }

    /**
     * 设置玩家的回应状态为完成回应
     * @param player
     */
    protected fun setResponsed(player: P) {
        this.getPlayerParamSet(player.position).responsed = true
        this.setPlayerState(player, PlayerState.RESPONSED)
        try {
            this.onPlayerResponsed(player)
        } catch (e: BoardGameException) {
            log.error(e, e)
        }

    }

    /**
     * 开始监听
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun startListen() {
        // 开始监听指令前,先设置监听玩家和监听类型的参数
        this.initListeningPlayers()
        // 发送阶段开始的指令
        this.createPhaseStartCommand().send(mode)
        this.setAllPlayersState(PlayerState.NONE)
        this.beforeStartListen()
        // 发送监听器的一些额外信息
        this.sendPlayerListeningInfo(null)
        // 发送开始监听的指令
        this.sendListenerCommand()
        // 执行一些需要的行动
        this.onStartListen()
    }

    companion object {
        /**
         * 参数key值
         */
        private const val PARAM_KEY = "PARAM_KEY"
        val log = Logger.getLogger(ActionListener::class.java)!!
    }

}