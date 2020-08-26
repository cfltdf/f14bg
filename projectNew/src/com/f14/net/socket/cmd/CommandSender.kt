package com.f14.net.socket.cmd

import com.f14.net.socket.exception.ConnectionException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.runBlocking
import org.apache.log4j.Logger
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CommandSender(val socket: AsynchronousSocketChannel) {
    private val consumer: SendChannel<ByteCommand> = GlobalScope.actor {
        for (cmd in channel) {
            val size = 16 + cmd.size
            val buffer = ByteBuffer.allocate(size).putShort(cmd.head).putShort(cmd.flag).putInt(cmd.roomId).putShort(cmd.zip).putInt(cmd.size).put(cmd.contentBytes).putShort(cmd.tail).apply { flip() }
            try {
                while (buffer.remaining() > 0) {
                    if (socket.aWrite(buffer) < 0) {
                        throw ConnectionException("连接中断!")
                    }
                }
            } catch (e: Exception) {
                channel.close(ConnectionException("连接中断", e))
            }
        }
    }

    /**
     * 发送指令
     * @param cmd
     * @throws IOException
     */
    @Synchronized
    fun sendCommand(cmd: ByteCommand) {
        runBlocking {
            consumer.send(cmd)
        }
    }

    /**
     * 关闭连接
     */
    fun close() {
        consumer.close()
        if (socket.isOpen) {
            socket.close()
        }
    }

    private suspend fun AsynchronousSocketChannel.aWrite(buffer: ByteBuffer): Int {
        return suspendCoroutine { continuation ->
            this.write(buffer, continuation, WriteComplettionHandler)
        }
    }

    object WriteComplettionHandler : CompletionHandler<Int, Continuation<Int>> {
        override fun completed(result: Int, attachment: Continuation<Int>) {
            attachment.resume(result)
        }

        override fun failed(exc: Throwable, attachment: Continuation<Int>) {
            attachment.resumeWithException(exc)
        }

    }


    companion object {
        private var log = Logger.getLogger(CommandSender::class.java)!!
    }

}