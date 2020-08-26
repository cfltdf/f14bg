package com.f14.F14bg.utils

import com.f14.F14bg.version.ModuleVersion
import com.f14.bg.utils.BgUtils
import org.apache.log4j.Logger
import java.io.File
import java.io.FilenameFilter
import java.util.*

/**
 * 更新管理器

 * @author F14eagle
 */
object UpdateUtil {
    /**
     * 版本文件的后缀
     */
    const val VERSION_FILE_POSTFIX = ".ver"
    private val log = Logger.getLogger(UpdateUtil::class.java)
    private lateinit var moduleVersions: MutableMap<String, ModuleVersion>

    /**
     * 添加模块版本

     * @param v
     */
    private fun addModuleVersion(v: ModuleVersion) {
        moduleVersions[v.moduleName] = v
    }

    /**
     * 取得模块版本

     * @param moduleName

     * @return
     */
    private fun getModuleVersion(moduleName: String): ModuleVersion? {
        return moduleVersions[moduleName]
    }

    /**
     * 按照指定的模块和版本信息取得需要更新的文件列表
     * @param moduleName
     * @param versionString
     * @return
     */
    fun getUpdateList(moduleName: String, versionString: String): List<String> {
        val v = getModuleVersion(moduleName)
        val c = ModuleVersion(moduleName)
        c.loadFromString(versionString)
        // 只检查单个文件的子版本
        // if(!v.getModuleVersion().equals(c.getModuleVersion())){
        return v!!.getDifferentFiles(c)
    }

    /**
     * 取得指定模块的版本信息字符串
     * @param moduleName
     * @return
     */
    fun getVersionString(moduleName: String): String {
        val v = getModuleVersion(moduleName)
        return v?.moduleFileContent?.toString() ?: ""
    }

    /**
     * 初始化模块版本

     * @throws Exception
     */
    @Throws(Exception::class)
    fun init() {
        moduleVersions = HashMap()
        // 装载version目录下的所有版本信息
        log.info("装载游戏版本信息...")
        val dir = BgUtils.getFile("./version/")
        if (!dir.exists() || !dir.isDirectory) {
            throw Exception("游戏版本信息文件夹不存在!")
        }
        val files = dir.listFiles(VersionFileFilter()) ?: throw Exception("游戏版本信息文件夹不存在!")
        for (f in files) {
            // 文件名去后缀就是模块名称
            val filename = f.name.substring(0, f.name.lastIndexOf("."))
            val v = ModuleVersion(filename)
            v.loadFile(f)
            addModuleVersion(v)
        }
        log.info("装载游戏版本信息完成!")
    }

    /**
     * 判断指定的模块是否需要更新

     * @param moduleName

     * @param version

     * @return
     */
    fun needUpdate(moduleName: String, version: String): Boolean {
        val v = getModuleVersion(moduleName)
        // 如果没有找到版本信息,或者版本信息相同,则不需要更新
        return !(v == null || v.moduleVersion == version)
    }

    /**
     * 版本文件过滤器

     * @author F14eagle
     */
    private class VersionFileFilter : FilenameFilter {

        override fun accept(dir: File, name: String): Boolean {
            // 取得所有后缀为ver的文件
            return name.toLowerCase().endsWith(VERSION_FILE_POSTFIX)
        }

    }
}
