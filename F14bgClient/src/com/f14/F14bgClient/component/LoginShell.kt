package com.f14.F14bgClient.component

import org.eclipse.swt.SWT
import org.eclipse.swt.browser.Browser
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Layout

import cn.smartinvoke.gui.FlashContainer

import com.f14.F14bgClient.manager.ManagerContainer

/**
 * 登录界面

 * @author F14eagle
 */
class LoginShell(shellId: String, display: Display) : FlashShell(shellId, display, SWT.TITLE or SWT.CLOSE or SWT.MIN) {
    protected var browser: Browser? = null

    protected override val defaultLayout: Layout?
        get() = null

    override fun createFlashContainer(): FlashContainer {
        val res = super.createFlashContainer()
        res.setBounds(0, 0, 745, 525)
        return res
    }

    override fun init() {
        super.init()
        this.text = "F14桌游平台"
        this.setSize(750, 550)

        // 创建显示公告用的浏览器部件
        // this.browser = new Browser(this, SWT.NONE);
        // this.browser.setBounds(15, 20, 350, 435);
        // this.browser.setUrl("http://www.google.com");
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
        // 装载完成后,创建客户端指令处理器
        super.onConnect()
        clientCmdHandler!!.loadModule(ManagerContainer.pathManager.loginPath)
        // clientCmdHandler.onConnection();
    }

    /**
     * 装载公告
     */
    fun loadNotice() {
        var url = ManagerContainer.propertiesManager.getLocalProperty("update_host")
        url += "f14notice.html"
        this.browser!!.url = url
    }

}
