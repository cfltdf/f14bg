package com.f14.F14bgClient.update

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.F14bgClient.manager.ManagerContainer
import com.f14.F14bgClient.manager.PathManager
import com.f14.utils.FileUtils
import org.apache.log4j.Logger
import java.io.File
import java.util.*

/**
 * 版本更新器

 * @author F14eagle
 */
class VersionUpdater(gameType: String) {
    var gameType: String
        protected set
    protected var listeners: MutableList<IUpdaterListener> = ArrayList<IUpdaterListener>()
    var fileList: MutableList<String> = ArrayList()
        set(files) {
            field.clear()
            field.addAll(files)
        }

    protected var versionString: String? = null
    /**
     * 取得服务器端的版本信息对象

     * @return
     */
    var serverVersion: ModuleVersion? = null
        protected set
    /**
     * 取得本地版本信息

     * @return
     */
    var localVersion: ModuleVersion? = null
        protected set

    init {
        this.gameType = gameType
    }

    /**
     * 添加监听器

     * @param listener
     */
    fun addListener(listener: IUpdaterListener) {
        this.listeners.add(listener)
    }

    /**
     * 执行更新
     */
    fun update() {
        // 尝试装载游戏资源
        val success = ManagerContainer.resourceManager.loadResource(gameType)
        if (success) {
            // 如果读取完成,则继续执行更新
            this.onResourceLoaded(false)
        } else {
            // 如果没有完成则等待资源读取完成后继续执行更新
            ManagerContainer.shellManager.hallShell!!.showTooltips("装载游戏资源信息...", 0.0)
        }

        // 更新时先从服务器装载资源字符串
        // ManagerContainer.resourceManager.loadResource(this.gameType);
        // 暂时直接返回成功事件
        // for(IUpdaterListener listener : this.listeners){
        // listener.onUpdateSuccess();
        // }
    }

    /**
     * 资源读取完成后,检查文件更新

     * @param updated
     */
    fun onResourceLoaded(updated: Boolean) {
        // 如果客户端不需要执行更新,则直接跳过更新
        if (!ManagerContainer.updateManager.needUpdate()) {
            // listener.onUpdateSuccess(false);
            // this.onSuccess(false);
            ManagerContainer.updateManager.updateSuccess(false)
            return
        }
        // 如果该模块已经更新过,则不用再次更新
        if (ManagerContainer.updateManager.isGameUpdated(gameType)) {
            // listener.onUpdateSuccess(false);
            // this.onSuccess(false);
            ManagerContainer.updateManager.updateSuccess(false)
            return
        }
        val shell = ManagerContainer.shellManager.currentShell
        if (shell === ManagerContainer.shellManager.hallShell) {
            shell!!.showTooltips("检查游戏版本信息...", 0.0)
        }
        this.sendUpdateRequest()
    }

    /**
     * 更新成功

     * @param 是否执行过更新
     */
    fun onSuccess(updated: Boolean) {
        for (listener in this.listeners) {
            listener.onUpdateSuccess(updated)
        }
    }

    /**
     * 更新失败
     */
    fun onFailure() {
        for (listener in this.listeners) {
            listener.onUpdateFailure()
        }
    }

    /**
     * 发送更新的请求
     */
    protected fun sendUpdateRequest() {
        val res = CmdFactory.createClientResponse(CmdConst.CLIENT_CHECK_UPDATE)
        val versionString = ManagerContainer.updateManager.getVersionString(gameType)
        res.setPublicParameter("gameType", gameType)
        res.setPublicParameter("versionString", versionString)
        ManagerContainer.connectionManager.sendResponse(res)
    }

    /**
     * 创建服务器端的版本信息对象

     * @param versionString
     */
    fun createServerVersion(versionString: String) {
        this.serverVersion = ModuleVersion(this.gameType)
        this.serverVersion!!.loadFromString(versionString)
    }

    /**
     * 取得下载URL的路径

     * @return
     */
    val urlPath: String
        get() {
            var res = ManagerContainer.propertiesManager.getLocalProperty("update_host")
            if (PathManager.MAIN_APP != this.gameType) {
                res += this.gameType + "/"
            }
            return res
        }

    /**
     * 取得临时下载文件夹的路径

     * @return
     */
    val templatePath: String
        get() {
            val temppath = ManagerContainer.pathManager.getTemplatePath(this.gameType)
            return temppath
        }

    /**
     * 取得正式文件夹的路径

     * @return
     */
    val realPath: String
        get() {
            val res = ManagerContainer.pathManager.getBasePath(this.gameType)
            return res
        }

    /**
     * 取得版本文件的路径

     * @return
     */
    val versionFilePath: String
        get() {
            val res = ManagerContainer.pathManager.getVersionFile(this.gameType)
            return res
        }

    /**
     * 装载本地版本信息
     */
    fun loadLocalVersion() {
        this.localVersion = ModuleVersion(this.gameType)
        val path = this.versionFilePath
        val f = File(path)
        if (f.exists() && f.isFile) {
            try {
                this.localVersion!!.loadFile(f)
                return
            } catch (e: Exception) {
                log.error("装载本地版本信息失败!", e)
            }

        }
        // 如果不存在本地版本信息,或者装载失败,则设置其默认版本号
        this.localVersion!!.moduleVersion = "0"
    }

    /**
     * 保存本地版本信息到文件中
     */
    fun saveLocalVersion() {
        FileUtils.newFile(this.versionFilePath, this.localVersion!!.toVersionString())
    }

    /**
     * 刷新本地版本号为服务器端版本号
     */
    fun refreshLocalModuleVersion() {
        if (this.localVersion != null && this.serverVersion != null) {
            this.localVersion!!.moduleVersion = this.serverVersion!!.moduleVersion
        }
    }

    companion object {
        protected var log = Logger.getLogger(VersionUpdater::class.java)
    }
}
