package com.f14.F14bg.network

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.manager.ResourceManager
import com.f14.F14bg.utils.PrivUtil
import com.f14.F14bg.utils.ResourceUtils
import com.f14.F14bg.utils.UpdateUtil
import com.f14.bg.action.BgAction.*
import com.f14.bg.action.BgResponse
import com.f14.bg.action.ISendable
import com.f14.bg.exception.BoardGameException
import com.f14.bg.hall.GameRoom
import com.f14.bg.hall.User
import com.f14.f14bgdb.F14bgdb
import com.f14.f14bgdb.model.UserModel
import com.f14.f14bgdb.service.RankingManager
import com.f14.f14bgdb.service.UserManager
import com.f14.f14bgdb.util.CodeUtil
import com.f14.net.socket.cmd.ByteCommand
import com.f14.net.socket.cmd.CommandSender
import com.f14.net.socket.server.SocketHandler
import com.f14.utils.MD5Utils
import org.apache.log4j.Logger
import java.io.IOException
import java.nio.channels.AsynchronousSocketChannel

class PlayerHandler @Throws(IOException::class) constructor(
        private val server: F14bgServer,
        channel: AsynchronousSocketChannel,
        timeOut: Long
) : SocketHandler(channel, timeOut), ISendable {
    var user: User? = null
    private val sender = CommandSender(channel)

    /**
     * 检查模块是否需要更新
     * @param act
     */
    private fun checkUpdate(act: ClientAction) {
        val gameType = act.getAsString("gameType")
        val versionString = act.getAsString("versionString")
        // 然后检查游戏文件的更新情况
        val files = UpdateUtil.getUpdateList(gameType, versionString)
        // 发送更新文件列表的信息
        CmdFactory.createClientResponse(CmdConst.CLIENT_CHECK_UPDATE)
                .public("gameType", gameType)
                .public("files", files.joinToString(","))
                .public("versionString", UpdateUtil.getVersionString(gameType))
                .send(this)
    }

    /**
     * 关闭房间窗口
     * @param roomId
     */
    fun closeRoomShell(roomId: Int) {
        // 检查通过后向客户端发送打开房间窗口的指令
        CmdFactory.createClientResponse(CmdConst.CLIENT_CLOSE_ROOM)
                .public("roomId", roomId)
                .send(this)
    }

    public override val logPrefix
        get() = listOfNotNull(user?.name, super.logPrefix).joinToString("/")

    /**
     * 装载游戏资源
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun initResource(act: ClientAction) {
        val gameType = act.getAsString("gameType")
        val rm = ResourceUtils.getResourceManager<ResourceManager>(gameType) ?: throw BoardGameException("装载游戏资源失败!")
        rm.sendResourceInfo(this)
    }

    /**
     * 读取系统代码
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun loadCodeDetail() {
        val codes = CodeUtil.allCodes
        CmdFactory.createClientResponse(CmdConst.CLIENT_LOAD_CODE)
                .public("codes", codes)
                .send(this)
    }

    /**
     * 用户登录
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun loginUser(act: ClientAction) {
        if (this.user != null) throw BoardGameException("你已经登录,不能重复登录!")
        // 登录名转成小写
        val loginName: String = act.getAsString("loginName").toLowerCase()
        val password = act.getAsString("password")
        if (loginName.isEmpty()) throw BoardGameException("请输入用户名!")
        // 检查登录密码
        val userModel = F14bgdb.getBean<UserManager>("userManager").doLogin(loginName, password)
        // 检查该用户是否已经登录
        this.server.gameHall.getUser(loginName)?.let {
            // 如果该账号已经登录,则切断其连接
            it.closeConnection()
            throw BoardGameException("用户已经登录,请重试!")
        }
        val recent = this.server.gameHall.getRecentUser(loginName)
        // 如果该用户在最近登录玩家的列表中,则直接取该用户对象
        // 否则创建一个用户对象
        this.user = when (recent) {
            null -> User(this, userModel).also { it.isAdmin = PrivUtil.hasPriv(it, PrivUtil.PRIV_ADMIN) }
            else -> recent.also { it.handler = this }.also(this.server.gameHall::reconnect)
        }.also { user ->
            this.server.gameHall.addUser(user)
            // 发送登录成功的指令到客户端
            CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_LOGIN, -1)
                    .public("name", user.name)
                    .public("id", user.id)
                    .send(user)
        }
    }

    override fun onSocketClose() {
        try {
            sender.close()
        } catch (e: Exception) {
            log.error("${logPrefix}断线处理时发生错误!", e)
        }
        this.user?.let { user ->
            try {
                this.server.gameHall.userDisconnect(user)
            } catch (e: Exception) {
                log.error("${logPrefix}断线处理时发生错误!", e)
            } finally {
                // 无论如何,都要从大厅中移除用户
                this.server.gameHall.removeUser(user)
            }
        }
    }

    override fun onSocketConnect() = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_CONNECT, -1)
            .send(this)

    /**
     * 处理客户端类型的行动
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun processClientAction(act: ClientAction) {
        when (act.code) {
            CmdConst.CLIENT_INIT_RESOURCE // 装载游戏资源
            -> this.initResource(act)
            CmdConst.CLIENT_LOAD_CODE // 读取系统代码
            -> this.loadCodeDetail()
            CmdConst.CLIENT_CHECK_UPDATE // 检查模块是否需要更新
            -> this.checkUpdate(act)
            CmdConst.CLIENT_USER_INFO // 查看用户信息
            -> this.refreshUserRanking(act)
            else -> throw BoardGameException("无效的指令代码!")
        }
    }

    override fun processCommand(cmd: ByteCommand) {
        if (cmd.flag != CmdConst.APPLICATION_FLAG.toShort()) throw BoardGameException("错误的应用指令!")
        val room = this.server.gameHall.getGameRoom(cmd.roomId)
        val user = this.user
        try {
            when {
                room != null && user != null -> room.processCommand(GameAction(user, room, cmd.content))
                user != null -> this.server.gameHall.processCommand(HallAction(user, cmd.content))
                else -> {
                    val act = ClientAction(cmd.content)
                    when (act.type) {
                        // 系统指令
                        CmdConst.SYSTEM_CMD -> this.processSystemAction(act)
                        else -> log.debug("无效的指令来自于 $channel")
                    }
                }
            }
        } catch (e: BoardGameException) {
            log.warn(e.message + " 来自于 " + this.logPrefix, e)
            this.sendCommand(CmdConst.EXCEPTION_CMD, cmd.roomId, e.message)
        } catch (e: Exception) {
            // 如果在处理指令时发生了异常,则记录日志并发送到客户端
            log.error(e.message + " 来自于 " + this.logPrefix, e)
            this.sendCommand(CmdConst.EXCEPTION_CMD, cmd.roomId, e.message)
        }
    }

    /**
     * 处理系统类型的行动
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun processSystemAction(act: ClientAction) {
        // 发送大厅用户列表
        when (act.code) {
            CmdConst.SYSTEM_CODE_USER_REGIST  // 注册
            -> this.registUser(act)
            CmdConst.SYSTEM_CODE_LOGIN // 登录
            -> this.loginUser(act)
            else -> throw BoardGameException("无效的指令代码!")
        }
    }


    /**
     * 刷新用户的积分信息
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun refreshUserRanking(act: ClientAction) {
        val userId = act.getAsLong("userId")
        val user = F14bgdb.getBean<UserManager>("userManager")[userId] ?: throw BoardGameException("找不到指定用户!")
        val list = F14bgdb.getBean<RankingManager>("rankingManager").queryUserRanking(userId)
        CmdFactory.createClientResponse(CmdConst.CLIENT_USER_INFO)
                .public("username", user.userName)
                .public("list", list)
                .send(this)
    }

    /**
     * 注册用户
     * @param act
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun registUser(act: ClientAction) {
        val reset = act.getAsBoolean("reset")
        // 登录名转成小写
        val loginName: String = act.getAsString("loginName").toLowerCase()
        val um = F14bgdb.getBean<UserManager>("userManager")
        if (reset) {
            // 重设密码
            val origpassword = act.getAsString("origpassword")
            val newpassword = act.getAsString("newpassword")
            um.doLogin(loginName, origpassword).apply {
                this.password = MD5Utils.getMD5(newpassword)
            }.also { um.update(it) }
        } else {
            // 新建用户
            val password = act.getAsString("password")
            val userName = act.getAsString("userName")
            // 登录名转成小写
            UserModel().apply {
                this.loginName = loginName
                this.password = password
                this.userName = userName
            }.also(um::createUser)
        }
        // 发送注册成功的消息
        CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_USER_REGIST, -1)
                .public("reset", reset)
                .send(this)
    }

    override fun sendCommand(cmd: ByteCommand) {
        if (sender.socket.isOpen) try {
            sender.sendCommand(cmd)
            log.debug("发送指令: $cmd | 至 ${this@PlayerHandler}")
        } catch (ex: Exception) {
            log.error(ex, ex)
            this.close()
        }
    }

    /**
     * 向客户端发送打开房间窗口的指令
     * @param room
     */
    fun sendOpenRoomResponse(room: GameRoom) {
        // 检查通过后向客户端发送打开房间窗口的指令
        CmdFactory.createClientResponse(CmdConst.CLIENT_OPEN_ROOM)
                .public("id", room.id)
                .public("gameType", room.type)
                .send(this)
    }

    /**
     * 将res中的公共信息发送到socket客户端
     * @param res
     */
    override fun sendResponse(res: BgResponse) {
        val content = res.publicString
        val cmd = when (res.type) {
            // 如果消息类型是客户端消息,则按照客户端消息的类型发送
            CmdConst.CLIENT_CMD -> CmdFactory.createClientCommand(content)
            else -> CmdFactory.createCommand(0, content)
        }
        this.sendCommand(cmd)
    }

    companion object {
        private val log = Logger.getLogger(PlayerHandler::class.java)!!
    }
}
