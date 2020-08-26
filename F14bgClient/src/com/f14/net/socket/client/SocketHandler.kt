package com.f14.net.socket.client

import com.f14.net.socket.SocketContext
import com.f14.net.socket.cmd.ByteCommand
import com.f14.net.socket.cmd.CommandFactory
import com.f14.net.socket.cmd.CommandReader
import org.apache.log4j.Logger
import java.io.IOException
import java.net.Socket

abstract class SocketHandler(var socket: Socket) {
    protected var log = Logger.getLogger(this.javaClass)
    var closed: Boolean = false

    suspend fun run() {
        try {
            // log.info("连接成功: " + socket);
            processSocket()
            // log.info("断开连接: " + socket);
        } catch (e: IOException) {
            log.error(socket.toString() + " 连接发生错误!", e)
        }

    }

    @Throws(IOException::class)
    suspend protected fun processSocket() {
        val `is` = socket.getInputStream()
        val cmdreader = CommandReader()
        try {
            this.onSocketConnect()
            for (cmd in cmdreader.readCommand(`is`)) {
                log.debug("收到指令: $cmd | 来自 $socket")
                this.processCommand(cmd)
            }
        } catch (e: IOException) {
            log.error(socket.toString() + " 连接发生错误!", e)
        } catch (e: Exception) {
            log.error(socket.toString() + " 系统错误!", e)
        } finally {
            this.onSocketClose()
            this.socket.close()
        }
    }

    /**
     * 在socket连接时调用的方法

     * @throws IOException
     */
    @Throws(IOException::class)
    protected abstract fun onSocketConnect()

    /**
     * 在关闭socket连接时调用的方法

     * @throws IOException
     */
    @Throws(IOException::class)
    protected abstract fun onSocketClose()

    /**
     * 处理指令

     * @param cmd
     */
    @Throws(IOException::class)
    protected abstract fun processCommand(cmd: ByteCommand)

    /**
     * 发送指令

     * @param flag @param content @throws
     */
    @Throws(IOException::class)
    fun sendCommand(flag: Int, roomId: Int, content: String) {
        val cmd = CommandFactory.createCommand(flag, roomId, content)
        this.sendCommand(cmd)
    }

    /**
     * 发送指令

     * @param cmd @throws
     */
    @Throws(IOException::class)
    abstract fun sendCommand(cmd: ByteCommand)

    /**
     * 初始化当前线程的socketContext
     */
    protected fun initSocketContext() {
        SocketContext.init()
        SocketContext.setSocket(socket)
    }

    /**
     * 判断连接是否关闭

     * @return
     */
    val isClosed: Boolean
        get() = this.socket.isClosed || this.closed

    /**
     * 关闭连接
     */
    fun close() {
        try {
            this.socket.close()
        } catch (e: IOException) {
            log.error(e, e)
        }

        this.closed = true
    }
}
