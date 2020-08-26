package com.f14.bg.hall

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.consts.GameType
import com.f14.F14bg.manager.ResourceManager
import com.f14.F14bg.network.CmdFactory
import com.f14.F14bg.utils.ConsoleUtil
import com.f14.F14bg.utils.PrivUtil
import com.f14.F14bg.utils.ResourceUtils
import com.f14.F14bg.utils.UpdateUtil
import com.f14.bg.action.BgAction.HallAction
import com.f14.bg.action.BgResponse
import com.f14.bg.action.ISendable
import com.f14.bg.chat.IChatable
import com.f14.bg.chat.Message
import com.f14.bg.consts.NotifyType
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.bg.utils.CheckUtils
import com.f14.f14bgdb.F14bgdb
import com.f14.f14bgdb.model.RankingListModel
import com.f14.f14bgdb.service.RankingManager
import com.f14.f14bgdb.service.UserManager
import com.f14.f14bgdb.util.CodeUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.apache.log4j.Logger
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */

class GameHall : ISendable, IChatable {
    private val log = Logger.getLogger(this.javaClass)
    /**
     * 所有房间
     */
    private val rooms: MutableMap<Int, GameRoom> = LinkedHashMap()
    /**
     * 在线用户
     */
    private val users: MutableMap<String, User> = LinkedHashMap()
    /**
     * 掉线用户
     */
    private val recentUsers: MutableMap<String, User> = LinkedHashMap()
    /**
     * 最后一次发送通知的时间（房间）
     */
    private val lastNotifyTimesRoom: MutableMap<Int, Long> = HashMap()
    /**
     * 最后一次发送通知的时间（用户）
     */
    private val lastNotifyTimesUser: MutableMap<Long, Long> = HashMap()

    /**
     * 处理聊天类型的行动
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun processChatAction(act: HallAction) {
        val user = act.user
        val msg = act.getAsString("msg")
        if (msg.isNotEmpty()) {
            when {
                ConsoleUtil.isConsoleCommand(msg) -> // 如果是控制台指令则执行该指令
                    ConsoleUtil.processConsoleCommand(user, msg)
                else -> // 发送消息到大厅
                    Message(msg, BgUtils.escapeHtml(user.name), user.loginName).send(this)
            }
        }
    }

    /**
     * 装载游戏资源
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun initResource(act: HallAction) {
        val gameType = act.getAsString("gameType")
        val rm = ResourceUtils.getResourceManager<ResourceManager>(gameType) ?: throw BoardGameException("装载游戏资源失败!")
        rm.sendResourceInfo(act.user)
    }

    /**
     * 刷新用户的积分信息
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun refreshUserRanking(act: HallAction) {
        // this.checkLogin();
        val userId = act.getAsLong("userId")
        val um = F14bgdb.getBean<UserManager>("userManager")
        val user = um[userId] ?: throw BoardGameException("找不到指定用户!")
        val rm = F14bgdb.getBean<RankingManager>("rankingManager")
        val list = rm.queryUserRanking(userId)
        CmdFactory.createClientResponse(CmdConst.CLIENT_USER_INFO).public("username", user.userName).public("list", list).send(act.user.handler)
    }

    /**
     * 读取系统代码
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun loadCodeDetail(act: HallAction) {
        val codes = CodeUtil.allCodes
        CmdFactory.createClientResponse(CmdConst.CLIENT_LOAD_CODE).public("codes", codes).send(act.user)
    }

    /**
     * 检查模块是否需要更新
     * @param act
     */
    private fun checkUpdate(act: HallAction) {
        val gameType = act.getAsString("gameType")
        val versionString = act.getAsString("versionString")
        // 然后检查游戏文件的更新情况
        val files = UpdateUtil.getUpdateList(gameType, versionString)
        // 发送更新文件列表的信息
        CmdFactory.createClientResponse(CmdConst.CLIENT_CHECK_UPDATE).public("gameType", gameType).public("files", files.joinToString(",")).public("versionString", UpdateUtil.getVersionString(gameType)).send(act.user)
    }

