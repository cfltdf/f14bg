package com.f14.utils


import java.security.MessageDigest

/**
 * MD5工具类
 *
 * @author F14eagle
 */
object MD5Utils {
    // 用来将字节转换成 16 进制表示的字符
    private val hexDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    /**
     * 取得MD5加密后的字符串
     *
     * @param source
     * @return
     */
    private fun getMD5(source: ByteArray) = try {
        val md = MessageDigest.getInstance("MD5")
        md.update(source)
        val tmp = md.digest() // MD5 的计算结果是一个 128 位的长整数，
        // 用字节表示就是 16 个字节
        // 所以表示成 16 进制需要 32 个字符
        val r = tmp.map(Byte::toInt) // 每个字节用 16 进制表示的话，使用两个字符，
                .flatMap { listOf(it ushr 4 and 0xf, it and 0xf) }
                .map { hexDigits[it] }
                .toCharArray()
        String(r)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

    /**
     * 取得MD5加密后的字符串
     *
     * @param str
     * @return
     */
    fun getMD5(str: String) = getMD5(str.toByteArray())

}
