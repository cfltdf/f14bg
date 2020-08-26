package com.f14.f14bgdb.util

import com.f14.F14bg.manager.PathManager
import com.f14.F14bg.manager.ResourceManager
import com.f14.bg.BoardGame
import com.f14.bg.player.Player
import com.f14.f14bgdb.F14bgdb
import com.f14.f14bgdb.dao.BoardGameDao
import com.f14.f14bgdb.model.BoardGameModel
import com.f14.f14bgdb.model.CodeDetailModel
import com.f14.f14bgdb.service.CodeDetailManager
import org.apache.log4j.Logger
import java.net.URLClassLoader
import java.util.*

object CodeUtil {
    const val CODE_BOARDGAME = "BOARDGAME"
    private val map = HashMap<String, MutableMap<String, CodeDetailModel>>()
    private val list = HashMap<String, MutableList<CodeDetailModel>>()
    private val bgs = LinkedHashMap<String, BoardGameModel>()
    private val classMap = HashMap<String, GameClass>()
    private val log = Logger.getLogger(CodeUtil::class.java)

    val allCodes: Map<String, List<CodeDetailModel>>
        get() = list

    val boardGames: Collection<BoardGameModel>
        get() = bgs.values

    fun loadAllCodes() {
        CodeUtil.log.info("装载系统代码...")
        CodeUtil.map.clear()
        list.clear()
        bgs.clear()
        val dao = F14bgdb.getBean<CodeDetailManager>("codeDetailManager")
        val list = dao.query(CodeDetailModel(), "codeType,codeIndex")
        list.forEach(CodeUtil::addCode)
        loadOtherCodes()
        CodeUtil.log.info("系统代码装载完成!")
    }

    private fun getMap(codeType: String): MutableMap<String, CodeDetailModel> {
        return CodeUtil.map.computeIfAbsent(codeType) { HashMap() }
    }

    private fun getList(codeType: String): MutableList<CodeDetailModel> {
        return list.computeIfAbsent(codeType) { ArrayList() }
    }

    private fun addCode(o: CodeDetailModel) {
        val codeType = o.codeType ?: return
        val value = o.value ?: return
        getMap(codeType)[value] = o
        getList(codeType).add(o)
    }

    fun getLabel(codeType: String, value: String): String? {
        return getMap(codeType)[value]?.label
    }

    fun getCodes(codeType: String): List<CodeDetailModel> {
        return getList(codeType)
    }

    private fun loadOtherCodes() {
        val dao = F14bgdb.getBean<BoardGameDao>("boardGameDao")
        val codes = dao.query(BoardGameModel())
        classMap.clear()
        codes.withIndex().forEach { (i, e) ->
            val c = CodeDetailModel()
            val id = e.id ?: return@forEach
            c.label = e.cnname
            c.value = e.id
            c.codeType = CODE_BOARDGAME
            c.codeIndex = i
            addCode(c)
            try {
                loadBoardGame(e)
                bgs[id] = e
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    @Throws(Exception::class)
    private fun loadBoardGame(bg: BoardGameModel) {
        val id = bg.id ?: return
        val file = PathManager.getGameFile(id)
        CodeUtil.log.info("加载游戏 " + bg.cnname)
        val gameClass = GameClass()
        gameClass.loader = URLClassLoader(arrayOf(file.toURI().toURL()), Thread.currentThread().contextClassLoader)
        gameClass.loadClasses(bg)
        classMap[id] = gameClass
    }

    fun getBoardGame(id: String): BoardGameModel? {
        return bgs[id]
    }

    fun getLoader(id: String): ClassLoader? {
        return classMap[id]?.loader
    }

    fun getBoardGameClass(id: String): Class<BoardGame<*, *, *>>? {
        return classMap[id]?.game
    }

    fun getPlayerClass(id: String): Class<Player>? {
        return classMap[id]?.player
    }

    fun getResourceManagerClass(id: String): Class<ResourceManager>? {
        return classMap[id]?.resourceManager
    }

    private class GameClass {
        var loader: ClassLoader? = null
        var game: Class<BoardGame<*, *, *>>? = null
        var player: Class<Player>? = null
        var resourceManager: Class<ResourceManager>? = null


        @Throws(ClassNotFoundException::class)
        fun loadClasses(bg: BoardGameModel) {
            val loader = this.loader ?: return
            this.game = loader.loadClass(bg.gameClass) as Class<BoardGame<*, *, *>>
            this.player = loader.loadClass(bg.playerClass) as Class<Player>
            this.resourceManager = loader.loadClass(bg.resourceClass) as Class<ResourceManager>
        }
    }

}