    /**
     * 处理客户端类型的行动
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun processClientAction(act: HallAction) = when (act.code) {
        CmdConst.CLIENT_INIT_RESOURCE -> this.initResource(act) // 装载游戏资源
        CmdConst.CLIENT_LOAD_CODE -> this.loadCodeDetail(act) // 读取系统代码
        CmdConst.CLIENT_CHECK_UPDATE -> this.checkUpdate(act) // 检查模块是否需要更新
        CmdConst.CLIENT_USER_INFO -> this.refreshUserRanking(act) // 查看用户信息
        else -> throw BoardGameException("无效的指令代码!")
    }

    /**
     * 创建房间
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun createRoom(act: HallAction) {
        val user = act.user
        val name = act.getAsString("name")
        val gameType = act.getAsString("gameType")
        val password = act.getAsString("password")
        val descr = act.getAsString("descr")
        val isMatchMode = name.startsWith("M#")
        val room = createGameRoom(user, gameType, name, descr, password, isMatchMode)

        // 创建完成直接加入房间并进入游戏
        room.join(user, password)
        room.joinPlay(user)
        // 通知客户端打开房间窗口
        // 检查通过后向客户端发送打开房间窗口的指令
        user.handler.sendOpenRoomResponse(room)

        // 给所有空闲的玩家发送游戏创建的提示信息,设置密码的房间不发送该通知
        if (password.isEmpty() && this.checkSendNotifyTime(user)) this.sendCreateRoomNotify(user, room)
    }

    /**
     * 刷新排行榜信息
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun refreshRankingList(act: HallAction) {
        val boardGameId = act.getAsString("boardGameId")
        val rm = F14bgdb.getBean<RankingManager>("rankingManager")
        val condition = RankingListModel().also { it.boardGameId = boardGameId }
        val list = rm.queryRankingList(condition)
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_RANKING_LIST, -1).public("list", list).send(act.user)
    }

    /**
     * 玩家强制退出房间
     * @param act
     * @throws BoardGameException
     */
    private fun leaveForce(act: HallAction) {
        val user = act.user
        val roomId = act.getAsInt("roomId")
        val room = this.getGameRoom(roomId) ?: throw BoardGameException("没有找到指定的房间!")
        room.leave(user)
        // 并关闭用户的房间窗口
        user.handler.closeRoomShell(roomId)
    }

    /**
     * 检查玩家是否需要断线重连
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun reconnectCheck(act: HallAction) {
        val user = act.user
        user.roomIds.mapNotNull(this::getGameRoom).filter { it.canReconnect(user) }.forEach(user.handler::sendOpenRoomResponse)
    }

    private fun processSystemAction(act: HallAction) {
        // 发送大厅用户列表
        when (act.code) {
            CmdConst.SYSTEM_CODE_USER_INFO -> this.loadUserInfo(act) // 读取本地用户信息
            CmdConst.SYSTEM_CODE_ROOM_LIST -> this.sendRoomList(act) // 刷新房间列表
            CmdConst.SYSTEM_CODE_PLAYER_LIST -> this.sendUserList(act) // 刷新玩家列表
            CmdConst.SYSTEM_CODE_CREATE_ROOM -> this.createRoom(act) // 创建房间
            CmdConst.SYSTEM_CODE_RANKING_LIST -> this.refreshRankingList(act) // 刷新排行榜
            CmdConst.SYSTEM_CODE_JOIN_CHECK -> this.joinCheck(act) // 进入房间前的检查
            CmdConst.SYSTEM_CODE_ROOM_LEAVE -> this.leaveForce(act) // 用户强制退出房间
            CmdConst.SYSTEM_CODE_RECONNECT -> this.reconnectCheck(act) // 检查用户是否需要断线重连
            CmdConst.SYSTEM_CODE_ROOM_LEAVE_REQUEST -> this.leaveRequest(act) // 退出房间的请求
            CmdConst.SYSTEM_CODE_HALL_NOTICE -> this.sendHallNotice(act) // 检查是否发送大厅的公告信息
            else -> throw BoardGameException("无效的指令代码!" + act.code)
        }
    }

    private fun joinCheck(act: HallAction) {
        val id = act.getAsInt("id")
        val room = this.getGameRoom(id) ?: throw BoardGameException("没有找到指定的房间!")
        val password = act.getAsString("password")
        room.joinCheck(act.user, password)
    }

    /**
     * 发送大厅公告
     */
    private fun sendHallNotice(act: HallAction) {
        CmdFactory.createClientResponse(CmdConst.CLIENT_HALL_NOTICE).send(act.user)
    }

