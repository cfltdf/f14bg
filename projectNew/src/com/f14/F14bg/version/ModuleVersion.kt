package com.f14.F14bg.version


import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

/**
 * 模块版本对象
 * @author F14eagle
 */
class ModuleVersion(val moduleName: String) {
    lateinit var moduleVersion: String
        private set
    private var fileVersions: MutableMap<String, FileVersion> = LinkedHashMap()
    var moduleFileContent = StringBuffer(32)
        private set

    /**
     * 添加子文件版本
     * @param v
     */
    private fun addFileVersion(v: FileVersion) {
        this.fileVersions[v.path] = v
    }

    /**
     * 取得当前版本中和入参版本不同子版本的文件名列表
     * @param v
     * @return
     */
    fun getDifferentFiles(v: ModuleVersion): List<String> {
        return this.fileVersions.values.filterNot { it.version == v.getFileVersion(it.path) }.map(FileVersion::path)
        val res = ArrayList<String>()
        for (o in this.fileVersions.values) {
            if (o.version != v.getFileVersion(o.path)) {
                res.add(o.path)
            }
        }
        return res
    }

    /**
     * 取得子文件的版本
     * @param path
     * @return
     */
    fun getFileVersion(path: String?): String? {
        return this.fileVersions[path]?.version
    }

    /**
     * 从文件中装载版本信息
     * @param file
     * @throws Exception
     */
    @Throws(Exception::class)
    fun loadFile(file: File) {
        val reader = FileReader(file)
        val br = BufferedReader(reader)
        for (str in br.lineSequence()) {
            this.moduleFileContent.append(str).append("\n")
        }
        br.close()
        reader.close()
        this.loadFromContent()
    }

    /**
     * 从版本内容装载版本信息
     */
    private fun loadFromContent() {
        val line = this.moduleFileContent.toString().split("\n".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        for ((i, l) in line.withIndex()) {
            if (i == 0) {
                // 第一行为模块的版本号
                this.moduleVersion = line[i].trim { it <= ' ' }
            } else {
                // 起始#的被注释了
                if (!l.startsWith("#")) {
                    // 文件版本格式为 文件路径|子版本号
                    val s = l.trim { it <= ' ' }.split("\\|".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
                    if (s.size == 2) {
                        val v = FileVersion(s[0].trim { it <= ' ' }, s[1].trim { it <= ' ' })
                        this.addFileVersion(v)
                    }
                }
            }
        }
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
     * 设置子文件的版本
     * @param path
     * @param version
     */
    fun setFileVersion(path: String, version: String) {
        var v: FileVersion? = this.fileVersions[path]
        if (v == null) {
            v = FileVersion(path, version)
            this.fileVersions[path] = v
        }
        v.version = version
    }

    /**
     * 将版本信息转换成字符串
     * @return
     */
    fun toVersionString(): String {
        val n = "\n"
        val s = "|"
        val sb = StringBuilder(32)
        sb.append(this.moduleVersion).append(n)
        for (v in fileVersions.values) {
            sb.append(v.path).append(s).append(v.version).append(n)
        }
        return sb.toString()
    }

    /**
     * 模块中的文件版本对象
     * @author F14eagle
     */
    private class FileVersion(var path: String, var version: String)
}
