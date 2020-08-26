package com.f14.F14bgClient.manager

import com.f14.F14bgClient.update.IUpdaterListener
import com.f14.F14bgClient.update.UpdateSuccessThread
import com.f14.F14bgClient.update.VersionUpdater
import com.f14.utils.FileUtils
import com.f14.utils.StringUtils
import org.apache.commons.net.ftp.FTPClient
import org.apache.log4j.Logger
import org.eclipse.swt.widgets.Display
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * 版本更新管理器

 * @author F14eagle
 */
class UpdateManager {
    private val lock = Any()
    private val updated = HashMap<String, Boolean>()
    private var updater: VersionUpdater? = null

    /**
     * 重置更新信息
     */
    fun reset() {
        this.updated.clear()
        this.updater = null
    }

    /**
     * 判断模块是否已经更新过

     * @param gameType
     * *
     * @return
     */
    fun isGameUpdated(gameType: String): Boolean {
        val res = this.updated[gameType]
        if (res == null) {
            return false
        } else {
            return res
        }
    }

    /**
     * 将模块设置为已经更新

     * @param gameType
     */
    private fun setGameUpdated(gameType: String) {
        this.updated.put(gameType, true)
    }

    /**
     * 客户端是否进行更新
     */
    fun needUpdate(): Boolean {
        // 该参数在config.properties中配置
        val ignoreUpdate = ManagerContainer.propertiesManager.getConfigValue("ignoreUpdate")
        if (StringUtils.isEmpty(ignoreUpdate)) {
            return true
        } else {
            return "true" != ignoreUpdate
        }
    }

    /**
     * 执行模块更新

     * @param gameType
     * *
     * @param listener
     */
    fun executeUpdate(gameType: String, listener: IUpdaterListener) {
        synchronized(lock) {
            try {
                if (this.updater == null) {
                    val v = VersionUpdater(gameType)
                    this.updater = v
                    v.addListener(listener)
                    v.update()
                    // throw new Exception("存在正在运行的更新任务!");
                }
            } catch (e: Exception) {
                log.error("更新模块出错!", e)
                this.updateFailure(e)
            }

        }
    }

    /**
     * 取得模块的版本信息字符串

     * @param gameType
     * *
     * @return
     */
    fun getVersionString(gameType: String): String {
        var sb = StringBuffer(32)
        val path = ManagerContainer.pathManager.getVersionFile(gameType)
        val file = File(path)
        // 按行读取文件内容
        try {
            if (file.isFile && file.exists()) {
                val fr = FileReader(file)
                val br = BufferedReader(fr)
                var str: String? = null
                do {
                    str = br.readLine()
                    if (str == null) {
                        break;
                    }
                    sb.append(str + "\n")
                }while (true)
                br.close()
                fr.close()
            }
        } catch (e: Exception) {
            log.error("读取版本文件时发生错误!", e)
            // 清空返回的文件内容
            sb = StringBuffer(0)
        }

        return sb.toString()
    }

    /**
     * 设置需要更新的文件列表

     * @param gameType
     * *
     * @param versionString
     * *
     * @param files
     */
    fun setUpdateFiles(gameType: String, versionString: String, files: String) {
        try {
            if (this.updater == null) {
                throw Exception("没有找到正在运行的更新任务!")
            }
            if (this.updater!!.gameType != gameType) {
                throw Exception("更新任务类型错误!")
            }
            if (StringUtils.isEmpty(files)) {
                // 如果更新文件列表为空,则不需要更新
                this.updateSuccess(false)
            } else {
                // 设置需要更新的文件信息
                val fileList = StringUtils.string2List(files)
                this.updater!!.fileList = fileList.toMutableList()
                this.updater!!.createServerVersion(versionString)
                // 显示更新界面
                Display.getDefault().asyncExec { ManagerContainer.shellManager.showUpdateShell() }
            }
        } catch (e: Exception) {
            log.error("更新模块出错!", e)
            this.updateFailure(e)
        }

    }

    /**
     * 更新结束时调用的方法
     */
    protected fun onUpdateOver() {
        this.updater = null
        /*
		 * FlashShell shell = ManagerContainer.shellManager.getCurrentShell();
		 * if(shell!=null){ shell.hideTooltips(); }
		 */
    }

    /**
     * 成功完成更新

     * @param updated 是否执行过更新
     */
    fun updateSuccess(updated: Boolean) {
        if (this.updater != null) {
            this.setGameUpdated(this.updater!!.gameType)
            this.updater!!.onSuccess(updated)
            this.onUpdateOver()
        }
    }

    /**
     * 资源读取完成

     * @param updated 是否执行过更新
     */
    fun loadResouceSuccess(updated: Boolean) {
        if (this.updater != null) {
            this.updater!!.onResourceLoaded(updated)
        }
    }

    /**
     * 更新失败

     * @param e
     */
    fun updateFailure(e: Exception?) {
        if (this.updater != null) {
            // 隐藏更新界面
            Display.getDefault().asyncExec {
                if (e != null) {
                    // 显示警告窗口
                    val message = "更新文件时发生错误!\n\n" + e.message
                    ManagerContainer.shellManager.updateShell!!.alert(message)
                }
                ManagerContainer.shellManager.hideUpdateShell()
                updater!!.onFailure()
                this@UpdateManager.onUpdateOver()
            }
        }
    }

