package com.f14.F14bgClient.FlashHandler

import org.eclipse.swt.widgets.Display

import cn.smartinvoke.IServerObject

import com.f14.F14bgClient.F14bgClient
import com.f14.F14bgClient.manager.ManagerContainer
import com.f14.F14bgClient.manager.PathManager
import com.f14.F14bgClient.update.ApplicationUpdateSuccessThread
import com.f14.F14bgClient.update.DefaultUpdaterListener

class SystemHandler : IServerObject {

    fun loadServerList() {

    }

    /**
     * 读取公告
     */
    fun loadNotice() {
        ManagerContainer.shellManager.loginShell!!.loadNotice()
    }

    /**
     * 连接到服务器

     * @param host
     * *
     * @param port
     */
    fun connectToServer(host: String, port: Int) {
        val t = Thread(Runnable {
            try {
                ManagerContainer.connectionManager.connect(host, port)
            } catch (e: Exception) {
                F14bgClient.instance.sendErrorMessage(0, "服务器连接失败! " + e.message)
            }
        })
        t.start()
    }

    /**
     * 成功登录后的执行的方法
     */
    fun onLogin() {
        // 隐藏登录界面
        ManagerContainer.shellManager.hideLoginShell()
        // 检查主模块的更新情况
        ManagerContainer.updateManager.executeUpdate(PathManager.MAIN_APP, object : DefaultUpdaterListener() {
            override fun onUpdateSuccess(updated: Boolean) {
                println("onUpdateSuccess: " + updated)
                if (updated) {
                    // 如果执行过更新,则需要重新启动应用,因为可能更新了主框架的内容
                    Display.getDefault().asyncExec(ApplicationUpdateSuccessThread())
                } else {
                    Display.getDefault().asyncExec {
                        // 装载系统代码,并等待代码载入完成
                        ManagerContainer.codeManager.loadAllCodes()
                    }

                    Display.getDefault().asyncExec {
                        // 切换界面到大厅
                        ManagerContainer.shellManager.showHallShell()
                    }
                }
            }

            override fun onUpdateFailure() {
                // 如果更新失败,则切断连接
                ManagerContainer.connectionManager.close()
            }
        })
    }

    override fun dispose() {

    }

}