    /**
     * 处理游戏类型的行动
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun processGameAction(act: HallAction) {
        // 首先需要检查是否登录用户
        when (act.code) {
            CmdConst.GAME_CODE_JOIN -> this.joinGame(act)
        }
    }


    /**
     * 加入游戏
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun joinGame(act: HallAction) {
        val id = act.getAsInt("id")
        val room = this.getGameRoom(id) ?: throw BoardGameException("没有找到指定的房间!")
        val password = act.getAsString("password")
        room.join(act.user, password)
    }

    /**
     * 玩家退出房间的请求
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun leaveRequest(act: HallAction) {
        val user = act.user
        val roomId = act.getAsInt("roomId")
        val room = this.getGameRoom(roomId)
        // 如果玩家正在进行游戏中,则提示用户是否强制退出游戏
        when {
            room == null || !room.containUser(user) -> user.handler.closeRoomShell(roomId)
            room.isPlayingGame(user) -> CmdFactory.createClientResponse(CmdConst.CLIENT_LEAVE_ROOM_CONFIRM).public("roomId", roomId).send(user)
            else -> {
                // 否则就直接从房间移除玩家
                room.leave(user)
                // 并关闭用户的房间窗口
                user.handler.closeRoomShell(roomId)
            }
        }
    }

    /**
     * 读取本地用户信息
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun loadUserInfo(act: HallAction) {
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_USER_INFO, -1).public("user", act.user.toMap()).send(act.user)
    }

    /**
     * 将user添加到最近登录过的用户列表中
     */
    fun addRecentUser(user: User) {
        synchronized(recentUsers) {
            this.recentUsers.put(user.loginName, user)
        }
    }

    /**
     * 添加用户到大厅中
     * @param user
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun addUser(user: User) {
        synchronized(users) {
            this.users.put(user.loginName, user)
        }
        // 将用户加入大厅的消息发送给所有大厅中的用户
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_JOIN_HALL, -1).public("user", user.toMap()).send(this)
    }

    /**
     * 向大厅中的所有玩家广播消息
     * @param user
     * @param message
     */
    fun broadcast(user: User, message: String) {
        CmdFactory.createClientResponse(CmdConst.CLIENT_BROADCAST).public("user", user.name).public("message", message).send(this)
    }

    /**
     * 检查目标是否允许发送通知
     * @return
     */
    fun checkSendNotifyTime(room: GameRoom): Boolean {
        synchronized(lastNotifyTimesRoom) {
            val time = this.lastNotifyTimesRoom[room.id] ?: return true
            return System.currentTimeMillis() - time >= NOTIFY_GAP
        }
    }

    /**
     * 检查目标是否允许发送通知
     * @return
     */
    fun checkSendNotifyTime(user: User): Boolean {
        synchronized(lastNotifyTimesUser) {
            val time = this.lastNotifyTimesUser[user.id] ?: return true
            return System.currentTimeMillis() - time >= NOTIFY_GAP
        }
    }

    /**
     * 创建游戏房间
     * @param user     创建的玩家
     * @param gameType
     * @param name     房间名称
     * @param descr    房间描述
     * @param password
     */

