package com.f14.net.socket.cmd

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.ActorJob
import kotlinx.coroutines.experimental.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.nio.aWrite
import kotlinx.coroutines.experimental.runBlocking
import org.apache.log4j.Logger
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

class CommandSender(val socket: Socket){
    protected val pool = newSingleThreadContext("sender")

    protected val consumer: ActorJob<ByteCommand> = actor(pool, UNLIMITED){
        for (cmd in channel){
            val size = 16 + cmd.size
            val buffer = ByteBuffer.allocate(size)
                    .putShort(cmd.head)
                    .putShort(cmd.flag)
                    .putInt(cmd.roomId)
                    .putShort(cmd.zip)
                    .putInt(cmd.size)
                    .put(cmd.contentBytes)
                    .putShort(cmd.tail)
                    .apply { flip() }
            socket.getOutputStream().write(buffer.array())
        }
    }

    /**
     * 发送指令
     * @param cmd
     * @throws IOException
     */
    @Synchronized fun sendCommand(cmd: ByteCommand) {
        runBlocking {
            consumer.send(cmd)
        }
    }

    /**
     * 关闭连接
     */
    fun close() {
        consumer.close()
    }

    companion object {
        protected var log = Logger.getLogger(CommandSender::class.java)!!
    }

}
