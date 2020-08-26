package com.f14.F14bgClient.FlashHandler

import java.io.BufferedReader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.FileDialog

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.F14bgClient.User
import com.f14.F14bgClient.component.HallShell
import com.f14.F14bgClient.component.RoomShell
import com.f14.F14bgClient.manager.ManagerContainer
import com.f14.F14bgClient.manager.NotifyManager
import com.f14.F14bgClient.update.DefaultUpdaterListener
import com.f14.bg.action.BgResponse
import com.f14.utils.CommonUtil

import cn.smartinvoke.IServerObject
import com.f14.utils.ZipUtils
import net.sf.json.JSONObject
import org.eclipse.swt.widgets.Display
import java.io.File
import java.io.FileReader

/**
 * 处理大厅界面指令的接收器

 * @author F14eagle
 */
class HallHandler : IServerObject {

    /**
     * 退出大厅
     */
    fun exit() {
        // 切断连接就能推出大厅
        ManagerContainer.connectionManager.close()
    }

    /**
     * 设置本地用户信息

     * @param userStr
     */
    fun setLocalUser(userStr: String) {
        val user = JSONObject.toBean(JSONObject.fromObject(userStr), User::class.java) as User
        val msg = "F14桌游大厅 - {0}"
        ManagerContainer.shellManager.hallShell!!.text = CommonUtil.getMsg(msg, user.name)
        ManagerContainer.connectionManager.localUser = user
    }

    /**
     * 创建房间

     * @param gameType
     * *
     * @param name
     * *
     * @param password
     * *
     * @param descr
     */
    fun createRoom(gameType: String, name: String, password: String, descr: String) {
        // 首先检查更新
        ManagerContainer.updateManager.executeUpdate(gameType, object : DefaultUpdaterListener() {
            override fun onUpdateSuccess(updated: Boolean) {
                // 向服务器发送创建房间的指令
                val res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_CREATE_ROOM, -1)
                res.setPublicParameter("gameType", gameType)
                res.setPublicParameter("name", name)
                res.setPublicParameter("password", password)
                res.setPublicParameter("descr", descr)
                ManagerContainer.connectionManager.sendResponse(res)
            }
        })
    }

    fun loadReplay() {
        val shell = ManagerContainer.shellManager.hallShell
        val dlg = FileDialog(shell, SWT.OPEN)// 设置为保存对话框
        dlg.text = "载入录像"
        dlg.filterNames = arrayOf("F14录像(*.f14)")
        dlg.filterExtensions = arrayOf("*.f14")
        val fileName = dlg.open()
        try {
            val file = File(fileName)
            if (file.exists()){
                val byteArray = file.readBytes()
                val content = String(ZipUtils.decompressBytes(byteArray))
//                val reader = FileReader(file)
//                val content = reader.readText()
                val jobj = JSONObject.fromObject(content)
                val gameType = jobj.getString("gameType")
                ManagerContainer.updateManager.executeUpdate(gameType, object : DefaultUpdaterListener() {
                    override fun onUpdateSuccess(updated: Boolean) {
                        // 更新成功后,打开房间窗口
                        ManagerContainer.resourceManager.replayResourceString = jobj.get("resourceString").toString()
                        ManagerContainer.resourceManager.replayResponse = jobj.getJSONArray("responseString")
                        Display.getDefault().asyncExec { ManagerContainer.shellManager.createRoomShell(-1, gameType) }
                    }
                })
            }
//            Files.newBufferedReader(Paths.get(fileName)).use { reader ->
//                val type = reader.readLine()
//                val replayString = reader.lines().collect<String, *>(Collectors.joining())
//                ManagerContainer.shellManager.createRoomShell(-1, type)
//                val room = ManagerContainer.shellManager.getRoomShell(-1)
//                room.setReplay(replayString)
//            }
        } catch (e: IOException) {
            ManagerContainer.shellManager.alert(e.message?: "")
        }

    }

    fun acceptNotice() {
        NotifyManager.notice = true
    }

    fun refuseNotice() {
        NotifyManager.notice = false
    }

    override fun dispose() {

    }

}
