package com.f14.bg

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.manager.ResourceManager
import com.f14.F14bg.network.CmdFactory
import com.f14.F14bg.utils.ResourceUtils
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.action.ISendable
import com.f14.bg.action.ISendableWith
import com.f14.bg.anim.AnimParam
import com.f14.bg.chat.IChatable
import com.f14.bg.chat.IChatableWith
import com.f14.bg.chat.Message
import com.f14.bg.consts.BgState
import com.f14.bg.consts.PlayerState
import com.f14.bg.exception.BoardGameException
import com.f14.bg.hall.GameRoom
import com.f14.bg.player.Player
import com.f14.bg.report.BgReport
import com.f14.bg.utils.BgUtils
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CollectionUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.SendChannel
import net.sf.json.JSONObject
import org.apache.log4j.Logger
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

abstract class BoardGame<P : Player, C : BoardGameConfig, R : BgReport<P>> : ISendable, ISendableWith<P?>, IChatable, IChatableWith<P?> {
    protected val log = Logger.getLogger(this.javaClass)!!

    val players: MutableList<P> = ArrayList()
    abstract val mode: GameMode<P, C, R>
    lateinit var startTime: Date
        protected set
    lateinit var room: GameRoom
        protected set
    lateinit var config: C
        protected set
    lateinit var report: R
        protected set
    lateinit var actor: SendChannel<GameAction>
        protected set
    private val currentResponse: MutableList<String> = ArrayList()
    val cachedResponse: MutableList<Array<String>> = ArrayList()

    /**
     * 玩家可否离开（不影响游戏进行）
     * @param player
     * @return
     */
    open fun canLeave(player: Player): Boolean = false

    /**
     * 清空所有玩家
     */
    protected fun clearPlayers() = this.players.clear()

    /**
     * 按照用户选择的参数创建游戏配置对象
     * @param <C>
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun createConfig(obj: JSONObject): C

    /**
     * 创建游戏配置的信息
     * @return
     */

