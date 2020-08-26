package com.f14.F14bg.utils

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.manager.PathManager
import com.f14.F14bg.network.CmdFactory
import com.f14.F14bg.network.F14bgServer
import com.f14.bg.exception.BoardGameException
import com.f14.bg.hall.User
import com.f14.f14bgdb.F14bgdb
import com.f14.f14bgdb.dao.BoardGameDao
import com.f14.f14bgdb.model.BoardGameModel
import com.f14.f14bgdb.util.CodeUtil
import com.f14.f14bgdb.util.ScoreUtil
import org.apache.log4j.Logger
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import kotlin.system.exitProcess

/**
 * 控制台指令管理器

 * @author F14eagle
 */
object ConsoleUtil {
    /**
     * 控制台指令的前缀
     */
    const val CMD_PREFIX = "/"
    /**
     * 控制台指令的分隔符
     */
    const val CMD_SPLIT = " "
    private var log = Logger.getLogger(ConsoleUtil::class.java)

    /**
     * 广播消息
     * @param user
     * @param command
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun broadcast(user: User, command: Command) {
        try {
            // 向服务器的所有用户广播信息
            val hall = F14bgServer.instance!!.gameHall
            hall.broadcast(user, command.paramString)
        } catch (e: Exception) {
            log.error("重载缓存时发生错误!", e)
            throw BoardGameException("重载缓存时发生错误!请重新启动服务器!")
        }

    }

    /**
     * 获得临时权限
     * @param user
     * @param command
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun getPrivilege(user: User, command: Command) {
        if (command.commandParams.isEmpty() || command.commandParams[0] != "nana") throw BoardGameException("非法的控制台指令!")
        user.isAdmin = true
        F14bgServer.instance?.gameHall?.let { hall ->
            hall.refreshUser(user)
            user.roomIds.mapNotNull(hall::getGameRoom).forEach { room -> room.refreshUser(user) }
        }
    }

    /**
     * 判断字符串是否是控制台指令
     * @param str
     * @return
     */
    fun isConsoleCommand(str: String?): Boolean {
        return str != null && str.startsWith(CMD_PREFIX)
    }

    /**
     * 处理控制台指令
     * @param user
     * @param cmd
     */
    @Throws(BoardGameException::class)
    fun processConsoleCommand(user: User, cmd: String) {
        if (!isConsoleCommand(cmd)) {
            throw BoardGameException("非法的控制台指令!")
        }
        val command = Command(cmd)
        if ("getPrivilege" == command.commandType) {
            getPrivilege(user, command)
        } else if (!PrivUtil.hasAdminPriv(user)) {
            throw BoardGameException("你没有权限进行该操作!")
        } else if ("refreshCache" == command.commandType) { // 重载缓存
            refreshCache()
        } else if ("restart" == command.commandType) { // 重启服务器
            restart()
        } else if ("update" == command.commandType) { // 更新文件
            update(command)
        } else if ("broadcast" == command.commandType) { // 广播公告
            broadcast(user, command)
        } else if ("see" == command.commandType) {
            see(user, command)
        } else if ("addGame" == command.commandType) { // 添加游戏
            addGame(command)
        } else if ("deleteGame" == command.commandType) {// 删除游戏
            deleteGame(command)
        } else {
            throw BoardGameException("非法的控制台指令!")
        }
    }

    /**
     * 添加游戏
     * @param command
     */
    private fun addGame(command: Command) {
        if (command.commandParams != null) {
            val dao = F14bgdb.getBean<BoardGameDao>("boardGameDao")
            val bg = BoardGameModel().apply {
                id = command.commandParams[0]
                cnname = command.commandParams[1]
                enname = command.commandParams[2]
                gameClass = command.commandParams[3]
                playerClass = command.commandParams[4]
                resourceClass = command.commandParams[5]
                minPlayerNumber = command.commandParams[6].toInt()
                maxPlayerNumber = command.commandParams[7].toInt()
            }
            dao.save(bg)
        }
    }

