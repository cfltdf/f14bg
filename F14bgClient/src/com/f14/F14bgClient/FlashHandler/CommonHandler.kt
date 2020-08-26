package com.f14.F14bgClient.FlashHandler

import cn.smartinvoke.IServerObject
import com.f14.F14bgClient.User
import com.f14.F14bgClient.manager.ManagerContainer
import com.f14.F14bgClient.vo.CodeDetail
import com.f14.bg.action.BgResponse
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import net.sf.json.JSONString
import net.sf.json.util.JSONStringer

/**
 * 通用指令接收器

 * @author F14eagle
 */
class CommonHandler : IServerObject {

    /**
     * 取得本地用户信息

     * @return
     */
    val localUser: User
        get() = ManagerContainer.connectionManager.localUser ?: throw Exception("没登录!")

    /**
     * 读取文件

     * @param gameType
     * *
     * @param file
     * *
     * @return
     */
    fun loadFile(gameType: String, file: String): ByteArray {
        try {
            return ManagerContainer.fileManager.loadFile(gameType, file)
        } catch (e: Exception) {
            e.printStackTrace()
            return ByteArray(0)
        }

    }

    fun nextReplay(): String? {
        ManagerContainer.resourceManager.replayResponse?.let { arr ->
            if (arr.isNotEmpty()){
                val next = arr.removeAt(0) as JSONArray
                println(next.toString())
                return next.toString()
            }
        }
        return null
    }

    /**
     * 读取游戏的资源字符串

     * @param gameType
     */
    fun loadReplayResourceString(): String {
        val res = ManagerContainer.resourceManager.replayResourceString ?: throw Exception("Invalid Type")
        /*
		 * if(res==null){ //如果没有取到,则从服务器装载资源字符串
		 * ManagerContainer.resourceManager.loadResource(gameType); res =
		 * ManagerContainer.resourceManager.getResourceString(gameType); }
		 */
        return res
    }

    /**
     * 读取游戏的资源字符串

     * @param gameType
     */
    fun loadResourceString(gameType: String): String {
        val res = ManagerContainer.resourceManager.getResourceString(gameType) ?: throw Exception("Invalid Type")
        /*
		 * if(res==null){ //如果没有取到,则从服务器装载资源字符串
		 * ManagerContainer.resourceManager.loadResource(gameType); res =
		 * ManagerContainer.resourceManager.getResourceString(gameType); }
		 */
        return res
    }

    /**
     * 装载本地参数字符串

     * @return
     */
    fun loadLocalProperties(): String {
        val prop = ManagerContainer.propertiesManager.localProperties
        return JSONObject.fromObject(prop).toString()
    }

    /**
     * 取得本地参数

     * @param key
     * *
     * @return
     */
    fun getLocalProperty(key: String): String {
        return ManagerContainer.propertiesManager.getLocalProperty(key)
    }

    /**
     * 保存本地参数

     * @param key
     * *
     * @param value
     */
    fun saveLocalProperty(key: String, value: String) {
        ManagerContainer.propertiesManager.saveLocalProperty(key, value)
    }

    /**
     * 保存本地参数

     * @param param
     */
    fun saveLocalProperties(param: Map<String, String>) {
        for ((key, value) in param) {
            ManagerContainer.propertiesManager.saveLocalProperty(key, value)
        }
    }

    /**
     * 取得配置参数

     * @param key
     * *
     * @return
     */
    fun getConfigValue(key: String): String {
        return ManagerContainer.propertiesManager.getConfigValue(key)
    }

    /**
     * 关闭连接
     */
    fun closeConnection() {
        ManagerContainer.connectionManager.close()
    }

    /**
     * 取得客户端版本

     * @return
     */
    fun getVersionInfo(gameType: String): Map<String, String> {
        return ManagerContainer.updateManager.getVersionInfo(gameType)
    }

    /**
     * 取得指定类型的系统代码

     * @param codeType
     * *
     * @return
     */
    fun getCodes(codeType: String): List<CodeDetail> {
        return ManagerContainer.codeManager.getCodes(codeType)
    }

    /**
     * 取得指定类型的系统代码

     * @param codeType
     * *
     * @return
     */
    fun getCodeLabel(codeType: String, codeValue: String): String {
        return ManagerContainer.codeManager.getCodeLabel(codeType, codeValue)?: ""
    }

    /**
     * 查看用户信息

     * @param userId
     */
    fun viewUser(userId: String) {
        if (ManagerContainer.shellManager.userShell == null) {
            // 如果用户窗口还未初始化,则初始化用户信息窗口,并由窗口调用查看用户的方法
            ManagerContainer.shellManager.createUserShell(userId.toLong())
        } else {
            // 否则就直接查询用户信息
            ManagerContainer.actionManager.viewUser(userId.toLong())
        }
    }

    override fun dispose() {

    }

}
