package com.f14.F14bgClient.component

import java.util.ArrayList

import org.apache.log4j.Logger
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Layout
import org.eclipse.swt.widgets.MessageBox
import org.eclipse.swt.widgets.Shell

import cn.smartinvoke.gui.FlashContainer
import cn.smartinvoke.gui.ILoadCompleteListener

import com.f14.F14bgClient.event.F14bgEvent
import com.f14.F14bgClient.event.FlashShellEvent
import com.f14.F14bgClient.event.FlashShellListener
import com.f14.F14bgClient.manager.ManagerContainer
import com.f14.F14bgClient.manager.PathManager
import com.f14.net.smartinvoke.ClientCmdHandler

/**
 * 装载Flash文件的Shell窗口

 * @author F14eagle
 */
open class FlashShell : Shell {
    protected var log = Logger.getLogger(this.javaClass)
    protected var shellId: String
    var fc: FlashContainer? = null
        protected set
    protected var clientCmdHandler: ClientCmdHandler? = null
    protected var flashShellListeners: MutableList<FlashShellListener> = ArrayList<FlashShellListener>()

    constructor(shellId: String, display: Display) : super(display) {
        this.shellId = shellId
        this.init()
    }

    constructor(shellId: String, display: Display, style: Int) : super(display, style) {
        this.shellId = shellId
        this.init()
    }

    /**
     * 取得默认的布局类型

     * @return
     */
    protected open val defaultLayout: Layout?
        get() = FillLayout()

    protected open fun init() {
        val layout = this.defaultLayout
        if (layout != null) {
            this.layout = layout
        }
        // 如果存在图标,则设置窗口图标
        val imagepath = ManagerContainer.pathManager.getGameImage(PathManager.MAIN_APP)
        // if(new File(imagepath).exists()){

        // }
        try {
            this.setIcon(imagepath)
        } catch (e: Exception) {
            log.warn("设置窗口图标时发生错误!", e)
        }

    }

    /**
     * 装载指定位置的flash文件

     * @param path
     */
    open fun loadFlash(path: String) {
        // 如果已经存在fc,则销毁并创建新的fc
        if (fc != null) {
            fc!!.dispose()
            this.clientCmdHandler = null
        }
        this.fc = this.createFlashContainer()
        this.fc!!.addListener { onConnect() }
        this.fc!!.loadMovie(0, path)
    }

    /**
     * 创建Flash容器

     * @return
     */
    protected open fun createFlashContainer(): FlashContainer {
        return FlashContainer(this, this.shellId)
    }

    /**
     * 连接flash文件成功后触发的方法
     */
    protected open fun onConnect() {
        // 装载完成后,创建客户端指令处理器
        clientCmdHandler = ClientCmdHandler(fc!!)
        // clientCmdHandler.onConnection();
    }

    /**
     * 发送错误信息

     * @param msg
     */
    fun sendErrorMessage(msg: String) {
        this.clientCmdHandler?.onError(msg)
    }

    /**
     * 发送指令

     * @param msg
     */
    fun sendCommand(cmdstr: String) {
        this.clientCmdHandler?.onCommand(cmdstr)
    }

    /**
     * 弹出提示框

     * @param message
     */
    fun alert(message: String) {
        val messagebox = MessageBox(this, SWT.ICON_WARNING or SWT.OK)
        messagebox.text = this.text
        messagebox.message = message
        messagebox.open()
    }

    /**
     * 居中窗口
     */
    fun center() {
        val displayBounds = Display.getDefault().primaryMonitor.bounds
        val shellBounds = this.bounds
        val x = displayBounds.x + (displayBounds.width - shellBounds.width) shr 1
        val y = displayBounds.y + (displayBounds.height - shellBounds.height) shr 1
        this.setLocation(x, y)
    }

    /**
     * 设置窗口的图标
     */
    fun setIcon(path: String) {
        val image = Image(this.display, path)
        this.image = image
    }

    override fun dispose() {
        this.minimized = true
        super.dispose()
        this.fc!!.dispose()
        // 销毁时分派一个事件
        this.dispatchEvent(FlashShellEvent())
    }

    override fun checkSubclass() {

    }

    /**
     * 显示读取进度条

     * @param message
     * *
     * @param timeout
     */
    fun showTooltips(message: String, timeout: Double) {
        this.clientCmdHandler?.showTooltips(message, timeout)
    }

    /**
     * 隐藏读取进度条
     */
    fun hideTooltips() {
        this.clientCmdHandler?.hideTooltips()
    }

    /**
     * 添加FlashShell的监听器

     * @param l
     */
    fun addFlashShellListener(l: FlashShellListener) {
        this.flashShellListeners.add(l)
    }

    /**
     * 处理事件

     * @param e
     */
    fun dispatchEvent(e: F14bgEvent) {
        if (e is FlashShellEvent) {
            for (o in this.flashShellListeners) {
                o.onShellDisposed(e)
            }
        }
    }

}
