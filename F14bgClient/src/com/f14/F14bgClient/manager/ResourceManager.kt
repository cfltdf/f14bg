package com.f14.F14bgClient.manager

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import net.sf.json.JSONArray
import org.apache.log4j.Logger
import java.util.*

/**
 * 游戏资源管理器

 * @author F14eagle
 */
class ResourceManager {
    protected var log = Logger.getLogger(this.javaClass)
    protected var resStrings: MutableMap<String, String> = HashMap()
    protected var locks: MutableMap<String, Object> = HashMap()

    var replayResourceString: String? = null
    var replayResponse: JSONArray? = null

    /**
     * 取得指定游戏类型的资源字符串

     * @param gameType
     * *
     * @return
     */
    fun getResourceString(gameType: String): String? {
        return this.resStrings[gameType]
    }

    /**
     * 从服务器读取指定游戏类型的资源字符串,该方法会等待到服务器回应

     * @param gameType
     * *
     * @return 返回是否完成读取
     */
    fun loadResource(gameType: String): Boolean {
        val str = this.getResourceString(gameType)
        if (str == null && PathManager.MAIN_APP != gameType) {
            // 如果没有读取过资源,则从服务器读取该资源信息
            val res = CmdFactory.createClientResponse(CmdConst.CLIENT_INIT_RESOURCE)
            res.setPublicParameter("gameType", gameType)
            ManagerContainer.connectionManager.sendResponse(res)
            /*
			 * try { Object lock = this.getLock(gameType); synchronized (lock) {
			 * BgResponse res =
			 * CmdFactory.createClientResponse(CmdConst.CLIENT_INIT_RESOURCE);
			 * res.setPublicParameter("gameType", gameType);
			 * ManagerContainer.connectionManager.sendResponse(res);
			 * //等待到读取完成后才继续执行 lock.wait(); } } catch (Exception e) {
			 * log.error("装载资源字符串发生错误!", e); }
			 */
            return false
        } else {
            return true
        }
    }

    /**
     * 取得游戏类型锁

     * @param gameType
     * *
     * @return
     */
    protected fun getLock(gameType: String): Object {
        var res: Object? = this.locks[gameType]
        if (res == null) {
            res = Object()
            this.locks.put(gameType, res)
        }
        return res
    }

    /**
     * 解除指定的游戏类型锁

     * @param gameType
     */
    fun notifyLock(gameType: String) {
        val lock = this.getLock(gameType)
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    /**
     * 设置资源字符串

     * @param gameType
     * *
     * @param resString
     */
    fun setResourceString(gameType: String, resString: String) {
        this.resStrings.put(gameType, resString)
    }


}
