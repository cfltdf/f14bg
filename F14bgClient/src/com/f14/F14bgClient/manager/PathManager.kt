package com.f14.F14bgClient.manager

import java.io.File
import java.io.FileInputStream
import java.net.URLDecoder
import java.util.Properties

import org.apache.log4j.Logger

import com.f14.utils.StringUtils

/**
 * 文件路径管理器

 * @author F14eagle
 */
class PathManager {
    init {
        this.init()
    }

    protected fun init() {
        // 初始化模块版本的目录
        val path = this.versionDirectory
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    /**
     * 取得基本模块的地址

     * @return
     */
    val loaderPath: String
        get() = home + mainPrefix + "F14loader.swf"

    /**
     * 取得基本游戏模块的地址

     * @return
     */
    val gameLoaderPath: String
        get() = home + mainPrefix + "F14room.swf"

    /**
     * 取得登录界面文件的路径

     * @return
     */
    val loginPath: String
        get() = home + loginPrefix + "LoginModule.swf"

    /**
     * 取得大厅界面文件的路径

     * @return
     */
    val hallPath: String
        get() = home + hallPrefix + "HallModule.swf"

    /**
     * 取得版本更新界面文件的路径

     * @return
     */
    val updatePath: String
        get() = home + updatePrefix + "UpdateModule.swf"

    /**
     * 取得用户信息界面文件的路径

     * @return
     */
    val viewUserPath: String
        get() = home + queryPrefix + "ViewUserModule.swf"

    /**
     * 取得游戏模块的路径

     * @param gameType
     * *
     * @return
     */
    fun getGameModulePath(gameType: String): String {
        return this.getBasePath(gameType) + gameType + ".swf"
    }

    /**
     * 取得主程序文件夹的路径

     * @return
     */
    val mainappPath: String
        get() = home + mainPrefix!!

    /**
     * 取得游戏的基本路径

     * @param gameType
     * *
     * @return
     */
    fun getBasePath(gameType: String): String {
        if (MAIN_APP == gameType) {
            return home
        } else {
            return home + modulePrefix + gamePrefix + gameType + "/"
        }
    }

    /**
     * 取得图片的路径

     * @param gameType
     * *
     * @param file
     * *
     * @return
     */
    fun getImagePath(gameType: String, file: String): String {
        return this.getBasePath(gameType) + "images/" + file
    }

    /**
     * 取得模块版本文件总目录的路径

     * @return
     */
    val versionDirectory: String
        get() = home + "version/"

    /**
     * 取得模块版本文件的路径

     * @param gameType
     * *
     * @return
     */
    fun getVersionFile(gameType: String): String {
        var gameType = gameType
        if (StringUtils.isEmpty(gameType)) {
            gameType = PathManager.MAIN_APP
        }
        return this.versionDirectory + gameType + ".ver"
    }

    /**
     * 取得临时下载文件夹的路径

     * @param gameType
     * *
     * @return
     */
    fun getTemplatePath(gameType: String): String {
        if (MAIN_APP == gameType) {
            return home + "temp/"
        } else {
            return home + "temp/" + modulePrefix + gameType + "/"
        }
    }

    /**
     * 取得游戏模块对应的图片地址

     * @param gameType
     * *
     * @return
     */
    fun getGameImage(gameType: String): String {
        return home + "images/" + gameType + ".png"
    }

    companion object {
        //	protected static Logger log = Logger.getLogger(PathManager.class);
        /**
         * 主程序的代码
         */
        val MAIN_APP = "mainapp"
        lateinit var home: String
        // private static String modulePath;
        private var modulePrefix: String? = null
        private var mainPrefix: String? = null
        private var loginPrefix: String? = null
        private var hallPrefix: String? = null
        private var updatePrefix: String? = null
        private var queryPrefix: String? = null
        private var gamePrefix: String? = null
        private val pathProperties = Properties()

        init {
            try {
                val orgpath = File("").canonicalPath
                val path = URLDecoder.decode(orgpath, "UTF-8")
                home = path + "/"
                println("当前文件路径: " + home)
                //			log.info("当前文件路径: " + home);
                // path =
                // ClassLoader.getSystemClassLoader().getResource(".").toString().replaceAll("file:/",
                // "");
                pathProperties.load(FileInputStream(home + "./path.properties"))
                modulePrefix = getPathProperty("modulePrefix")
                mainPrefix = getPathProperty("mainPrefix")
                loginPrefix = getPathProperty("loginPrefix")
                hallPrefix = getPathProperty("hallPrefix")
                updatePrefix = getPathProperty("updatePrefix")
                queryPrefix = getPathProperty("queryPrefix")
                gamePrefix = getPathProperty("gamePrefix")
                if ("true" != getPathProperty("absulote")) {
                    // modulePath = home;
                    // }else{
                    home = getPathProperty("home")
                }
                // home = "D:/flexwork/F14bg/bin-debug/";
            } catch (e: Exception) {
                println("读取路径配置文件出错!")
                e.printStackTrace()
                //			log.error("读取路径配置文件出错!", e);
                System.exit(-1)
            }

        }

        /**
         * 取得路径配置值

         * @param key
         * *
         * @return
         */
        fun getPathProperty(key: String): String {
            return pathProperties.getProperty(key)
        }
    }
}