    /**
     * 删除游戏
     * @param command
     */
    private fun deleteGame(command: Command) {
        val dao = F14bgdb.getBean<BoardGameDao>("boardGameDao")
        val bg = dao[command.commandParams[0]] ?: return
        dao.delete(bg)
    }

    /**
     * 重载缓存
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun refreshCache() {
        // 暂时只重载代码表,积分信息,和更新模块的缓存
        try {
            CodeUtil.loadAllCodes()
            ResourceUtils.init()
            ScoreUtil.init()
            UpdateUtil.init()
        } catch (e: Exception) {
            log.error("重载缓存时发生错误!", e)
            throw BoardGameException("重载缓存时发生错误!请重新启动服务器!")
        }

    }

    /**
     * 重启服务器
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun restart() {
        // 重启服务器
        log.info("接收到重启服务器的指令...")
        val filename = PathManager.restartFileName
        val builder = ProcessBuilder(filename)
        builder.directory(PathManager.homeFile)
        try {
            try {
                if (executeSystemCommand(filename)) {
                    exitProcess(0)
                }
            } catch (e: Exception) {
                log.error(e)
            }
            // 启动重启服务器的进程后,杀掉该服务器进程
        } catch (e: Exception) {
            log.error("重启服务器时发生错误!", e)
            throw BoardGameException("重启服务器时发生错误! " + e.message)
        }

    }

    /**
     * 广播消息
     * @param user
     * @param command
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun see(user: User, command: Command) {
        try {
            if (command.commandParams != null) {
                val fr = FileReader(command.commandParams[0])
                val br = BufferedReader(fr)
                val sb = StringBuilder()
                while (true) {
                    val line = br.readLine() ?: break
                    sb.append(line).append("\r\n")
                }
                br.close()
                fr.close()
                val res = CmdFactory.createClientResponse(CmdConst.CLIENT_BROADCAST)
                res.public("user", user.name)
                res.public("message", sb.toString())
                user.sendResponse(0, res)
            }
        } catch (e: Exception) {
            log.error("重载缓存时发生错误!", e)
            throw BoardGameException("重载缓存时发生错误!请重新启动服务器!")
        }

    }

    @Throws(IOException::class)
    private fun executeSystemCommand(commandString: String): Boolean {
        val builder = ProcessBuilder(commandString)
        val p = builder.start()
        val b = ByteArray(1024)
        var readbytes: Int
        // 读取进程输出值
        // 在JAVA IO中,输入输出是针对JVM而言,读写是针对外部数据源而言
        val `in` = p.inputStream
        try {
            while (true) {
                readbytes = `in`.read(b)
                if (readbytes == -1) break
                val msg = String(b, 0, readbytes)
                log.debug(msg)
                // 出现[时表示新进程已经启动成功,这时可以杀掉当前进程
                if (msg.contains("[")) {
                    return true
                }
            }
        } catch (e: Exception) {
            log.error(e)
        }

        return false
    }

    /**
     * 更新文件
     * @param command
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun update(command: Command) {
        // 更新文件
        log.info("接收到更新文件的指令...")

        if (command.commandParams != null) {
            try {
                if (executeSystemCommand(command.commandParams[0])) {
                    exitProcess(0)
                }
            } catch (e: Exception) {
                log.error("更新文件时发生错误!", e)
                throw BoardGameException("更新文件时发生错误! " + e.message)
            }

        }
    }

    private class Command @Throws(BoardGameException::class) constructor(str: String) {
        val commandType: String
        val commandParams: Array<String>
        val paramString: String

        init {
            val cmds = str.substring(1).split(CMD_SPLIT.toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            this.commandType = cmds[0]
            if (cmds.size > 1) {
                paramString = str.substring(str.indexOf(CMD_SPLIT) + 1, str.length)
                commandParams = paramString.split(CMD_SPLIT.toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            } else {
                throw BoardGameException("未知指令内容!")
            }
        }

    }
}
