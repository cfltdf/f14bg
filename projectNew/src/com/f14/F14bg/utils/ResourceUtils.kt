package com.f14.F14bg.utils

import com.f14.F14bg.consts.GameType
import com.f14.F14bg.manager.ResourceManager
import com.f14.f14bgdb.util.CodeUtil
import org.apache.log4j.Logger
import java.util.*
import kotlin.system.exitProcess

object ResourceUtils {
    private val log = Logger.getLogger(ResourceUtils::class.java)

    private val map = HashMap<String, ResourceManager>()

    /**
     * 添加资源管理器,在添加时将初始化管理器,如果初始化失败,则将结束程序
     * @param <RM>
     * @param string
     * @param rm
     */
    fun <RM : ResourceManager> addResourceManager(string: String, rm: RM) {
        try {
            rm.init()
        } catch (e: Exception) {
            log.fatal(e, e)
            exitProcess(-1)
        }
        map[string] = rm
    }

    /**
     * 按照clazz类型取得对应的资源管理器
     * @param <RM>
     * @return
     */
    fun <RM : ResourceManager> getResourceManager(gameType: GameType): RM {
        return getResourceManager(gameType.toString())!!
    }

    /**
     * 按照clazz类型取得对应的资源管理器
     * @param <RM>
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    fun <RM : ResourceManager> getResourceManager(gameType: String): RM? {
        val bg = CodeUtil.getBoardGame(gameType)
        if (bg != null) {
            return map[bg.id] as RM
        }
        return null
    }

    @Throws(InstantiationException::class, IllegalAccessException::class)
    fun init() {
        val boardGames = CodeUtil.boardGames
        var rm: ResourceManager
        map.clear()
        for (bg in boardGames) {
            log.info("装载 " + bg.cnname + " 的游戏资源...")
            val id = bg.id ?: continue
            val cls = CodeUtil.getResourceManagerClass(id) ?: continue
            rm = cls.newInstance()
            rm.loader = CodeUtil.getLoader(id)!!
            addResourceManager(id, rm)
        }
    }
}
