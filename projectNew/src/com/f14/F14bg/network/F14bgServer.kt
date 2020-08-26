package com.f14.F14bg.network

import com.f14.F14bg.manager.PathManager
import com.f14.F14bg.utils.ResourceUtils
import com.f14.F14bg.utils.UpdateUtil
import com.f14.bg.hall.GameHall
import com.f14.f14bgdb.F14bgdb
import com.f14.f14bgdb.util.CodeUtil
import com.f14.f14bgdb.util.ScoreUtil
import com.f14.net.socket.server.SimpleServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.log4j.Logger
import java.io.IOException
import java.nio.channels.AsynchronousSocketChannel
import kotlin.system.exitProcess

class F14bgServer @Throws(IOException::class) private constructor() : SimpleServer() {
    val gameHall: GameHall = GameHall()

    private val log = Logger.getLogger(this.javaClass)

    override val timeoutTime: Long
        get() = 1000 * 60 * 120

    override suspend fun handlerSocket(channel: AsynchronousSocketChannel) {
        try {
            PlayerHandler(this, channel, timeoutTime).run()
        } catch (e: IOException) {
            log.error("创建用户失败!", e)
        }

    }

    override fun startupServer() {
        try {
            PathManager.init()
            log.info("初始化持久层...")
            F14bgdb.init()
            log.info("持久层初始化完成!")
            CodeUtil.loadAllCodes()
            ScoreUtil.init()
            log.info("装载资源...")
            ResourceUtils.init()
            log.info("资源装载完成!")
            UpdateUtil.init()
        } catch (e: Exception) {
            log.error("服务器启动时发生错误! ", e)
            exitProcess(-1)
        }

    }

    companion object {
        private var log = Logger.getLogger(F14bgServer::class.java)!!
        private var Instance: F14bgServer? = null

        val instance: F14bgServer?
            get() = Instance ?: try {
                Instance = F14bgServer()
                Instance
            } catch (e: IOException) {
                log.fatal("创建服务器实例出错!", e)
                null
            }

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 1) throw Exception("启动参数错误,请输入启动端口!")
            listenPort = args[0].toInt()
            runBlocking {
                val retry = 3
                (1..retry).forEach { i ->
                    // 如果启动失败,则等待30秒后重试,可以重试3次
                    instance?.run {
                        this.run()
                        return@runBlocking
                    } ?: if (i < retry) {
                        log.fatal("Socket服务器启动失败!等待30秒后重试...")
                        delay(30000)
                    } else {
                        log.fatal("Socket服务器启动失败!请检查端口是否被占用!")
                    }
                }
            }
        }
    }

}
