package com.f14.net.smartinvoke

import java.io.IOException

import com.f14.F14bgClient.F14bgClient
import com.f14.F14bgClient.manager.ManagerContainer

import cn.smartinvoke.IServerObject

class FlashCommandHandler : IServerObject {

    /**
     * 向服务器发送指令

     * @param roomId
     * *
     * @param cmdstr
     */
    fun sendCommand(roomId: Int, cmdstr: String) {
        try {
            ManagerContainer.connectionManager.sendCommand(roomId, cmdstr)
        } catch (e: IOException) {
            F14bgClient.instance.sendErrorMessage(roomId, "指令转发失败! " + e.message)
        }

    }

    override fun dispose() {

    }

}
