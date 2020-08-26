package com.f14.net.socket.cmd

import com.f14.utils.ZipUtils

import java.io.UnsupportedEncodingException

/**
 * 远程通讯时用到的指令
 * 指令包括以下几个部分:
 * 头信息:发送指令方的信息
 * 指令标志:表示执行对应程序的代码
 * 内容长度:指令具体内容的长度
 * 内容:指令的具体内容
 * 结束符号:表示一条指令结束的符号
 * @author F14eagle
 */
class ByteCommand {

    var head: Short = 0
    var flag: Short = 0
    var roomId: Int = 0
    /**
     * 是否压缩 0:否 1:是
     */
    var zip: Short = 0
    var size: Int = 0
    var contentBytes: ByteArray = ByteArray(0)
    var tail: Short = 0
    private val toString: String by lazy {
        String.format("%X | %X | %d | N | %d | %s | %X", this.head, this.flag, this.roomId, this.size, if (zip.toInt() == 1) "内容已压缩" else this.content, this.tail)
    }
    private var _content: String? = null
    var content: String
        get() {
            return _content ?: try {
                String(this.contentBytes)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                ""
            }.also { _content = it }
        }
        set(content) {
            _content = content
            try {
                contentBytes = content.toByteArray(charset("UTF-8"))
                when {
                    contentBytes.size < ZIP_LIMIT -> zip = 0
                    else -> {
                        // 进行数据压缩
                        zip = 1
                        contentBytes = ZipUtils.compressBytes(this.contentBytes)
                    }
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                contentBytes = ByteArray(0)
            }
            size = contentBytes.size
        }

    override fun toString() = this.toString

    companion object {
        var BYTE_HEAD = 0xf14f.toShort()
        var BYTE_TAIL = 0xf41f.toShort()
        /**
         * 需要压缩数据的最小数据量
         */
        var ZIP_LIMIT = 500
    }
}
