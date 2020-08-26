package com.f14.F14bgClient.FlashHandler

import cn.smartinvoke.IServerObject
import com.f14.F14bgClient.manager.ManagerContainer
import com.f14.utils.ZipUtils
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import org.eclipse.swt.SWT
import org.eclipse.swt.widgets.FileDialog
import org.eclipse.swt.widgets.MessageBox
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.MutableMap
import kotlin.collections.dropLastWhile
import kotlin.collections.toTypedArray

/**
 * 处理大厅界面指令的接收器

 * @author F14eagle
 */
class RoomHandler : IServerObject {
    protected val replays: MutableMap<Int, ReplayObject> = HashMap()

    /**
     * 用户退出房间的请求
     */
    fun leaveRequest(roomId: Int) {
        ManagerContainer.actionManager.leaveRequest(roomId)
    }

    /**
     * 用户退出房间的请求
     */
    fun replaceRequest(roomId: Int) {
        ManagerContainer.actionManager.replaceRequest(roomId)
    }

    fun saveReport(report: String) {
        if (ManagerContainer.propertiesManager.getLocalProperty("save_report") == "yes") {
            if (report.isEmpty()) return
            val now = Date()
            val format = SimpleDateFormat("yyyyMMddHHmmss")
            val fileName = format.format(now) + ".txt"
            try {
                val dir = File("report")
                if (!dir.exists()) {
                    dir.mkdir()
                }
                val gameType = report
                        .split("\n".toRegex())
                        .get(0)
                        .split(" ".toRegex())
                        .get(1)
                val gameDir = File(dir, gameType)
                if (!gameDir.exists()) {
                    gameDir.mkdir()
                }
                val stream = FileOutputStream(File(gameDir, fileName))
                stream.write(report.toByteArray())
                stream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun initReplay(roomId: Int, gameType: String) {
        val resource = ManagerContainer.resourceManager.getResourceString(gameType)
        if (resource != null) {
            val replay = ReplayObject()
            replay.gameType = gameType
            replay.resourceString = resource
            this.replays.put(roomId, replay)
        }
    }

    fun addReplay(roomId:Int, response: String){
        val replay = this.replays.get(roomId)
        if (replay != null){
            replay.responseStrings.add(response)
        }
    }

    fun saveReplay(roomId: Int) {
        val replay = this.replays.get(roomId)
        if (replay != null) {
            val dlg = FileDialog(ManagerContainer.shellManager.roomShells[roomId], SWT.SAVE)
            dlg.text = "保存录像"
            dlg.filterNames = arrayOf("F14录像(*.f14)")
            dlg.filterExtensions = arrayOf("*.f14")
            val filename = dlg.open()
            val file = File(filename)
            val done: Boolean
            if (file.exists()) {
                // The file already exists; asks for confirmation
                val mb = MessageBox(dlg.getParent(), SWT.ICON_WARNING or SWT.YES or SWT.NO);
                mb.setText("Tips");
                mb.setMessage(filename + " 已经存在，是否要替换它?");
                // If they click Yes, drop out. If they click No,
                // redisplay the File Dialog
                done = mb.open() == SWT.YES;
            } else {
                // 不存在文件名重复，可以保存
                done = true;
            }
            if (done) {
                val jobj = JSONObject()
                jobj.put("gameType", replay.gameType)
                jobj.put("resourceString", replay.resourceString)
                jobj.put("responseString", JSONArray.fromObject(replay.responseStrings))
                val content = jobj.toString().toByteArray()
                val byteArray = ZipUtils.compressBytes(content)
                file.writeBytes(byteArray)
            }
        }
    }

    override fun dispose() {

    }

    class ReplayObject {
        lateinit var gameType: String
        lateinit var resourceString: String
        val responseStrings = ArrayList<String>()
    }

}
