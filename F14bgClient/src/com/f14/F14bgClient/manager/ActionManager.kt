package com.f14.F14bgClient.manager

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.bg.action.BgResponse

/**
 * 行动管理器

 * @author F14eagle
 */
class ActionManager {

    /**
     * 用户退出房间的请求
     */
    fun leaveRequest(roomId: Int) {
        val res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_LEAVE_REQUEST, -1)
        res.setPublicParameter("roomId", roomId)
        ManagerContainer.connectionManager.sendResponse(roomId, res)
    }

    fun replaceRequest(roomId: Int) {
        val res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_REPLACE_REQUEST, -1)
        res.setPublicParameter("roomId", roomId)
        ManagerContainer.connectionManager.sendResponse(roomId, res)
    }

    /**
     * 用户退出房间
     */
    fun leave(roomId: Int) {
        val res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_LEAVE, -1)
        res.setPublicParameter("roomId", roomId)
        ManagerContainer.connectionManager.sendResponse(roomId, res)
    }

    /**
     * 查看用户信息

     * @param userId
     */
    fun viewUser(userId: Long) {
        val res = CmdFactory.createClientResponse(CmdConst.CLIENT_USER_INFO)
        res.setPublicParameter("userId", userId)
        ManagerContainer.connectionManager.sendResponse(res)
    }

    /**
     * 加入房间

     * @param roomId
     */
    fun joinRoomCheck(roomId: Int) {
        val res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_JOIN_CHECK, 0)
        res.setPublicParameter("id", roomId)
        ManagerContainer.connectionManager.sendResponse(res)
    }

}
