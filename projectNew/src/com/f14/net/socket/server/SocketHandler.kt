package com.f14.net.socket.server

import com.f14.net.socket.cmd.ByteCommand
import com.f14.net.socket.cmd.CommandFactory
import com.f14.net.socket.cmd.CommandReader
import com.f14.net.socket.exception.ConnectionException
import org.apache.log4j.Logger
import java.io.IOException
import java.net.SocketAddress
import java.nio.channels.AsynchronousSocketChannel

abstract class SocketHandler @Throws(IOException::class) constructor(
        val channel: AsynchronousSocketChannel,
        val timeOut: Long = 0
) {
    val address: SocketAddress = channel.remoteAddress
    protected val reader = CommandReader()

    /**
     * 关闭连接
     * @throws IOException
     */
    fun close() {
        try {
            if (channel.isOpen) channel.close()
        } catch (ex: IOException) {
            log.error(ex)
        } finally {
            this.onSocketClose()
        }
    }

    /**
     * 取得记录日志的前缀信息
     * @return
     */
    protected open val logPrefix: String
        get() = address.toString()

    /**
     * 在关闭socket连接时调用的方法
     * @throws IOException
     */
    protected abstract fun onSocketClose()

    /**
     * 在socket连接时调用的方法
     */
    protected abstract fun onSocketConnect()

    /**
     * 处理指令
     * @param cmd
     */
    abstract fun processCommand(cmd: ByteCommand)

    /**
     * 发送指令
     * @param cmd
     */
    abstract fun sendCommand(cmd: ByteCommand)

    /**
     * 发送指令
     * @param flag @param content @throws
     */
    fun sendCommand(flag: Int, roomId: Int, content: String?) = sendCommand(CommandFactory.createCommand(flag, roomId, content))

    suspend fun run() {
        onSocketConnect()
        try {
            this.reader.validate(channel, timeOut)
            for (cmd in this.reader.readCommand(channel, timeOut)) {
                log.debug("收到指令: $cmd | 来自 ${this@SocketHandler}")
                this.processCommand(cmd)
            }
        } catch (ex: IOException) {
//            log.warn("连接中断 来自 ${this@SocketHandler}")
        } catch (ex: ConnectionException) {
//            log.warn("连接中断 来自 ${this@SocketHandler}")
        } catch (ex: Exception) {
            log.error("连接发生错误!", ex)
        } finally {
            this.close()
            log.debug("连接结束: ${this@SocketHandler}")
        }
    }

    override fun toString() = logPrefix

    companion object {
        protected val log = Logger.getLogger(SocketHandler::class.java)!!
    }

}