    /**
     * 开始更新
     */
    fun updateFiles() {
        // 创建线程用以执行更新
        val t = Thread(Runnable {
            try {
                if (this@UpdateManager.updater == null) {
                    throw Exception("没有找到正在运行的更新任务!")
                }
                val fileList = this@UpdateManager.updater!!.fileList
                if (!fileList.isEmpty()) {
                    // 设置下载缓冲区为1024KB
                    val block = 1024
                    var read = 0
                    val cache = ByteArray(block)
                    // 整理出下载地址和临时文件存放的路径
                    var connection: HttpURLConnection
                    val urlpath = updater!!.urlPath
                    val temppath = updater!!.templatePath
                    val realpath = updater!!.realPath
                    // 清除临时文件夹中的内容
                    FileUtils.delFolder(temppath)
                    // 取得服务器和本地的版本信息对象
                    updater!!.loadLocalVersion()
                    val serverVersion = updater!!.serverVersion
                    val localVersion = updater!!.localVersion
                    var retry = 0
                    var i = 0
                    while (i < fileList.size) {
                        try {
                            val file = fileList[i]
                            // 将文件下载到临时文件夹中
                            val u = URL(urlpath + file)
                            // 因为老绿的服务器需要更改文件名,在此定义本地的文件名
                            var localfile = file
                            connection = createHttpURLConnection(u)
                            val `is` = connection.inputStream
                            // 如果临时文件夹不存在,则创建该文件夹
                            if (file.endsWith(".sds")) {
                                // 因为老绿的服务器不支持MP3后缀的下载,所以改名成sds供下载
                                localfile = file.replace(".sds".toRegex(), ".mp3")
                            }
                            val f = FileUtils.newFile(temppath + localfile)
                            val os = FileOutputStream(f)
                            // 发送当前下载信息到界面
                            val param = HashMap<String, String>()
                            param.put("filename", file)
                            param.put("totalSize", connection.contentLength.toString() + "")
                            param.put("totalFiles", fileList.size.toString() + "")
                            param.put("i", (i + 1).toString() + "")
                            param.put("currentSize", 0.toString() + "")
                            ManagerContainer.shellManager.updateShell!!.loadParam(param)
                            var currentSize = 0 // 已下载的大小
                            do {
                                read = `is`.read(cache)
                                if (read == -1){break}
                                os.write(cache, 0, read)
                                currentSize += read
                                ManagerContainer.shellManager.updateShell!!.refreshSize(currentSize)
                            }while (true)
                            `is`.close()
                            os.close()
                            connection.disconnect()
                            // 文件更新成功后,将其移到正式目录中,并更新本地版本信息
                            FileUtils.moveFile(temppath + localfile, realpath + localfile)
                            localVersion!!.setFileVersion(file, serverVersion!!.getFileVersion(file))
                            updater!!.saveLocalVersion()
                            i++
                        } catch (e: Exception) {
                            log.error("更新文件时发生错误!", e)
                            retry++
                            // 如果重试次数大于3,则抛出异常
                            if (retry > 2) {
                                throw e
                            }
                        }

                    }
                    // 下载完成后,将临时文件夹里的文件移动到正式的文件夹

                    // FileUtils.moveFolder(temppath, realpath);

                    // 更新模块版本文件的内容到最新
                    // String versionpath = updater.getVersionFilePath();
                    // FileUtils.newFile(versionpath,
                    // UpdateManager.this.updater.getVersionString());

                    // 更新本地版本号
                    updater!!.refreshLocalModuleVersion()
                    updater!!.saveLocalVersion()
                }

                // 完成更新
                Display.getDefault().asyncExec(UpdateSuccessThread())
            } catch (e: Exception) {
                log.error("更新模块出错!", e)
                updateFailure(e)
            } finally {
                // 无论如何,将本地版本信息写入文件
                // updater.saveLocalVersion();
            }
        })
        t.start()
    }

    /**
     * 创建Http连接

     * @param url
     * *
     * @return
     * *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createHttpURLConnection(url: URL): HttpURLConnection {
        val proxy = ManagerContainer.connectionManager.httpProxy
        // 检查是否使用代理
        if (proxy == null) {
            return url.openConnection() as HttpURLConnection
        } else {
            return url.openConnection(proxy) as HttpURLConnection
        }
    }

    /**
     * 取得版本信息

     * @param gameType
     * *
     * @return
     */
    fun getVersionInfo(gameType: String): Map<String, String> {
        val res = HashMap<String, String>()
        val path = ManagerContainer.pathManager.getVersionFile(gameType)
        try {
            val br = BufferedReader(FileReader(path))
            val version = br.readLine()
            res.put("version", version)
            // 同时取得游戏中文名称
            var title: String? = ManagerContainer.codeManager.getCodeLabel("BOARDGAME", gameType)
            if (StringUtils.isEmpty(title)) {
                title = "F14桌游"
            }
            res.put("title", title!!)
            br.close()
        } catch (e: Exception) {
            log.error("读取版本信息时发生错误!", e)
        }

        return res
    }

    companion object {
        protected var log = Logger.getLogger(UpdateManager::class.java)

        @Throws(IOException::class)
        @JvmStatic fun main(args: Array<String>) {
            val client = FTPClient()
            client.connect("www.joylink.me", 21)
            client.login("joylink@ynhuiguan.com", "joylink1359")

            // client.setBufferSize(1024);
            client.setFileType(FTPClient.BINARY_FILE_TYPE)

            val remoteFile = "/f14/client/f14hall.html"

            client.retrieveFile(remoteFile, FileOutputStream("d:/f14hall.html"))

            client.disconnect()
        }
    }
}
