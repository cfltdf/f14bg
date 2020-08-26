package com.f14.F14bgClient.component

import org.eclipse.swt.SWT
import org.eclipse.swt.events.ShellEvent
import org.eclipse.swt.events.ShellListener
import org.eclipse.swt.widgets.Display

import com.f14.F14bgClient.manager.ManagerContainer
import com.f14.net.smartinvoke.QueryCommandHandler

/**
 * 用户信息界面

 * @author F14eagle
 */
class UserShell(shellId: String, display: Display) : FlashShell(shellId, display, SWT.TITLE or SWT.CLOSE or SWT.ON_TOP) {
    lateinit protected var commandHandler: QueryCommandHandler
    var currentUserId: Long? = null

    override fun init() {
        super.init()
        this.text = "F14桌游 - 查看用户"
        this.setSize(500, 350)
        /**
         * 添加shell事件监听器
         */
        this.addShellListener(UserShellListener())
    }

    /**
     * 本方法只能装载登录界面
     */
    override fun loadFlash(path: String) {
        this.loadUI()
    }

    /**
     * 装载界面
     */
    fun loadUI() {
        super.loadFlash(ManagerContainer.pathManager.loaderPath)
    }

    override fun onConnect() {
        super.onConnect()
        commandHandler = QueryCommandHandler(fc!!)
        clientCmdHandler!!.loadModule(ManagerContainer.pathManager.viewUserPath)
    }

    /**
     * 装载用户信息参数

     * @param paramString
     */
    fun loadUserParam(paramString: String) {
        this.commandHandler.loadUserParam(paramString)
    }

    /**
     * 用户信息窗口的事件监听器

     * @author F14eagle
     */
    protected inner class UserShellListener : ShellListener {

        override fun shellActivated(arg0: ShellEvent) {

        }

        /**
         * 点击关闭按钮时触发的事件
         */
        override fun shellClosed(e: ShellEvent) {
            // 不允许dispose,只能隐藏该窗口
            ManagerContainer.shellManager.hideUserShell()
            e.doit = false
        }

        override fun shellDeactivated(arg0: ShellEvent) {

        }

        override fun shellDeiconified(arg0: ShellEvent) {

        }

        override fun shellIconified(arg0: ShellEvent) {

        }

    }

}
