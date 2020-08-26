package com.f14.bg.hall

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.consts.GameType
import com.f14.F14bg.network.CmdFactory
import com.f14.F14bg.utils.PrivUtil
import com.f14.bg.BoardGame
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.action.ISendable
import com.f14.bg.action.ISendableWith
import com.f14.bg.chat.IChatable
import com.f14.bg.chat.IChatableWith
import com.f14.bg.chat.Message
import com.f14.bg.component.Convertable
import com.f14.bg.consts.BgState
import com.f14.bg.consts.PlayingState
import com.f14.bg.exception.BoardGameException
import com.f14.bg.player.Player
import com.f14.bg.utils.BgUtils
import com.f14.bg.utils.CheckUtils
import com.f14.f14bgdb.util.CodeUtil
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import org.apache.log4j.Logger
import java.util.*
import java.util.concurrent.Executors
import kotlin.properties.Delegates

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */

class GameRoom(
        val owner: User,
        val type: GameType,
        val name: String,
        val descr: String,
        private val password: String,
        private val bgId: String,
        val maxPlayerNumber: Int,
        val minPlayerNumber: Int,
        val isMatchMode: Boolean,
        val hall: GameHall
) : Convertable, ISendable, ISendableWith<User?>, IChatable, IChatableWith<User?> {
    private val log = Logger.getLogger(this.javaClass)
    val id: Int = RoomManager.generateRoomId()
    val game: BoardGame<*, *, *>
    var reportString: String? = null
    var responseString: List<Array<String>>? = null
    val users: MutableCollection<User> = LinkedHashSet()
    val joinUsers: MutableCollection<User> = LinkedHashSet()
    private val userParams = HashMap<Long, UserRoomParam>()
    var state: BgState by Delegates.observable(BgState.WAITING) { _, old, new ->
        if (new != old) this.onGamePropertyChange()
    }
    private var replaceUser: User? = null
    var job: Job? = null
    val context = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    init {
        try {
            this.game = CodeUtil.getBoardGameClass(bgId)!!.newInstance()
            this.game.init(this)
        } catch (e: Exception) {
            log.error("创建游戏实例时发生错误!", e)
            throw e
        }
    }

    /**
     * 判断用户是否可以重连到该房间
     * @param user
     * @return
     */
    fun canReconnect(user: User): Boolean = // 如果房间中的游戏存在,并且在进行中,并且玩家的状态为断线重,则可以重连
            this.isPlaying && this.joinUsers.contains(user) && this.getUserState(user) == PlayingState.LOST_CONNECTION

    /**
     * 检查玩家是否可以执行动作
     * @param user
     * @param playing
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkCanAction(user: User, playing: Boolean) {
        CheckUtils.check(this.getUserState(user) != PlayingState.PLAYING, "你不能执行动作!")
        when {
            playing -> CheckUtils.check(this.state != BgState.PLAYING, "游戏不在进行,不能执行此动作!")
            else -> CheckUtils.check(this.state == BgState.PLAYING, "游戏正在进行,不能执行此动作!")
        }
    }

    /**
     * 检查密码是否匹配
     * @param pwd
     * @return
     */
    fun invalidPassword(pwd: String): Boolean = this.hasPassword() && this.password != pwd

    /**
     * 检查是否可以开始游戏
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun checkStart() {
        CheckUtils.check(this.state != BgState.WAITING, "游戏状态错误,不能开始游戏!")
        CheckUtils.check(!this.isPlayersSuit, "玩家数量不正确,不能开始游戏!")
        CheckUtils.check(!this.isAllPlayersReady, "还有玩家没有准备好,不能开始游戏!")
        CheckUtils.check(this.isMatchMode and (this.reportString != null), "比赛房间只进行一场游戏!")
    }

    /**
     * 判断用户是否在该房间中
     * @param user
     * @return
     */
    fun containUser(user: User): Boolean = synchronized(this.users) {
        user in this.users
    }

    /**
     * 创建玩家对象
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun createPlayer(user: User): Player {
        try {
            val player = CodeUtil.getPlayerClass(bgId)!!.getConstructor(user.javaClass, this.javaClass).newInstance(user, this)
            if (this.isMatchMode) {
                val existsNames = this.joinUsers.mapNotNull { it.getPlayer<Player>(this)?.name }
                do {
                    player.name = "(${BgUtils.randomName()})"
                } while (player.name in existsNames)
            }
            return player
        } catch (e: Exception) {
            log.error("创建玩家实例时发生错误!", e)
            throw BoardGameException("创建玩家实例时发生错误!")
        }
    }

    /**
     * 为用户创建房间参数
     * @param user
     */
    private fun createUserParam(user: User) = synchronized(userParams) {
        UserRoomParam(user).also { userParams[user.id] = it }
    }

    /**
     * 执行换人操作
     * @param user
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doReplace(user: User) {
        when (val replaceUser = this.replaceUser) {
            null -> throw BoardGameException("没有玩家在等待换人!")
            user -> synchronized(this.users) {
                this.setUserState(user, PlayingState.PLAYING)
                this.setUserReady(user, false)
                this.refreshUser(user)
                this.sendReplaceEndResponse()
                // 向玩家发送游戏中的信息
                this.users.mapNotNull { it.getPlayer(this) }.forEach(this.game::sendRefreshListeningInfo)
            }
            else -> synchronized(this.users) {
                CheckUtils.check(this.isJoinGame(user), "你已经加入了游戏!")
                CheckUtils.check(this.getUserState(user) != PlayingState.AUDIENCE, "你不在旁观状态,不能加入游戏!")
                val player = replaceUser.getPlayer<Player>(this) ?: throw BoardGameException("未知错误!")
                val replacePlayer = user.getPlayer<Player>(this) ?: throw BoardGameException("未知错误!")
                val name = player.name
                val replaceName = replacePlayer.name
                replacePlayer.user = replaceUser
                player.user = user
                replacePlayer.name = name
                player.name = replaceName
                user.addPlayer(this, player)
                replaceUser.addPlayer(this, replacePlayer)
                this.setUserState(replaceUser, PlayingState.AUDIENCE)
                this.setUserState(user, PlayingState.PLAYING)
                this.setUserReady(user, false)
                this.joinUsers.remove(replaceUser)
                this.joinUsers.add(user)
                this.sendPlayerReplaceResponse(user, replaceUser)
                this.onGamePropertyChange()
                this.refreshUser(user)
                this.refreshUser(replaceUser)
                this.sendReplaceEndResponse()
                // 向玩家发送游戏中的信息
                this.users.mapNotNull { it.getPlayer(this) }.forEach(this.game::sendRefreshListeningInfo)

                this.game.report.info("[${player.name}] 替换 [${replacePlayer.name}]")
                this.sendReplaceMessage(replaceUser, user)
            }
        }

        this.state = BgState.PLAYING
        this.sendUserButtonResponse()
        this.replaceUser = null
    }

    /**
     * 结束游戏
     */
    fun endGame() {
        this.saveResponse(game.cachedResponse)
        // 将房间的状态设为等待中
        this.state = BgState.WAITING
        this.replaceUser = null
        this.sendUserButtonResponse()
        this.joinUsers.filter { this.getUserState(it) == PlayingState.LOST_CONNECTION }.forEach(this::leave)
    }

    /**
     * 取得加入游戏中的玩家数
     * @return
     */
    private val joinUserNumber: Int
        get() = this.joinUsers.size

    /**
     * 房间名字
     * @return
     */
    private val logName: String
        get() = "${this.name}(${this.type}#${this.id})"

    /**
     * 取得用户对应的map对象,包括用户状态信息
     * @param user
     * @return
     */
    private fun getUserMap(user: User): Map<String, Any> = user.toMap(this).let { if (containUser(user)) it + mapOf("userState" to getUserState(user)) else it }

    /**
     * 取得用户的房间参数
     * @param user
     * @return
     */
    private fun getUserParam(user: User): UserRoomParam = synchronized(userParams) {
        this.userParams.computeIfAbsent(user.id) { UserRoomParam(user) }
    }


    /**
     * 取得用户在房间中的状态
     * @param user
     */
    private fun getUserState(user: User): PlayingState {
        return this.getUserParam(user).playingState
    }

    /**
     * 判断房间是否有密码
     * @return
     */
    private fun hasPassword(): Boolean {
        return password.isNotEmpty()
    }

    /**
     * 判断是否所有玩家都准备好进行游戏
     * @return
     */
    private val isAllPlayersReady: Boolean
        get() = this.joinUsers.all(this::isUserReady)

    /**
     * 判断玩家是否加入房间中的游戏
     * @param user
     * @return
     */
    private fun isInGame(user: User): Boolean = when (this.getUserState(user)) {
        PlayingState.PLAYING, PlayingState.WAITING, PlayingState.LOST_CONNECTION -> true
        else -> false
    }

    /**
     * 判断玩家是否加入了房间中的游戏
     * @param user
     * @return
     */
    private fun isJoinGame(user: User): Boolean = this.joinUsers.contains(user)

    /**
     * 判断玩家数量是否已满
     * @return
     */
    private val isPlayerFull: Boolean
        get() = this.joinUserNumber >= this.maxPlayerNumber

    /**
     * 判断玩家数量是否适合游戏
     * @return
     */
    private val isPlayersSuit: Boolean
        get() = this.joinUserNumber in (this.minPlayerNumber..this.maxPlayerNumber)

    /**
     * 判断房间里的游戏是否在进行中
     * @return
     */
    val isPlaying: Boolean
        get() = this.state in arrayOf(BgState.PLAYING, BgState.PAUSE)

    /**
     * 判断玩家是否正在进行游戏
     * @param user
     * @return
     */
    fun isPlayingGame(user: User) = this.isPlaying && this.isInGame(user)

    /**
     * 判断玩家是否已经准备
     * @param user
     * @return
     */
    private fun isUserReady(user: User) = this.getUserParam(user).ready

    /**
     * 用户进入房间
     * @param user
     * @param password
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun join(user: User, password: String) {
        CheckUtils.check(this.containUser(user), "你已经在这个房间里了!")
        if (!PrivUtil.hasAdminPriv(user)) {
            CheckUtils.check(!this.isMatchMode and user.hasRoom(), "你已经在其他房间里了!")
            CheckUtils.check(this.invalidPassword(password), "密码错误,不能加入房间!")
        }
        synchronized(this.users) {
            this.users.add(user)
            // 为用户创建玩家对象
            val player = this.createPlayer(user)
            user.addPlayer(this, player)
            // 为用户创建房间参数,设置用户的状态为旁观
            this.createUserParam(user)

            this.onGamePropertyChange()
            this.hall.refreshUser(user)
            this.sendJoinRoomResponse(user)
            if (!this.isMatchMode) this.sendInfo(user, "进入了房间!")
        }
    }

    /**
     * 用户进入房间前的检查
     * @param user
     * @param password
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun joinCheck(user: User, password: String) {
        CheckUtils.check(this.containUser(user), "你已经在这个房间里了!")
        if (!PrivUtil.hasAdminPriv(user)) {
            CheckUtils.check(user.hasRoom(), "你已经在其他房间里了!")
            CheckUtils.check(this.invalidPassword(password), "密码错误,不能加入房间!")
        }
        // 检查通过后向客户端发送打开房间窗口的指令
        user.handler.sendOpenRoomResponse(this)
    }

    /**
     * 用户加入游戏
     * @param user
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun joinPlay(user: User) {
        CheckUtils.check(!this.containUser(user), "你不在这个房间里了!")
        when (this.state) {
            BgState.PAUSE -> this.doReplace(user)
            else -> {
                CheckUtils.check(this.isJoinGame(user), "你已经加入了游戏!")
                CheckUtils.check(this.getUserState(user) != PlayingState.AUDIENCE, "你不在旁观状态,不能加入游戏!")
                CheckUtils.check(this.isPlayerFull, "游戏中的玩家数量已满,不能加入游戏!")
                CheckUtils.check(this.state != BgState.WAITING, "房间状态错误,不能加入游戏!")
                // 设置用户的状态为进入游戏
                this.setUserState(user, PlayingState.PLAYING)
                this.setUserReady(user, false)
                this.joinUsers.add(user)

                this.onGamePropertyChange()
                this.refreshUser(user)
                this.sendJoinPlayResponse(user)
                this.sendUserButtonResponse(user)
                this.sendInfo(user, "加入了游戏!")
            }
        }
    }

    /**
     * 用户离开房间
     * @param user
     * @throws BoardGameException
     */
    fun leave(user: User) {
        if (this.containUser(user)) {
            // 如果玩家在游戏进行时退出,则需要将其移出游戏
            if (this.isPlayingGame(user)) when {
                user.getPlayer<Player>(this)?.let(this.game::removePlayerForce)
                        ?: false -> this.sendInfo(user, "强行离开了游戏!")
                else -> this.sendInfo(user, "离开了游戏!")
            }
            synchronized(this.users) {
                if (this.isInGame(user)) {
                    this.joinUsers.remove(user)
                    this.sendLeavePlayResponse(user)
                }
                this.users.remove(user)
                this.userParams.remove(user.id)
                // 将玩家移出房间
                user.removePlayer<Player>(this)
                // 如果移除玩家后房间里没有其他的用户了,则从大厅移除该房间
                when {
                    this.users.isEmpty() -> this.hall.removeGameRoom(this)
                    else -> {
                        if (!this.isMatchMode) this.sendInfo(user, "离开了房间!")
                        this.onGamePropertyChange()
                    }
                }

                this.hall.refreshUser(user)
                this.sendLeaveRoomResponse(user)
            }
        }
    }

    /**
     * 玩家强制退出房间
     * @param user
     */
    private fun leaveForce(user: User) {
        if (this.containUser(user)) this.leave(user)
        // 并关闭用户的房间窗口
        user.handler.closeRoomShell(this.id)
    }

    /**
     * 用户离开游戏
     * @param user
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun leavePlay(user: User) {
        CheckUtils.check(!this.containUser(user), "你不在这个房间里了!")
        CheckUtils.check(this.isPlaying, "游戏正在进行中,不能离开游戏!")
        CheckUtils.check(!this.isJoinGame(user), "你还没有加入游戏!")
        // 设置用户的状态为进入游戏
        this.setUserState(user, PlayingState.AUDIENCE)
        this.joinUsers.remove(user)

        this.onGamePropertyChange()
        this.refreshUser(user)
        this.sendLeavePlayResponse(user)
        this.sendUserButtonResponse(user)
        this.sendInfo(user, "离开了游戏!")
    }

    /**
     * 玩家退出房间的请求
     * @throws BoardGameException
     */
    private fun leaveRequest(user: User) = when {
        this.containUser(user) -> when {
            !this.isPlayingGame(user) || user.getPlayer<Player>(this)?.let(this.game::canLeave) ?: false -> {
                // 否则就直接从房间移除玩家
                this.leave(user)
                // 并关闭用户的房间窗口
                user.handler.closeRoomShell(this.id)
            }
        // 如果玩家正在进行游戏中,则提示用户是否强制退出游戏
            else -> CmdFactory.createClientResponse(CmdConst.CLIENT_LEAVE_ROOM_CONFIRM).public("roomId", this.id).send(this, user)
        }
        else -> user.handler.closeRoomShell(this.id)
    }

    /**
     * 用户读取房间信息
     * @param user
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun loadRoomInfo(user: User) {
        // 读取时,如果用户还未加入房间,则自动加入
        if (!this.containUser(user)) this.join(user, this.password)
        this.sendRoomInfo(user)
        this.sendUserInfo(user)
        // 向玩家发送游戏设置
        this.sendConfig(user)
        if (this.isPlaying) {
            val player = user.getPlayer<Player>(this) ?: throw BoardGameException("未知错误!")
            // 如果游戏正在进行中,则需要向玩家发送游戏中的信息
            this.game.sendPlayingInfo(player)
            // 发送最近的战报信息
            this.game.report.sendRecentMessages(player)
            // 如果玩家是断线重连的,则发送断线重连的信息
            if (this.canReconnect(user)) {
                this.game.sendReconnectInfo(player)
                // 并刷新用户的状态
                when (user) {
                    this.replaceUser -> {
                        this.setUserState(user, PlayingState.WAITING)
                        this.setUserReady(user, true)
                    }
                    else -> {
                        this.setUserState(user, PlayingState.PLAYING)
                        this.setUserReady(user, false)
                    }
                }
                this.refreshUser(user)
            }
            this.sendUserButtonResponse(user)
        }
    }

    /**
     * 用户断线,返回是否被移出游戏
     * @param user
     * @throws BoardGameException
     */
    fun lostConnect(user: User): Boolean {
        return this.containUser(user) and when {
            this.isPlayingGame(user) -> {
                // 如果游戏正在进行中,并且玩家在游戏中
                // 则将其状态设置为断线
                this.setUserState(user, PlayingState.LOST_CONNECTION)
                // 刷新用户的状态
                this.refreshUser(user)
                true
            }
            else -> {
                this.leave(user)
                false
            }
        }
    }

    /**
     * 游戏状态变化时触发的事件
     */
    fun onGamePropertyChange() = // 将变化后的房间属性发送到大厅的玩家
            this.hall.sendRoomChangeResponse(this)

    /**
     * 处理聊天类型的行动
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun processChatAction(act: GameAction) {
        CheckUtils.check(this.isMatchMode && this.reportString == null, "比赛模式不允许聊天!")
        val user = act.user
        val msg = act.getAsString("msg")
        if (msg.isNotEmpty()) {
            // 作弊代码
            if (msg.startsWith("$#") && this.game.doCheat(msg.substring(2))) return
            // 现阶段只按照玩家所处的位置来发送消息
            // 将消息发送到所在的房间
            Message(msg, BgUtils.escapeHtml(user.name), user.loginName).send(this)
        }
    }

    /**
     * 处理房间内的指令
     * @param act
     */
    @Throws(BoardGameException::class)
    @Synchronized
    fun processCommand(act: GameAction) {
        when (act.type) {
            CmdConst.SYSTEM_CMD -> this.processSystemAction(act)
            CmdConst.CHAT_CMD -> this.processChatAction(act)
            CmdConst.GAME_CMD -> this.processGameAction(act)
        }

    }

    /**
     * 处理游戏类型的行动
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun processGameAction(act: GameAction) {
        when (act.code) {
        // 设置游戏配置
            CmdConst.GAME_CODE_SET_CONFIG -> this.setConfig(act)
            CmdConst.GAME_CODE_LOAD_REPORT -> this.sendReport(act.user)
            else -> {
                this.checkCanAction(act.user, true)
                game.doAction(act)
            }
        }
    }

    private fun setConfig(act: GameAction) {
        this.checkCanAction(act.user, false)
        this.game.setConfig(act)
        this.sendConfig()

    }

    /**
     * 处理系统类型的行动
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun processSystemAction(act: GameAction) {
        val user = act.user
        when (act.code) {
            CmdConst.SYSTEM_CODE_LOAD_ROOM_INFO -> this.loadRoomInfo(user)// 用户读取房间信息
            CmdConst.SYSTEM_CODE_PLAYER_LIST -> this.sendUserList(user) // 刷新玩家列表
            CmdConst.SYSTEM_CODE_ROOM_JOIN_PLAY -> this.joinPlay(user) // 加入游戏
            CmdConst.SYSTEM_CODE_ROOM_LEAVE_PLAY -> this.leavePlay(user) // 离开游戏
            CmdConst.SYSTEM_CODE_USER_READY -> this.ready(user) // 用户准备
            CmdConst.SYSTEM_CODE_USER_START -> this.startGame(user) // 用户开始游戏
            CmdConst.SYSTEM_CODE_ROOM_INVITE_NOTIFY -> this.sendRoomInviteNotify(user) // 发送房间邀请通知
            CmdConst.SYSTEM_CODE_ROOM_LEAVE_REQUEST -> this.leaveRequest(user) // 退出房间的请求
            CmdConst.SYSTEM_CODE_ROOM_REPLACE_REQUEST -> this.replace(user) // 退出房间的请求
            CmdConst.SYSTEM_CODE_ROOM_LEAVE -> this.leaveForce(user) // 用户强制退出房间
            CmdConst.SYSTEM_CODE_JOIN_CHECK -> this.joinCheck(user, act.getAsString("password")) // 进入房间前的检查
            CmdConst.SYSTEM_CODE_SAVE_REPLAY -> this.saveReplay(user)
            else -> throw BoardGameException("无效的指令代码!")
        }
    }

    private fun saveReplay(user: User) {
        this.responseString?.let { responseString ->
            CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_REPLAY_START, -1).public("gameType", this.type.toString()).send(this, user)
            for (strs in responseString) {
                CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_SAVE_REPLAY, -1).public("response", strs).send(this, user)
            }
            CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_REPLAY_END, -1).send(this, user)
        }
    }

    /**
     * 玩家准备
     * @param user
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun ready(user: User) {
        this.checkCanAction(user, false)
        this.setUserReady(user, !this.isUserReady(user))
        this.sendUserReadyResponse(user)
        // 检查是否所有的玩家都准备了,如果是,则尝试直接开始游戏
        try {
            this.startGame(user)
        } catch (ignored: Exception) {
        }
    }

    /**
     * 刷新房间中用户的状态
     * @param user
     */
    fun refreshUser(user: User) = this.sendRoomUserResponse(user, CmdConst.SYSTEM_CODE_ROOM_REFRESH_USER)

    /**
     * 换人
     * @param user
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun replace(user: User) {
        CheckUtils.check(this.isMatchMode, "比赛模式不允许换人!")
        CheckUtils.check(!this.containUser(user), "用户不在这个房间内!")
        CheckUtils.check(this.replaceUser != null, "已经有玩家在等待换人!")
        CheckUtils.check(this.state == BgState.PAUSE, "已经有玩家在等待换人")
        this.replaceUser = user
        this.setUserState(user, PlayingState.WAITING)
        this.setUserReady(user, true)
        this.sendUserReadyResponse(user)
        this.state = BgState.PAUSE
        this.sendUserButtonResponse()
        this.sendReplaceStartResponse()
        if (this.hall.checkSendNotifyTime(this)) {
            this.hall.sendWaitReplayRoomNotify(user, this)
        }
    }

    /**
     * 向所有玩家发送游戏配置
     */
    fun sendConfig() = this.game.createConfigResponse().send(this)

    /**
     * 向玩家发送游戏配置
     * @param user
     */
    fun sendConfig(user: User) = this.game.createConfigResponse().send(this, user)

    /**
     * @param user
     * @param string
     */
    private fun sendInfo(user: User, string: String) {
        Message(" [${BgUtils.escapeHtml(user.getPlayer<Player>(this)?.name?: user.name)}] $string")
                .send(this)
    }

    /**
     * 发送玩家加入游戏的信息
     * @param user
     */
    private fun sendJoinPlayResponse(user: User) = this.sendRoomUserResponse(user, CmdConst.SYSTEM_CODE_ROOM_JOIN_PLAY)

    /**
     * 发送玩家加入房间的信息
     * @param user
     */
    private fun sendJoinRoomResponse(user: User) = this.sendRoomUserResponse(user, CmdConst.SYSTEM_CODE_ROOM_JOIN)

    /**
     * 发送玩家离开游戏的信息
     * @param user
     */
    private fun sendLeavePlayResponse(user: User) = this.sendRoomUserResponse(user, CmdConst.SYSTEM_CODE_ROOM_LEAVE_PLAY)

    /**
     * 发送玩家离开房间的信息
     * @param user
     */
    private fun sendLeaveRoomResponse(user: User) = this.sendRoomUserResponse(user, CmdConst.SYSTEM_CODE_ROOM_LEAVE)

    /**
     * 给房间内的所有用户发送消息
     * @param message
     */
    override fun sendMessage(message: Message) = message.send(this.users, this.id)

    /**
     * 向用户发送消息,如果u为空,则向所有用户发送
     * @param arg
     * @param message
     */
    override fun sendMessage(arg: User?, message: Message) = when (arg) {
        null -> this.sendMessage(message)
        else -> arg.sendMessage(this.id, message)
    }

    /**
     * 发送玩家换人的信息
     * @param user
     */
    private fun sendPlayerReplaceResponse(user: User, replaceUser: User) {
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_REPLACE, -1)
                .public("user", this.getUserMap(user))
                .public("replaceUser", this.getUserMap(replaceUser))
                .send(this)
    }

    /**
     * 发送换人开始的信息
     */
    fun sendReplaceStartResponse() {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_REPLACE_START, -1).send(this)
    }

    /**
     * 发送换人结束的信息
     */
    fun sendReplaceEndResponse() {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_REPLACE_END, -1).send(this)
    }

    /**
     * @param replaceUser
     * @param user
     */
    private fun sendReplaceMessage(replaceUser: User, user: User) {
        Message(" [${BgUtils.escapeHtml(user.name)}] 替换 [${BgUtils.escapeHtml(replaceUser.name)}]").send(this)
    }

    /**
     * 发送战报信息
     */
    fun sendReport(user: User? = null) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_LOAD_REPORT, -1)
                .public("reportString", GsonBuilder().create().fromJson(this.reportString ?: return, Any::class.java))
                .send(this, user)
    }

    /**
     * 给房间内的所有用户发送指令
     * @param res
     */
    override fun sendResponse(res: BgResponse) = synchronized(this.users) {
        res.send(this.users, this.id)
    }

    /**
     * 向用户发送指令,如果u为空,则向所有用户发送
     * @param arg
     * @param res
     */
    override fun sendResponse(arg: User?, res: BgResponse) = when (arg) {
        null -> this.sendResponse(res)
        else -> arg.sendResponse(this.id, res)
    }

    /**
     * 向用户发送用户状态和房间的基本信息
     * @param user
     */
    private fun sendRoomInfo(user: User) {
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_USER_ROOM_INFO, -1).public("userState", this.getUserState(user)).public("room", this.toMap()).send(this, user)
    }

    /**
     * 发送房间邀请的通知
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun sendRoomInviteNotify(user: User) {
        CheckUtils.check(!this.containUser(user), "用户不在指定的房间内!")
        CheckUtils.check(this.isPlaying, "游戏已经开始,不能发送邀请!")
        CheckUtils.check(!this.hall.checkSendNotifyTime(user) || !this.hall.checkSendNotifyTime(this), "每分钟只允许发送1次通知!")
        this.hall.sendCreateRoomNotify(user, this)
    }

    /**
     * 发送房间中用户操作相关的信息
     * @param user
     * @param code 操作代码
     */
    private fun sendRoomUserResponse(user: User, code: Int) {
        CmdFactory.createSystemResponse(code, -1).public("user", this.getUserMap(user)).send(this)
    }

    /**
     * 向所有用户发送按键状态变化的信息
     */
    fun sendUserButtonResponse() = synchronized(this.users) {
        users.forEach { this.sendUserButtonResponse(it) }
    }

    /**
     * 向指定用户发送按键状态变化的信息
     * @param user
     */
    fun sendUserButtonResponse(user: User) {
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_USER_BUTTON, -1).public("userState", this.getUserState(user)).public("roomState", this.state).send(this, user)
    }

    /**
     * 向用户发送房间中所有用户的列表,以及加入游戏的用户和准备状态
     * @param user
     */
    private fun sendUserInfo(user: User) = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_USER_LIST_INFO, -1).public("joinUsers", joinUsers.map { it.toMap(this) + mapOf("ready" to isUserReady(it)) }).public("users", users.map { it.toMap(this) + mapOf("userState" to getUserState(it)) }).send(this, user)

    /**
     * 发送房间中所有用户列表
     * @param user
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendUserList(user: User) {
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_PLAYER_LIST, -1).public("users", this.users.map { it.toMap(this) }).send(user, this.id)
    }

    /**
     * 向所有用户发送用户准备状态变化的信息
     * @param user
     */
    private fun sendUserReadyResponse(user: User) {
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_USER_READY, -1).public("user", this.getUserMap(user)).public("ready", this.isUserReady(user)).send(this)
    }

    /**
     * 保存战报信息
     */
    fun setReport() {
        this.reportString = this.game.report.toJSONString()
    }

    /**
     * 设置玩家的准备状态
     * @param user
     */
    private fun setUserReady(user: User, ready: Boolean) {
        this.getUserParam(user).ready = ready
    }

    /**
     * 设置用户在房间中的状态
     * @param user
     * @param state
     */
    private fun setUserState(user: User, state: PlayingState) {
        this.getUserParam(user).playingState = state
    }

    /**
     * 开始游戏
     * @param user
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun startGame(user: User) {
        this.checkCanAction(user, false)
        this.checkStart()
        // 将所有玩家的准备状态设为未准备
        // 将所有玩家添加到游戏中,并且开始游戏
        this.joinUsers.forEach {
            this.game.joinGame(it.getPlayer(this) ?: throw BoardGameException("未知错误!"))
            this.setUserReady(it, false)
            this.sendUserReadyResponse(it)
        }
        // 将房间的状态设为游戏中
        this.state = BgState.PLAYING
        this.sendUserButtonResponse()
        // 创建游戏线程
        this.job = GlobalScope.launch(context) {
            game.run()
            endGame()
        }
    }

    // 保存录像
    fun saveResponse(cachedResponse: MutableList<Array<String>>) {
        this.responseString = cachedResponse
    }

    override fun toMap(): Map<String, Any> = mapOf(
            "id" to this.id,
            "name" to this.name,
            "gameType" to this.type,
            "state" to this.state,
            "players" to "${this.joinUserNumber}/${this.users.size}",
            "password" to this.hasPassword(),
            "descr" to this.descr
    )

    /**
     * 用户在房间中的参数
     * @author F14eagle
     */
    private data class UserRoomParam(var user: User) {
        var ready = false
        var playingState = PlayingState.AUDIENCE
    }

    fun close() = this.job?.cancel()

}
