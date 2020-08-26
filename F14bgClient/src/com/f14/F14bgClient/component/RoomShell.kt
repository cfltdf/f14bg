package com.f14.F14bgClient.component

import java.util.HashMap
import org.eclipse.swt.SWT
import org.eclipse.swt.events.ShellEvent
import org.eclipse.swt.events.ShellListener
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.MessageBox

import com.f14.F14bgClient.manager.ManagerContainer
import com.f14.bg.action.BgResponse
import com.f14.utils.CommonUtil

import net.sf.json.JSONArray

class RoomShell(roomId: Int, display: Display) : FlashShell("ROOM" + roomId, display) {
    lateinit protected var gameType: String
    var roomId: Int = 0
        protected set
    protected var replay: List<BgResponse>? = null

    init {
        this.roomId = roomId
    }

    override fun init() {
        super.init()
        this.text = "F14桌游"
        this.maximized = true

        this.addShellListener(RoomShellListener())
    }

    /**
     * 装载指定的游戏模块

     * @param gameType
     */
    fun loadModule(gameType: String) {
        val username = ManagerContainer.connectionManager.localUser!!.name
        val msg = "F14桌游 - {0} - {1}"
        this.gameType = gameType
        this.text = CommonUtil.getMsg(msg, ManagerContainer.codeManager.getCodeLabel("BOARDGAME", this.gameType),
                username)
        this.loadUI()
    }

    /**
     * 装载界面
     */
    protected fun loadUI() {
        super.loadFlash(ManagerContainer.pathManager.gameLoaderPath)
    }

    override fun onConnect() {
        super.onConnect()
        val param = HashMap<String, Any>()
        param.put("roomId", this.roomId)
        param.put("gameType", this.gameType)
        param.put("basePath", ManagerContainer.pathManager.getBasePath(this.gameType))
        this.clientCmdHandler!!.setRoomInfo(param)
        this.clientCmdHandler!!.loadModule(ManagerContainer.pathManager.getGameModulePath(gameType))
    }

    /**
     * 关闭窗口时的提示
     */
    fun disposeConfirm() {
        // 弹出提示框提示玩家是否要关闭程序
        val messagebox = MessageBox(this, SWT.ICON_QUESTION or SWT.YES or SWT.NO)
        messagebox.text = this.text
        messagebox.message = "您还在游戏中,确定要强制退出游戏吗?"
        val message = messagebox.open()
        val res = message == SWT.YES
        if (res) {
            // 如果选是,则强制退出游戏
            ManagerContainer.actionManager.leave(roomId)
        }
    }

    /**
     * 房间窗口的事件监听器

     * @author F14eagle
     */
    protected inner class RoomShellListener : ShellListener {

        override fun shellActivated(arg0: ShellEvent) {}

        override fun shellClosed(e: ShellEvent) {
            // 关闭房间窗口时会向服务器发送关闭的指令,通过该方法才会关闭房间窗口
            if (roomId < 0){
                return
            }
            ManagerContainer.actionManager.leaveRequest(roomId)
            e.doit = false
        }

        override fun shellDeactivated(arg0: ShellEvent) {}

        override fun shellDeiconified(arg0: ShellEvent) {}

        override fun shellIconified(arg0: ShellEvent) {}

    }

    fun setReplay(replayString: String) {
        val array = JSONArray.fromObject(replayString)
    }

}