    @Throws(BoardGameException::class)
    fun createGameRoom(user: User, gameType: String, name: String, descr: String, password: String, isMatchMode: Boolean): GameRoom {
        synchronized(rooms) {
            CheckUtils.check(rooms.size >= MAX_ROOM, "房间已满,不能创建房间!")
            CheckUtils.check(user.roomIds.size >= PLAYER_ROOM_LIMIT, "你不能再创建更多的房间了!")
            CheckUtils.check(!PrivUtil.hasAdminPriv(user) and user.hasRoom(), "你已经在其他房间中,不能创建房间!")
            val type = GameType.valueOf(gameType)
            val bg = CodeUtil.getBoardGame(gameType) ?: throw BoardGameException("未知找到指定的游戏信息!")
            val room = GameRoom(user, type, name, descr, password, bg.id!!, bg.maxPlayerNumber!!, bg.minPlayerNumber!!, isMatchMode, this)
            // 创建房间中的游戏,并设置允许的人数
            rooms[room.id] = room
            // 发送创建房间的消息到大厅的玩家
            CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_ADDED, -1).public("room", room.toMap()).send(this)
            return room
        }
    }

    /**
     * 按照id取得游戏实例
     * @param id
     * @return
     */
    fun getGameRoom(id: Int): GameRoom? = synchronized(rooms) {
        return rooms[id]
    }

    /**
     * 取得所有游戏房间的列表
     * @return
     */

    val gameRoomList: Collection<GameRoom>
        get() = synchronized(rooms) {
            return this.rooms.values.toList()
        }

    /**
     * 按照loginName取得最近登录过的用户对象
     * @param loginName
     * @return
     */
    fun getRecentUser(loginName: String): User? {
        synchronized(recentUsers) {
            return this.recentUsers[loginName]
        }
    }

    /**
     * 按照登录名取得登录的用户
     * @param loginName
     * @return
     */
    fun getUser(loginName: String): User? {
        synchronized(users) {
            return this.users[loginName]
        }
    }

    /**
     * 取得所有在大厅中的用户
     * @return
     */
    fun getUsers(): Collection<User> {
        synchronized(users) {
            return this.users.values.toList()
        }
    }

    /**
     * 用户失去连接
     * @param user
     */
    fun lostConnect(user: User) {
        this.addRecentUser(user)
        user.disconnectJob = GlobalScope.async {
            delay(TIME_OUT)
            synchronized(recentUsers) {
                if (recentUsers.containsKey(user.loginName)) {
                    removeRecentUser(user.loginName)
                    // 如果玩家在游戏中,则将其从游戏中移除
                    user.roomIds.mapNotNull(this@GameHall::getGameRoom).forEach { it.leave(user) }
                }
            }
        }
    }

    /**
     * 用户重新连接
     * @param user
     */
    fun reconnect(user: User) {
        this.removeRecentUser(user.loginName)
        user.disconnectJob?.cancel()
        user.disconnectJob = null
    }

    /**
     * 刷新目标的最近一次发送通知的时间
     */
    private fun refreshSendNotifyTime(room: GameRoom) {
        synchronized(lastNotifyTimesRoom) {
            this.lastNotifyTimesRoom.put(room.id, System.currentTimeMillis())
        }
    }

    /**
     * 刷新目标的最近一次发送通知的时间
     */
    private fun refreshSendNotifyTime(user: User) {
        synchronized(lastNotifyTimesUser) {
            this.lastNotifyTimesUser.put(user.id, System.currentTimeMillis())
        }
    }

    /**
     * 刷新大厅中用户的信息
     * @param user
     */
    fun refreshUser(user: User) {
        // 将用户加入大厅的消息发送给所有大厅中的用户
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_HALL_REFRESH_USER, -1).public("user", user.toMap()).send(this)
    }

    /**
     * 移除游戏房间
     */
    fun removeGameRoom(room: GameRoom) {
        synchronized(rooms) {
            room.close()
            rooms.remove(room.id)
            // 发送移除房间的消息到大厅的玩家
            CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_REMOVED, -1).public("roomId", room.id).send(this)
        }
    }

    /**
     * 按照loginName移除最近登录过的用户对象
     * @param loginName
     */
    private fun removeRecentUser(loginName: String) {
        synchronized(recentUsers) {
            this.recentUsers.remove(loginName)
        }
    }

    /**
     * 将用户从大厅中移除
     * @param user
     */
    fun removeUser(user: User) {
        synchronized(users) {
            this.users.remove(user.loginName)
        }
        // 将用户离开大厅的消息发送给所有大厅中的用户
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_LEAVE_HALL, -1).public("user", user.toMap()).send(this)
    }

    /**
     * 发送创建房间的通知
     * @param sender
     * @param room
     */
    fun sendCreateRoomNotify(sender: User, room: GameRoom) {
        val message = "[${room.type}][${room.name}]等待玩家加入,${room.descr}"
        this.sendNotify(sender, room, message)
    }

    /**
     * 给在大厅中不在房间里的所有玩家发送消息
     * @param message
     */
    override fun sendMessage(message: Message) {
        message.send(this.getUsers())
    }

    /**
     * 将回应发送给大厅内不在房间里的所有玩家
     * @param res
     */
    override fun sendResponse(res: BgResponse) {
        res.send(this.getUsers())
    }

    /**
     * 向大厅中的用户发送房间属性变化的消息
     * @param room
     */
    fun sendRoomChangeResponse(room: GameRoom) {
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_CHANGED, -1).public("room", room.toMap()).send(this)
    }

    /**
     * 发送房间列表
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendRoomList(act: HallAction) {
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_LIST, -1).public("rooms", BgUtils.toMapList(this.gameRoomList)).send(act.user)
    }

    /**
     * 发送大厅中所有用户列表
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendUserList(act: HallAction) {
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_PLAYER_LIST, -1).public("users", BgUtils.toMapList(this.getUsers())).send(act.user)
    }

    /**
     * 发送等待换人的通知
     * @param sender
     * @param room
     */
    fun sendWaitReplayRoomNotify(sender: User, room: GameRoom) {
        val message = "[${room.type}][${room.name}]等待换人,${room.descr}"
        this.sendNotify(sender, room, message)
    }


    /**
     * 发送通知
     * @param sender
     * @param room
     * @param message
     */
    private fun sendNotify(sender: User, room: GameRoom, message: String) {
        CmdFactory.createClientNotifyResponse(CmdConst.CLIENT_BUBBLE_NOTIFY, NotifyType.CREATE_ROOM).public("roomId", room.id).public("message", message).public("gameType", room.type).send(this.getUsers()
                // 只能向所有不在进行游戏,并且同意接受该类型通知的玩家发送该通知
                .filter(User::canSendCreateRoomNotify)
                // 也不用给自己发
                .filterNot { sender == it }
                // 如果已经在该房间的也不用发
                .filterNot(room::containUser))
        // 记录最后一次发送通知的时间
        this.refreshSendNotifyTime(sender)
        this.refreshSendNotifyTime(room)
    }

    /**
     * 处理用户掉线
     * @param user
     */
    fun userDisconnect(user: User) {
        // 如果用户已经登陆,则处理断开
        val lostConnect = user.roomIds.mapNotNull(this::getGameRoom)
                // 如果玩家正在进行游戏,则将用户保存到最近用户列表中
                .count { it.lostConnect(user) }
        if (lostConnect > 0) this.lostConnect(user)
    }

    /**
     * 处理指令
     * @param act
     */
    @Throws(BoardGameException::class)
    @Synchronized
    fun processCommand(act: HallAction) {
        when (act.type) {
            CmdConst.SYSTEM_CMD -> processSystemAction(act)
            CmdConst.CHAT_CMD -> processChatAction(act)
            CmdConst.GAME_CMD -> processGameAction(act)
            CmdConst.CLIENT_CMD -> processClientAction(act)
            else -> log.warn("无效的指令来自于 " + act.user.handler.channel)
        }
    }

    companion object {
        /**
         * 允许的最大房间数量
         */
        const val MAX_ROOM = 50
        /**
         * 单个玩家允许的房间最大数量
         */
        const val PLAYER_ROOM_LIMIT = 3
        /**
         * 超时时限 - 15分钟
         */
        const val TIME_OUT = 1000L * 60 * 15
        /**
         * 通知的发送间隔 - 1分钟
         */
        const val NOTIFY_GAP = 1000L * 60
    }

}
