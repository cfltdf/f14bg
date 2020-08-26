package com.f14.net.socket.cmd

import com.f14.utils.ZipUtils
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.apache.log4j.Logger
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel

class CommandReader {
    private val pool = newSingleThreadContext("reader")

    private fun readBytes(inputStream: InputStream, buffer: ByteBuffer, len: Int) : ByteBuffer {
        var result = buffer
        while (CACHE_SIZE < len) CACHE_SIZE += CACHE_SIZE
        if (buffer.capacity() < CACHE_SIZE){
            result = ByteBuffer.allocate(CACHE_SIZE)
            result.put(buffer)
            result.flip()
        }
        while (result.remaining() < len) {
            result.compact()
            val array = ByteArray(CACHE_SIZE)
            val count = inputStream.read(array)
            if (count <= 0)
                throw Exception("连接中断!")
            result.put(array, 0, count)
            result.flip()
        }
        return  result
    }

    fun readCommand(inputStream: InputStream) = produce<ByteCommand>(pool) {
        var buffer = ByteBuffer.allocate(CACHE_SIZE)
        buffer.flip()
        while (true) {
            val cmd = ByteCommand()
            // 读取头信息
            buffer = readBytes(inputStream, buffer, 14)
            cmd.head = buffer.short
            if (cmd.head != ByteCommand.BYTE_HEAD) {
                CommandReader.log.error("非法头信息!切断连接!")
                throw Exception("非法头信息!切断连接!")
            }
            cmd.flag = buffer.short // 标志信息
            cmd.roomId = buffer.int // 房间信息
            cmd.zip = buffer.short // 压缩信息
            cmd.size = buffer.int // 长度信息
            // 读取内容
            buffer = readBytes(inputStream, buffer, cmd.size)
            cmd.contentBytes = ByteArray(cmd.size)
            buffer.get(cmd.contentBytes)
            // 读取尾信息
            buffer = readBytes(inputStream, buffer, 2)
            cmd.tail = buffer.short
            if (cmd.tail != ByteCommand.BYTE_TAIL) {
                CommandReader.log.error("非法结束信息!切断连接!")
                throw Exception("非法结束信息!切断连接!")
            }
            // 判断数据是否经过压缩,如果经过压缩,则需要解压
            if (cmd.zip == 1.toShort()) {
                cmd.contentBytes = ZipUtils.decompressBytes(cmd.contentBytes)
            }
            this.send(cmd)
        }
    }

    companion object {
//        var TAG_HEAD = "HEAD:"
//        var TAG_FLAG = "FLAG:"
//        var TAG_SIZE = "SIZE:"
//        var TAG_CONTENT = "CONTENT:"
//        var TAG_TAIL = "TAIL:"
        var CACHE_SIZE = 1024 * 500
        private val log = Logger.getLogger(CommandReader::class.java)
    }

}
