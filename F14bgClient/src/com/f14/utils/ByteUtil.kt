package com.f14.utils

object ByteUtil {

    /**
     * 将4个byte转换成int

     * @param ba
     * *
     * @return
     */
    fun b2toi(ba: ByteArray): Int {
        return ba[0].toInt() shl 8 and 0xff00 or (ba[1].toInt() and 0xff)
    }

    /**
     * 将4个byte转换成int

     * @param ba
     * *
     * @return
     */
    fun b4toi(ba: ByteArray): Int {
        return ba[0].toInt() shl 24 or (ba[1].toInt() shl 24).ushr(8) or (ba[2].toInt() shl 8 and 0xff00) or (ba[3].toInt() and 0xff)
    }

    /**
     * 将int转换为2个byte

     * @param i
     * *
     * @return
     */
    fun itob2(i: Int): ByteArray {
        val b = ByteArray(2)
        b[1] = (i and 0xff).toByte()
        b[0] = (i shr 8 and 0xff).toByte()
        return b
    }

    /**
     * 将int转换为4个byte

     * @param i
     * *
     * @return
     */
    fun itob4(i: Int): ByteArray {
        val b = ByteArray(4)
        b[3] = (i and 0xff).toByte()
        b[2] = (i shr 8 and 0xff).toByte()
        b[1] = (i shr 16 and 0xff).toByte()
        b[0] = i.ushr(24).toByte()
        return b
    }

    @JvmStatic fun main(args: Array<String>) {
        val ba = itob2(1)
        for (e in ba) {
            println(e)
        }

        println(b2toi(ba))
    }
}
