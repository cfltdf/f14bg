package com.f14.net.socket.server

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.log4j.Logger
import java.io.IOException
import java.net.InetSocketAddress
import java.net.StandardSocketOptions
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

abstract class SimpleServer @Throws(IOException::class) constructor() {
    private val listener: AsynchronousServerSocketChannel = AsynchronousServerSocketChannel.open().bind(InetSocketAddress(listenPort))

    /**
     * 超时设置,如果小于0则不设置超时
     * @return
     */
    abstract val timeoutTime: Long

    /**
     * 处理socket连接
     * @param channel
     */
    protected abstract suspend fun handlerSocket(channel: AsynchronousSocketChannel)

    suspend fun run() {
        log.info("开始启动服务器...")
        startupServer()
        log.info("服务器启动完成!")
        try {
            while (true) {
                val channel = listener.aAccept()
                GlobalScope.launch {
                    channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true)
                    handlerSocket(channel)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            log.error(e, e)
        } finally {
            println("finished server")
        }
    }

    /**
     * 启动服务器时的操作
     */
    abstract fun startupServer()

    private suspend fun AsynchronousServerSocketChannel.aAccept(): AsynchronousSocketChannel{
        return suspendCoroutine { continuation ->
            this.accept(continuation, AcceptCompletionHandler)
        }
    }

    object AcceptCompletionHandler : CompletionHandler<AsynchronousSocketChannel, Continuation<AsynchronousSocketChannel>> {
        override fun completed(result: AsynchronousSocketChannel, attachment: Continuation<AsynchronousSocketChannel>) {
            attachment.resume(result)
        }

        override fun failed(exc: Throwable, attachment: Continuation<AsynchronousSocketChannel>) {
            attachment.resumeWithException(exc)
        }
    }


    companion object {
        const val xml = "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"*\" /></cross-domain-policy>"
        var listenPort = 8181
        private val log = Logger.getLogger(SimpleServer::class.java)
    }

}
