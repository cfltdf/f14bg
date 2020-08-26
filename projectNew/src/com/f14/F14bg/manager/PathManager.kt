package com.f14.F14bg.manager

import org.apache.log4j.Logger
import java.io.File
import java.io.FileInputStream
import java.net.URLDecoder
import java.util.*

object PathManager {
    lateinit var home: String
    var log = Logger.getLogger(PathManager::class.java)!!
    var pathProperties = Properties()
    var OS = System.getProperty("os.name").toLowerCase()

    /**
     * 获得游戏模块的jar文件对象
     * @param id
     * @return
     */
    fun getGameFile(id: String): File {
        return File(home + PathManager.pathProperties.getProperty(id))
    }

    /**
     * 取得服务器启动路径对象
     * @return
     */
    val homeFile: File
        get() = File(home)

    /**
     * 获得重启命令的文件对象
     * @return
     */
    val restartFileName: String
        get() = if (OS.contains("windows")) "f14bgServer.bat" else "./f14bgServer.sh"

    fun init() {
        try {
            val orgpath = PathManager::class.java.classLoader.getResource(".")!!.path
            val path = URLDecoder.decode(orgpath, "UTF-8")
            home = if (OS.contains("windows")) path.substring(1) else path
            PathManager.log.info("服务器启动路径: $home")
            PathManager.pathProperties.load(FileInputStream(home + "conf/path.properties"))
        } catch (e: Exception) {
            PathManager.log.fatal("设置服务器启动路径失败!", e)
        }

    }
}
