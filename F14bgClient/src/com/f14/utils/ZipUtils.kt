package com.f14.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * 把字符串使用ZIP压缩和解压缩的代码。

 * @author F14eagle
 */
object ZipUtils {
    private val cachesize = 1024
    private val decompresser = Inflater()
    private val compresser = Deflater()

    /**
     * 按zlib方式压缩byte数组

     * @param input
     * *
     * @return
     */
    fun compressBytes(input: ByteArray): ByteArray {
        compresser.reset()
        compresser.setInput(input)
        compresser.finish()
        var output = ByteArray(0)
        val o = ByteArrayOutputStream(input.size)
        try {
            val buf = ByteArray(cachesize)
            var got: Int
            while (!compresser.finished()) {
                got = compresser.deflate(buf)
                o.write(buf, 0, got)
            }
            output = o.toByteArray()
        } finally {
            try {
                o.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return output
    }

    /**
     * 按zlib方式解压byte数组

     * @param input
     * *
     * @return
     */
    fun decompressBytes(input: ByteArray): ByteArray {
        var output = ByteArray(0)
        decompresser.reset()
        decompresser.setInput(input)
        val o = ByteArrayOutputStream(input.size)
        try {
            val buf = ByteArray(cachesize)

            var got: Int
            while (!decompresser.finished()) {
                got = decompresser.inflate(buf)
                o.write(buf, 0, got)
            }
            output = o.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                o.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return output
    }
}