package com.f14.F14bgClient

import org.apache.log4j.Logger
import org.eclipse.swt.widgets.Display

import com.f14.F14bgClient.component.FlashShell
import com.f14.F14bgClient.manager.ManagerContainer

class F14bgClient
/**
 * 取得客户端版本信息

 * @return
 */
// public static Version getVersion(){
// return version;
// }

private constructor() {
    protected var log = Logger.getLogger(this.javaClass)

    init {
        this.init()
    }

    protected fun init() {
        // 初始化服务对象
    }

    fun run() {
        try {
            val display = Display.getDefault()
            ManagerContainer.shellManager.showLoginShell()
            while (!ManagerContainer.shellManager.isDisposed) {
                if (!display.readAndDispatch()) {
                    display.sleep()
                }
            }
            System.exit(0)
        } catch (e: Exception) {
            log.error(e, e)
        }

    }

    /**
     * 发送错误信息

     * @param roomId
     * *
     * @param msg
     */
    fun sendErrorMessage(roomId: Int, msg: String) {
        val shell = ManagerContainer.shellManager.getShell(roomId)
        if (shell != null && !shell.isDisposed) {
            shell.sendErrorMessage(msg)
        }
    }

    /**
     * 发送指令

     * @param roomId
     * *
     * @param cmdstr
     */
    fun sendCommand(roomId: Int, cmdstr: String) {
        val shell = ManagerContainer.shellManager.getShell(roomId)
        if (shell != null && !shell.isDisposed) {
            shell.sendCommand(cmdstr)
        }
    }

    companion object {
        // private static final Version version;
        /**
         * 取得客户端实例

         * @return
         */
        val instance: F14bgClient = F14bgClient()

        @JvmStatic fun main(args: Array<String>) {
            val c = F14bgClient.instance
            c.run()
        }
    }

}
