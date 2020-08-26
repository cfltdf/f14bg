package com.f14.F14bgClient.manager

import java.util.LinkedHashMap

import org.eclipse.swt.widgets.Display

import com.f14.F14bgClient.component.FlashShell
import com.f14.F14bgClient.component.HallShell
import com.f14.F14bgClient.component.LoginShell
import com.f14.F14bgClient.component.RoomShell
import com.f14.F14bgClient.component.UpdateShell
import com.f14.F14bgClient.component.UserShell
import com.f14.F14bgClient.consts.UIState

class ShellManager {
    var loginShell: LoginShell? = null
    var hallShell: HallShell? = null
    var updateShell: UpdateShell? = null
    var userShell: UserShell? = null
    var roomShells: MutableMap<Int, RoomShell> = LinkedHashMap()
    /**
     * 取得当前界面状态

     * @return
     */
    var currentState: UIState? = null
        private set

    init {
        this.init()
    }

    protected fun init() {

    }

    /**
     * 按照房间id取得窗口,如果房间id为0,则返回当前状态的窗口

     * @param roomId
     * *
     * @return
     */
    fun getShell(roomId: Int): FlashShell? {
        if (roomId == 0) {
            // 如果房间id为0,则取当前窗口
            return this.currentShell
        } else {
            return this.getRoomShell(roomId)
        }
    }

    /**
     * 取得当前的窗口

     * @return
     */
    val currentShell: FlashShell?
        get() {
            when (this.currentState) {
                UIState.LOGIN -> return this.loginShell
                UIState.HALL -> return this.hallShell
                else -> return null
            }
        }

    /**
     * 按照房间ID取得窗口

     * @param roomId
     * *
     * @return
     */
    fun getRoomShell(roomId: Int): RoomShell? {
        return this.roomShells[roomId]
    }

    /**
     * 切换到登录界面
     */
    fun showLoginShell() {
        // 如果存在大厅窗口,则销毁
        if (this.hallShell != null) {
            if (!this.hallShell!!.isDisposed) {
                this.hallShell!!.dispose()
            }
            this.hallShell = null
        }
        // 销毁所有房间窗口
        this.disposeRoomShells()

        if (this.loginShell == null) {
            // 如果没有登录窗口,则创建
            val display = Display.getDefault()
            loginShell = LoginShell("login", display)
        }
        // 装载登录界面
        loginShell!!.center()
        loginShell!!.loadFlash("")
        loginShell!!.open()
        loginShell!!.layout()
        loginShell!!.visible = true
        // 激活窗口
        loginShell!!.forceActive()
        this.currentState = UIState.LOGIN
    }

    /**
     * 隐藏登录界面
     */
    fun hideLoginShell() {
        this.loginShell!!.visible = false
    }

    /**
     * 切换到大厅界面
     */
    fun showHallShell() {
        // 隐藏登录界面
        this.hideLoginShell()

        if (this.hallShell == null) {
            // 如果没有登录窗口,则创建
            val display = Display.getDefault()
            hallShell = HallShell("hall", display)
        }
        // 装载大厅界面
        hallShell!!.loadFlash("")
        hallShell!!.layout()
        hallShell!!.open()
        this.currentState = UIState.HALL
    }

    /**
     * 判断是否结束

     * @return
     */
    val isDisposed: Boolean
        get() {
            if (this.loginShell == null) {
                return true
            }
            return this.loginShell!!.isDisposed
        }

    /**
     * 创建房间窗口

     * @param roomId
     * *
     * @param gameType
     */
    fun createRoomShell(roomId: Int, gameType: String) {
        var shell = this.getRoomShell(roomId)
        if (shell == null || shell.isDisposed) {
            // 如果不存在该房间的窗口,则创建该房间窗口
            shell = RoomShell(roomId, Display.getDefault())
        }
        shell.loadModule(gameType)
        shell.open()
        shell.layout()
        this.roomShells.put(roomId, shell)
        // 创建房间时,清空大厅的读取进度条
        this.hallShell!!.hideTooltips()
    }

    /**
     * 销毁所有房间窗口
     */
    private fun disposeRoomShells() {
        for (o in this.roomShells.values) {
            if (o != null && !o.isDisposed) {
                o.dispose()
            }
        }
        this.roomShells.clear()
    }

    /**
     * 销毁房间窗口

     * @param roomId
     */
    fun disposeRoomShell(roomId: Int) {
        val shell = this.getRoomShell(roomId)
        if (shell != null && !shell.isDisposed) {
            shell.dispose()
        }
    }

    /**
     * 确认是否关闭房间窗口

     * @param roomId
     */
    fun disposeConfirmRoomShell(roomId: Int) {
        val shell = this.getRoomShell(roomId)
        if (shell != null && !shell.isDisposed) {
            shell.disposeConfirm()
        }
    }

    /**
     * 显示版本更新界面
     */
    fun showUpdateShell() {
        if (this.updateShell == null) {
            // 如果没有窗口,则创建
            val display = Display.getDefault()
            updateShell = UpdateShell("update", display)
        }
        // 显示界面
        updateShell!!.center()
        updateShell!!.loadFlash("")
        updateShell!!.layout()
        updateShell!!.open()
        this.updateShell!!.visible = true
    }

    /**
     * 隐藏版本更新界面
     */
    fun hideUpdateShell() {
        this.updateShell!!.visible = false
    }

    /**
     * 创建用户信息窗口

     * @param userId
     */
    fun createUserShell(userId: Long?) {
        if (this.userShell == null) {
            // 如果没有窗口,则创建
            val display = Display.getDefault()
            userShell = UserShell("user", display)
            // 该窗口只会初始化一次
            userShell!!.currentUserId = userId
            userShell!!.loadFlash("")
            userShell!!.layout()
            userShell!!.center()
            userShell!!.open()
            // 初始化时将读取currentUserId中的用户信息
        }
    }

    /**
     * 显示用户信息界面
     */
    fun showUserShell(paramString: String) {
        if (this.userShell != null) {
            // 显示界面
            // userShell.currentUserId = userId;
            userShell!!.visible = true
            userShell!!.loadUserParam(paramString)
        }
    }

    /**
     * 隐藏用户信息界面
     */
    fun hideUserShell() {
        this.userShell!!.visible = false
    }

    /**
     * 销毁房间窗口

     * @param roomId
     */
    fun alert(message: String) {
        if (hallShell != null && !hallShell!!.isDisposed) {
            hallShell!!.setActive()
            hallShell!!.alert(message)
        }
    }

    /**
     * 取得当前打开的游戏窗口

     * @return
     */
    val currentRoomShell: RoomShell?
        get() {
            for (shell in this.roomShells.values) {
                if (shell != null && !shell.isDisposed) {
                    return shell
                }
            }
            return null
        }

}
