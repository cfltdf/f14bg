package com.f14.F14bgClient

import java.io.BufferedWriter
import java.io.IOException
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Paths

import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.FileDialog

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bgClient.component.HallShell
import com.f14.F14bgClient.manager.ManagerContainer
import com.f14.F14bgClient.update.DefaultUpdaterListener
import com.f14.bg.action.BgAction
import com.f14.net.socket.client.SocketHandler
import com.f14.net.socket.cmd.ByteCommand
import com.f14.net.socket.cmd.CommandSender

class PlayerHandler @Throws(IOException::class, InterruptedException::class)
constructor(socket: Socket) : SocketHandler(socket) {
    protected var sender: CommandSender

    init {
        this.sender = CommandSender(socket)
    }

    @Throws(IOException::class)
    override fun onSocketClose() {
        // 连接中断时,需要跳转到登录界面
        // 需要使用UI线程进行界面操作
        Display.getDefault().asyncExec { ManagerContainer.shellManager.showLoginShell() }
    }

    @Throws(IOException::class)
    override fun onSocketConnect() {

    }

    @Throws(IOException::class)
    override fun processCommand(cmd: ByteCommand) {
        log.info(cmd.toString())
        when (cmd.flag.toInt()) {
            CmdConst.EXCEPTION_CMD // 错误信息提示
            -> F14bgClient.instance.sendErrorMessage(cmd.roomId, cmd.content)
            CmdConst.CLIENT_CMD // 客户端指令
            -> this.processClientCommand(cmd)
            else // 处理其他指令
            -> F14bgClient.instance.sendCommand(cmd.roomId, cmd.content)
        }
    }

    @Throws(IOException::class)
    override fun sendCommand(cmd: ByteCommand) {
        this.sender.sendCommand(cmd)
    }

    /**
     * 处理客户端类型的行动

     * @param act
     */
    protected fun processClientCommand(cmd: ByteCommand) {
        val act = BgAction(cmd.content)
        when (act.code) {
            CmdConst.CLIENT_LOAD_CODE // 读取系统代码
            -> ManagerContainer.codeManager.loadCodeParam(act)
            CmdConst.CLIENT_OPEN_ROOM // 打开游戏窗口
            -> {
                val roomId = act.getAsInt("id")
                val gameType = act.getAsString("gameType") ?: throw Exception("No Game Type")
                // 检查版本后再打开房间窗口
                ManagerContainer.updateManager.executeUpdate(gameType, object : DefaultUpdaterListener() {
                    override fun onUpdateSuccess(updated: Boolean) {
                        // 更新成功后,打开房间窗口
                        Display.getDefault().asyncExec { ManagerContainer.shellManager.createRoomShell(roomId, gameType) }
                    }
                })
            }
            CmdConst.CLIENT_INIT_RESOURCE // 装载系统资源
            -> {
                val gameType = act.getAsString("gameType")?: throw Exception("No Game Type")
                val resString = act.jsonString
                ManagerContainer.resourceManager.setResourceString(gameType, resString)
                // 继续检查文件更新
                ManagerContainer.updateManager.loadResouceSuccess(true)
                // 解锁
                // ManagerContainer.resourceManager.notifyLock(gameType);
            }
            CmdConst.CLIENT_CLOSE_ROOM // 关闭房间窗口
            -> {
                val roomId = act.getAsInt("roomId")
                Display.getDefault().asyncExec { ManagerContainer.shellManager.disposeRoomShell(roomId) }
            }
            CmdConst.CLIENT_LEAVE_ROOM_CONFIRM // 询问是否关闭窗口
            -> {
                val roomId = act.getAsInt("roomId")
                Display.getDefault().asyncExec { ManagerContainer.shellManager.disposeConfirmRoomShell(roomId) }
            }
            CmdConst.CLIENT_CHECK_UPDATE // 文件更新
            -> {
                val gameType = act.getAsString("gameType")?: throw Exception("No Game Type")
                val versionString = act.getAsString("versionString")?: throw Exception("No Version String")
                val files = act.getAsString("files")?: throw Exception("No Files")
                ManagerContainer.updateManager.setUpdateFiles(gameType, versionString, files)
            }
            CmdConst.CLIENT_USER_INFO // 查看用户信息
            -> {
                Display.getDefault().asyncExec {
                    ManagerContainer.shellManager.showUserShell(act.jsonString)
                    // ManagerContainer.shellManager.userShell.loadUserParam(act.getJSONString());
                }
            }
            CmdConst.CLIENT_BROADCAST // 广播消息
            -> {
                val message = act.getAsString("message")?: throw Exception("No Game Type")
                Display.getDefault().asyncExec { ManagerContainer.shellManager.alert(message) }
            }
            CmdConst.CLIENT_HALL_NOTICE // 打开大厅公告面板
            -> {
                Display.getDefault().asyncExec { ManagerContainer.shellManager.hallShell!!.showHallNotice() }
            }
            CmdConst.CLIENT_BUBBLE_NOTIFY // 显示气泡通知
            -> {
                Display.getDefault().asyncExec { ManagerContainer.notifyManager.showNotify(act) }
            }
            CmdConst.CLIENT_SAVE_REPLAY // 保存录像
            -> {
                Display.getDefault().asyncExec {
                    val shell = ManagerContainer.shellManager.hallShell
                    val dlg = FileDialog(shell, SWT.SAVE)// 设置为保存对话框
                    dlg.text = "保存录像"
                    dlg.filterNames = arrayOf("录像文件(.rep)")
                    dlg.filterExtensions = arrayOf("*.rep")
                    val fileName = dlg.open()
                    try {
                        Files.newBufferedWriter(Paths.get(fileName)).use { writer ->
                            writer.write(act.jsonString)
                        }
                    } catch (e: IOException) {
                        ManagerContainer.shellManager.alert(e.message?: "")
                    }
                }
            }
            else -> {
            }
        }
    }

}
