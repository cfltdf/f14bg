package com.f14.F14bgClient.update

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.ArrayList
import java.util.LinkedHashMap

/**
 * 模块版本对象

 * @author F14eagle
 */
class ModuleVersion(moduleName: String) {
    var moduleName: String
        protected set
    lateinit var moduleVersion: String
    protected var fileVersions: MutableMap<String, FileVersion> = LinkedHashMap()
    var moduleFileContent = StringBuffer(32)
        protected set

    init {
        this.moduleName = moduleName
    }

    /**
     * 从文件中装载版本信息

     * @param file
     * *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun loadFile(file: File) {
        val reader = FileReader(file)
        val br = BufferedReader(reader)
        var str: String? = null
        do {
            str = br.readLine()
            if (str == null){
                break
            }
            this.moduleFileContent.append(str!! + "\n")
        }while (true)
        br.close()
        reader.close()
        this.loadFromContent()
    }

    /**
     * 从字符串装载版本信息

     * @param str
     */
    fun loadFromString(str: String) {
        this.moduleFileContent = StringBuffer(str)
        this.loadFromContent()
    }

    /**
     * 从版本内容装载版本信息
     */
    protected fun loadFromContent() {
        val line = this.moduleFileContent.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in line.indices) {
            if (i == 0) {
                // 第一行为模块的版本号
                this.moduleVersion = line[i].trim { it <= ' ' }
            } else {
                // 起始#的被注释了
                if (!line[i].startsWith("#")) {
                    // 文件版本格式为 文件路径|子版本号
                    val s = line[i].trim { it <= ' ' }.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (s.size == 2) {
                        val v = FileVersion()
                        v.path = s[0].trim { it <= ' ' }
                        v.version = s[1].trim { it <= ' ' }
                        this.addFileVersion(v)
                    }
                }
            }
        }
    }

    /**
     * 添加子文件版本

     * @param v
     */
    protected fun addFileVersion(v: FileVersion) {
        this.fileVersions.put(v.path!!, v)
    }

    /**
     * 取得子文件的版本

     * @param path
     * *
     * @return
     */
    fun getFileVersion(path: String): String? {
        val fv = this.fileVersions[path]
        if (fv == null) {
            return null
        } else {
            return fv.version
        }
    }

    /**
     * 设置子文件的版本

     * @param path
     * *
     * @param version
     */
    fun setFileVersion(path: String, version: String?) {
        var v: FileVersion? = this.fileVersions[path]
        if (v == null) {
            v = FileVersion()
            v.path = path
            this.fileVersions.put(path, v)
        }
        v.version = version
    }

    /**
     * 取得当前版本中和入参版本不同子版本的文件名列表

     * @param v
     * *
     * @return
     */
    fun getDifferentFiles(v: ModuleVersion): List<String> {
        val res = ArrayList<String>()
        for (o in this.fileVersions.values) {
            if (o.version != v.getFileVersion(o.path!!)) {
                res.add(o.path!!)
            }
        }
        return res
    }

    /**
     * 将版本信息转换成字符串

     * @return
     */
    fun toVersionString(): String {
        val n = "\n"
        val s = "|"
        val sb = StringBuffer(32)
        sb.append(this.moduleVersion).append(n)
        for (v in this.fileVersions.values) {
            sb.append(v.path).append(s).append(v.version).append(n)
        }
        return sb.toString()
    }

    /**
     * 模块中的文件版本对象

     * @author F14eagle
     */
    protected class FileVersion {
        internal var path: String? = null
        internal var version: String? = null
    }
}
