package com.f14.net.socket.cmd

import com.f14.F14bg.consts.CmdConst
import com.f14.net.socket.exception.ConnectionException
import com.f14.utils.ZipUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce
import org.apache.log4j.Logger
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CommandReader {
    val buffer = ByteBuffer.allocate(CACHE_SIZE)!!

    suspend fun validate(channel: AsynchronousSocketChannel, timeOut: Long) {
        buffer.flip()
        readBytes(channel, timeOut, buffer, 2)
        val flag = buffer.short
        if (flag != CmdConst.APPLICATION_FLAG.toShort()) {
            throw ConnectionException("连接校验失败! 切断连接!")
        }
    }

    private suspend fun readBytes(channel: AsynchronousSocketChannel, timeOut: Long, buffer: ByteBuffer, len: Int) {
        while (buffer.remaining() < len) {
            buffer.compact()
            try {
                if (channel.aRead(buffer, timeOut) < 0) throw ConnectionException("连接中断!")
            } catch (ex: CancellationException) {
                throw ConnectionException("连接超时!")
            }
            buffer.flip()
        }
    }

    fun readCommand(channel: AsynchronousSocketChannel, timeOut: Long) = GlobalScope.produce {
        try {
            while (true) {
                val cmd = ByteCommand()
                // 读取头信息
                readBytes(channel, timeOut, buffer, 14)
                cmd.head = buffer.short
                if (cmd.head != ByteCommand.BYTE_HEAD) {
                    CommandReader.log.error("非法头信息!切断连接!")
                    throw ConnectionException("非法头信息!切断连接!")
                }
                // 标志信息
                cmd.flag = buffer.short
                // 房间信息
                cmd.roomId = buffer.int
                // 压缩信息
                cmd.zip = buffer.short
                // 长度信息
                cmd.size = buffer.int
                // 读取内容
                readBytes(channel, timeOut, buffer, cmd.size)
                cmd.contentBytes = ByteArray(cmd.size)
                buffer.get(cmd.contentBytes)
                // 读取尾信息
                readBytes(channel, timeOut, buffer, 2)
                cmd.tail = buffer.short
                if (cmd.tail != ByteCommand.BYTE_TAIL) {
                    CommandReader.log.error("非法结束信息!切断连接!")
                    throw ConnectionException("非法结束信息!切断连接!")
                }
                // 判断数据是否经过压缩,如果经过压缩,则需要解压
                if (cmd.zip == 1.toShort()) cmd.contentBytes = ZipUtils.decompressBytes(cmd.contentBytes)
                this.send(cmd)
            }
        } catch (e: ConnectionException) {
            this.close(e)
        }
    }

    private suspend fun AsynchronousSocketChannel.aRead(buffer: ByteBuffer, timeOut: Long): Int{
        return suspendCoroutine { continuation ->
            this.read(buffer, timeOut, TimeUnit.MILLISECONDS, continuation, ReadCompletionHandler)
        }
    }

    object ReadCompletionHandler : CompletionHandler<Int, Continuation<Int>> {
        override fun completed(result: Int, attachment: Continuation<Int>) {
            attachment.resume(result)
        }

        override fun failed(exc: Throwable, attachment: Continuation<Int>) {
            attachment.resumeWithException(exc)
        }
    }

    companion object {
        var CACHE_SIZE = 1024 * 100
        private val log = Logger.getLogger(CommandReader::class.java)
    }


}
