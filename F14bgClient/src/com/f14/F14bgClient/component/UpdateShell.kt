package com.f14.F14bgClient.component

import org.eclipse.swt.SWT
import org.eclipse.swt.events.ShellEvent
import org.eclipse.swt.events.ShellListener
import org.eclipse.swt.widgets.Display

import com.f14.F14bgClient.manager.ManagerContainer
import com.f14.net.smartinvoke.UpdateCommandHandler

/**
 * 更新模块界面

 * @author F14eagle
 */
class UpdateShell(shellId: String, display: Display) : FlashShell(shellId, display, SWT.TITLE or SWT.CLOSE or SWT.APPLICATION_MODAL) {
    lateinit protected var commandHandler: UpdateCommandHandler

    override fun init() {
        super.init()
        this.text = "F14桌游 - 版本更新"
        this.setSize(480, 110)
        /**
         * 添加shell事件监听器
         */
        this.addShellListener(UpdateShellListener())
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
        commandHandler = UpdateCommandHandler(fc!!)
        clientCmdHandler!!.loadModule(ManagerContainer.pathManager.updatePath)
    }

    /**
     * 装载页面参数

     * @param param
     */
    fun loadParam(param: Map<String, String>) {
        this.commandHandler.loadParam(param)
    }

    /**
     * 刷新已下载大小

     * @param currentSize
     */
    fun refreshSize(currentSize: Int) {
        this.commandHandler.refreshSize(currentSize)
    }

    /**
     * 大厅窗口的事件监听器

     * @author F14eagle
     */
    protected inner class UpdateShellListener : ShellListener {

        override fun shellActivated(arg0: ShellEvent) {

        }

        /**
         * 点击关闭按钮时触发的事件
         */
        override fun shellClosed(e: ShellEvent) {
            // 弹出提示框提示玩家是否要关闭程序
            /*
			 * MessageBox messagebox = new MessageBox(UpdateShell.this,
			 * SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			 * messagebox.setText(UpdateShell.this.getText());
			 * messagebox.setMessage("您确定要退出更新吗?") ; int message =
			 * messagebox.open(); boolean res = (message == SWT.YES); e.doit =
			 * res;
			 */
            // 不允许中断,哈哈
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
