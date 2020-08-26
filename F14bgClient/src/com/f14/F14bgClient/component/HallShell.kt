package com.f14.F14bgClient.component

import org.eclipse.swt.SWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.events.ShellEvent
import org.eclipse.swt.events.ShellListener
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.MessageBox
import org.eclipse.swt.widgets.Shell

import com.f14.F14bgClient.manager.ManagerContainer

/**
 * 大厅界面

 * @author F14eagle
 */
class HallShell(shellId: String, display: Display) : FlashShell(shellId, display) {

    override fun init() {
        super.init()
        this.text = "F14桌游大厅"
        this.maximized = true

        /**
         * 添加shell事件监听器
         */
        this.addShellListener(HallShellListener())
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
        clientCmdHandler!!.loadModule(ManagerContainer.pathManager.hallPath)
    }

    /**
     * 大厅窗口的事件监听器

     * @author F14eagle
     */
    protected inner class HallShellListener : ShellListener {

        override fun shellActivated(arg0: ShellEvent) {

        }

        /**
         * 点击关闭按钮时触发的事件
         */
        override fun shellClosed(e: ShellEvent) {
            // 弹出提示框提示玩家是否要关闭程序
            val messagebox = MessageBox(this@HallShell, SWT.ICON_QUESTION or SWT.YES or SWT.NO)
            messagebox.text = "F14桌游大厅"
            messagebox.message = "您确定要关闭F14桌游大厅吗?"
            val message = messagebox.open()
            val res = message == SWT.YES
            if (res) {
                // 如果选是,则销毁登录窗口,程序将自动关闭
                ManagerContainer.shellManager.loginShell!!.dispose()
            }
            e.doit = res
        }

        override fun shellDeactivated(arg0: ShellEvent) {

        }

        override fun shellDeiconified(arg0: ShellEvent) {

        }

        override fun shellIconified(arg0: ShellEvent) {

        }

    }

    /**
     * 显示大厅的公告信息
     */
    fun showHallNotice() {
        val shell = Shell(this, SWT.DIALOG_TRIM or SWT.PRIMARY_MODAL or SWT.BORDER)
        shell.text = "欢迎来到F14桌游平台"
        shell.layout = FillLayout()
        shell.setSize(750, 400)
        // 居中显示
        val displayBounds = Display.getDefault().primaryMonitor.bounds
        val shellBounds = shell.bounds
        val x = displayBounds.x + (displayBounds.width - shellBounds.width) shr 1
        val y = displayBounds.y + (displayBounds.height - shellBounds.height) shr 1
        shell.setLocation(x, y)
        // 添加大厅公告的浏览器部件
        val url = ManagerContainer.propertiesManager.getLocalProperty("update_host") + "f14hall.html"
        val browser = Browser(shell, SWT.NONE)
        browser.url = url
        // 显示窗口
        shell.open()
    }

}
