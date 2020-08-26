package com.f14.F14bgClient.manager

import java.io.File
import java.io.FileInputStream

class FileManager {

    /**
     * 读取文件,返回byte数组

     * @param path
     * *
     * @return
     * *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun loadFile(path: String): ByteArray {
        val file = File(path)
        val `is` = FileInputStream(file)
        val size = `is`.available()
        val bs = ByteArray(size)
        `is`.read(bs)
        `is`.close()
        return bs
    }

    /**
     * 读取文件

     * @param gameType
     * *
     * @param file
     * *
     * @return
     * *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun loadFile(gameType: String, file: String): ByteArray {
        val path = ManagerContainer.pathManager.getImagePath(gameType, file)
        return this.loadFile(path)
    }
}