    fun createConfigResponse(): BgResponse = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_LOAD_CONFIG, -1).public("config", this.config)

    /**
     * 执行行动
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun doAction(act: GameAction) = actor.offer(act)

    /**
     * 作弊代码
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    open fun doCheat(msg: String): Boolean {
        return when {
            msg.startsWith("kill") -> {
                val position = Integer.valueOf(msg.substring(5)) ?: throw BoardGameException("INVALID POSITION")
                val p = this.getPlayer(position)
                this.room.replace(p.user)
                true
            }
            msg.startsWith("end") -> {
                this.interruptGame()
                true
            }
            else -> false
        }
    }

    /**
     * 结束游戏
     * @throws BoardGameException
     */
    fun endGame() {
        this.players.forEach { it.position = BGConst.INT_NULL }
        // 结束时清空所有玩家
        this.clearPlayers()
        // 将结束游戏的信息发送到客户端
        this.sendGameEndResponse()
        this.pushResponse()
    }

    /**
     * 设置游戏配置
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun setConfig(action: GameAction) {
        try {
            CheckUtils.check(this.room.state != BgState.WAITING, "游戏状态错误,不能改变游戏设置!")
            val config = action.parameters.getJSONObject("config")
            this.config = this.createConfig(config)
            // 设置完成后,需要将玩家更改设置的消息,返回到客户端
            Message("玩家 [${BgUtils.escapeHtml(action.getPlayer<Player>().name)}] 更改了游戏设置!").send(this)
        } catch (e: BoardGameException) {
            // 如果改变设置时发生错误,则重新发送游戏设置信息到客户端
            this.room.sendConfig(action.getPlayer<Player>().user)
            throw e
        }

    }

    /**
     * 取得当前玩家数量
     * @return
     */
    val currentPlayerNumber: Int
        get() = this.players.size

    /**
     * 取得指定玩家的下一位玩家
     * @return
     */
    fun getNextPlayersByOrder(player: P): P {
        return this.getPlayersByOrder(player)[1]
    }

    /**
     * 取得指定位置上的玩家
     * @param position
     * @return
     */

    fun getPlayer(position: Int): P {
        return this.players.elementAtOrNull(position) ?: throw BoardGameException("INVALID PLAYER POSITION")
//        return if (position < 0 || position >= this.players.size) {
//            null
//        } else {
//            this.players[position]
//        }
    }

    /**
     * 从当前回合玩家开始取得所有玩家的序列
     * @return
     */
    open val playersByOrder: List<P>
        get() = this.getPlayersByOrder(0)

    /**
     * 从指定位置开始,按顺序取得所有玩家的序列
     * @param position
     * @return
     */

    private fun getPlayersByOrder(position: Int) = this.players.drop(position) + this.players.take(position)

    /**
     * 从指定玩家开始,按顺序取得所有玩家的序列
     * @return
     */

    fun getPlayersByOrder(player: P) = this.getPlayersByOrder(player.position)

    /**
     * 取得资源管理器
     * @param <RM>
     * @return
    </RM> */
    fun <RM : ResourceManager> getResourceManager() = ResourceUtils.getResourceManager<RM>(this.room.type)

    var state: BgState
        get() = this.room.state
        set(state) {
            this.room.state = state
        }

    /**
     * 初始化
     */
    fun init(room: GameRoom) {
        this.room = room
        initConst()
        initConfig()
    }

    /**
     * 初始化游戏配置
     */
    abstract fun initConfig()

    /**
     * 初始化常量
     */
    abstract fun initConst()

    /**
     * 初始化玩家的游戏信息
     */
    protected fun initPlayers() = this.players.forEach(Player::reset)

    /**
     * 初始化玩家的座位信息
     */
    @Throws(BoardGameException::class)
    protected open fun initPlayersSeat() {
        // 如果是随机座位,则打乱玩家的顺序
        if (this.config.randomSeat) this.regroupPlayers()
        this.players.withIndex().forEach { (i, p) -> p.position = i }
    }

    /**
     * 初始化玩家的组队情况
     */
    protected open fun initPlayerTeams() = // 按座位号设置队伍,则所有人都是敌对的
            this.players.forEach { it.team = it.position }

    /**
     * 初始化战报模块
     */
    abstract fun initReport()

    /**
     * 中断游戏
     */
    fun interruptGame() {
        this.state = BgState.INTERRUPT
        wakeAll()
    }

    /**
     * 判断游戏是否在进行中
     * @return
     */
    val isPlaying: Boolean
        get() = this.room.isPlaying

    /**
     * 判断玩家是否在游戏中
     * @param player
     * @return
     */
    fun isPlayingGame(player: P): Boolean = this.players.contains(player)

    /**
     * 判断是否是组队赛
     * @return
     */
    open val isTeamMatch: Boolean
        get() = this.config.isTeamMatch

    /**
     * 判断这些玩家是否是队友
     * @param players
     * @return
     */
    @SafeVarargs
    fun isTeammates(vararg players: P): Boolean = when {
        this.isTeamMatch -> players.distinctBy { it.team }.count() == 1
        else -> false
    }

    /**
     * 按照指定的位置加入游戏, 如果游戏已经开始,或者游戏人数已满,或者未能 取到空位,则不能加入游戏并抛出异常
     * @param player
     * @throws BoardGameException
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(BoardGameException::class)
    @Synchronized
    fun joinGame(player: Player) = synchronized(this.players) {
        CheckUtils.check(players.size > this.room.maxPlayerNumber, "未取得空位,不能加入游戏!")
        player.reset()
        this.players.add(player as P)
    }


    /**
     * 游戏开始时执行的方法
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun onStartGame() {
        this.clearResponse()
        // 初始化游戏信息
        this.initReport()
        this.report.start()
        this.startTime = Date()
        // 设置玩家的座位顺序
        this.initPlayersSeat()
        this.initPlayerTeams()
        // 发送游戏开始的指令
        this.sendGameStartResponse()
        this.initPlayers()
        this.sendLocalPlayerInfo(null)
        this.sendPlayerSitInfo(null)
        this.setupGame()
    }

    /**
     * 将游戏中的玩家的座位信息发送给客户端
     */
    @Synchronized
    fun sendPlayerSitInfo(receiver: P?) = synchronized(this.players) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_LOAD_PLAYER, -1).public("players", players.map { it.toMap() }).send(this, receiver)
    }

    private fun clearResponse() {
        this.currentResponse.clear()
        this.cachedResponse.clear()
    }

    fun pushResponse() {
        if (this.currentResponse.isNotEmpty()) {
            this.cachedResponse.add(this.currentResponse.toTypedArray())
            this.currentResponse.clear()
        }
    }

    /**
     * 重新排列玩家的位置
     */
    @Synchronized
    open fun regroupPlayers() = synchronized(this.players) {
        // 打乱玩家的顺序
        CollectionUtils.shuffle(this.players)
        // 设置座位号
        this.players.withIndex().forEach { (i, p) -> p.position = i }
    }

    /**
     * 玩家强行离开游戏
     * @param player
     * @return
     */
    @Synchronized
    fun removePlayerForce(player: Player): Boolean {
        when {
            this.room.isPlaying && this.canLeave(player) -> return false
            this.players.contains(player) -> synchronized(this.players) {
                this.players.remove(player)
                player.reset()
                // 将玩家退出的信息发送到客户端
                CmdFactory.createGameResponse(CmdConst.GAME_CODE_REMOVE_PLAYER, player.position).public("userId", player.user.id).public("sitPosition", player.position).send(this)
                // 游戏属性发生变化
                this.room.onGamePropertyChange()
                // 如果玩家在游戏进行中退出,则中断游戏
                if (this.room.isPlaying) {
                    this.interruptGame()
                    return true
                }
            }
        }
        return false
    }

    @Throws(Exception::class)
    suspend fun run() {
        log.info("游戏开始!")
        onStartGame()
        val job = Job()
        actor = mode.run(job)
        job.start()
        try {
            job.join()
        } catch (e: Exception) {
            when (e) {
                is CancellationException -> log.info("游戏结束!")
                is BoardGameException -> log.info("游戏中止!", e)
                else -> log.error("游戏过程中发生错误!", e)
            }
        }
        endGame()
    }

    /**
     * 向玩家发送提示信息
     * @param player
     * @param msg
     */
    fun sendAlert(player: P, msg: String) = this.sendAlert(player, msg, null)

    /**
     * 向玩家发送提示信息
     * @param player
     * @param msg
     * @param param
     */
    fun sendAlert(player: P, msg: String, param: Any?) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_TIP_ALERT, player.position).private("msg", msg).private("param", param).send(this, player)
    }

    /**
     * 向所有玩家发送提示信息
     * @param msg
     */
    fun sendAlertToAll(msg: String) = this.sendAlertToAll(msg, null)

    /**
     * 向所有玩家发送提示信息
     * @param msg
     * @param param
     */
    fun sendAlertToAll(msg: String, param: Any?) = this.players.forEach { this.sendAlert(it, msg, param) }

    /**
     * 发送动画效果的指令
     * @param param
     */
    fun sendAnimationResponse(param: AnimParam) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_ANIM_CMD, -1).public("animParam", param).send(this)
    }

    /**
     * 发送游戏结束的信息
     * @throws BoardGameException
     */
    fun sendGameEndResponse() {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_END, -1).send(this)
    }

    /**
     * 将游戏当前内容发送给玩家
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun sendGameInfo(receiver: P?)

    /**
     * 发送游戏开始的信息
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendGameStartResponse() {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_START, -1).send(this)
    }

    /**
     * 向客户端发送当前游戏的时间
     * @param receiver
     */
    fun sendGameTime(receiver: P?) {
        val sec = System.currentTimeMillis() - this.startTime.time
        val totalMinute = sec / (1000 * 60)
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_GAME_TIME, -1).public("hour", totalMinute / 60).public("minute", totalMinute % 60).send(this, receiver)
    }

    /**
     * 将初始信息传送给玩家
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun sendInitInfo(receiver: P?)

    /**
     * 向所有游戏中的玩家发送本地玩家的信息
     */
    fun sendLocalPlayerInfo(receiver: P?) {
        when (receiver) {
            null -> // 向所有游戏中的玩家发送本地玩家信息
                this.players.forEach(this::sendLocalPlayerInfoResponse)
            else -> // 向指定玩家发送本地玩家信息
                this.sendLocalPlayerInfoResponse(receiver)
        }
    }

    /**
     * 向指定玩家发送本地玩家的信息,只有在游戏中的玩家才会发送该信息
     * @param player
     */
    private fun sendLocalPlayerInfoResponse(player: P?) {
        if (player != null && this.isPlayingGame(player)) {
            CmdFactory.createGameResponse(CmdConst.GAME_CODE_LOCAL_PLAYER, player.position).public("localPlayer", player.toMap()).send(this, player)
        }
    }

    /**
     * 将信息发送给所有玩家
     * @param message
     */
    override fun sendMessage(message: Message) = this.room.sendMessage(message)

    /**
     * 将信息发送给玩家
     * @param message
     */
    override fun sendMessage(arg: P?, message: Message) = this.room.sendMessage(arg?.user, message)

    /**
     * 将玩家当前信息发送给玩家
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun sendPlayerPlayingInfo(receiver: P?)

    /**
     * 向指定玩家发送玩家状态的消息
     * @param playerStates
     * @param receiver
     */
    fun sendPlayerState(playerStates: Map<P, PlayerState>, receiver: P?) {
        val list = playerStates.map { (p, s) -> mapOf("userId" to p.user.id, "playerState" to s) }
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_PLAYER_STATE, -1).public("states", list).send(this, receiver)
    }

    /**
     * 发送游戏当前玩家信息给玩家
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendPlayingInfo() {
        this.sendGameTime(null)
        // 发送游戏的基本设置信息
        this.sendInitInfo(null)
        // 发送游戏的当前信息
        this.sendGameInfo(null)
        // 发送玩家的当前信息
        this.sendPlayerPlayingInfo(null)
    }

    /**
     * 发送游戏当前玩家信息给玩家
     * @param player
     * @throws BoardGameException
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(BoardGameException::class)
    fun sendPlayingInfo(player: Player) {
        // 发送玩家的座位信息
        val p = player as P
        this.sendLocalPlayerInfo(p)
        this.sendPlayerSitInfo(p)
        this.sendGameTime(p)
        // 发送游戏的基本设置信息
        this.sendInitInfo(p)
        // 发送游戏的当前信息
        this.sendGameInfo(p)
        // 发送玩家的当前信息
        this.sendPlayerPlayingInfo(p)
    }

    /**
     * 在重新连接游戏时发送的消息
     * @param player
     * @throws BoardGameException
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(BoardGameException::class)
    fun sendReconnectInfo(player: Player) {
        this.mode.currentListener?.sendReconnectResponse(player as P)
    }

    /**

     */
    fun sendRefreshListeningInfo(player: Player) {
        this.sendPlayingInfo(player)
        this.sendReconnectInfo(player)
    }

    /**
     * 将回应发送给所有玩家
     * @param res
     */
    override fun sendResponse(res: BgResponse) {
        this.currentResponse.add(res.privateString)
        this.room.sendResponse(res)
    }

    /**
     * 向玩家发送信息,如果玩家为空,则向所有玩家发送(包括旁观者)
     * @param arg
     * @param res
     */
    override fun sendResponse(arg: P?, res: BgResponse) = when (arg) {
        null -> this.sendResponse(res)
        else -> this.room.sendResponse(arg.user, res)
    }

    /**
     * 向指定玩家发送简单指令
     * @param key
     * @param value
     * @param receiver
     */
    fun sendSimpleResponse(key: String, value: Any, receiver: P?) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_SIMPLE_CMD, -1).public(key, value).send(this, receiver)
    }

    /**
     * 向指定玩家发送简单指令
     * @param subact
     * @param receiver
     */
    fun sendSimpleResponse(subact: String, receiver: P?) = this.sendSimpleResponse("subact", subact, receiver)

    /**
     * 设置游戏
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun setupGame()

    /**
     * 唤醒所有等待中的线程
     */
    fun wakeAll() {
        actor.close(BoardGameException("游戏中止!"))
        mode.wakeUp()
    }

    /**
     * 中盘结束游戏
     */
    fun winGame() {
        this.state = BgState.WIN
        this.wakeAll()
    }

}
