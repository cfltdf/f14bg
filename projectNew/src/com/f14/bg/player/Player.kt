package com.f14.bg.player

import com.f14.F14bg.consts.CmdConst
import com.f14.bg.BGConst
import com.f14.bg.action.ISendableWith
import com.f14.bg.chat.IChatableWith
import com.f14.bg.common.ParamCache
import com.f14.bg.component.Convertable
import com.f14.bg.hall.GameRoom
import com.f14.bg.hall.User
import com.f14.bg.report.Printable

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */
abstract class Player(var user: User, val room: GameRoom) : Convertable, Printable, ISendableWith<Int> by user, IChatableWith<Int> by user {
    var position = BGConst.INT_NULL
    var team: Int = 0
    val params = ParamCache()
    var name = user.name

//    val name: String
//        get() = this.user.name

    override val reportString
        get() = "玩家${this.position + 1}[${this.name}]"


    /**
     * 重置玩家的游戏信息
     */
    open fun reset() {
        this.params.clear()
    }

    /**
     * 向玩家发送异常信息
     * @param roomId
     * @param e
     */
    fun sendException(roomId: Int, e: Exception) {
        user.handler.sendCommand(CmdConst.EXCEPTION_CMD, roomId, e.message)
    }

//    /**
//     * 发送消息
//     * @param arg
//     * @param message
//     */
//    override fun sendMessage(arg: Int, message: Message) {
//        user.sendMessage(arg, message)
//    }
//
//    /**
//     * 发送回应
//     * @param arg
//     * @param res
//     */
//    override fun sendResponse(arg: Int, res: BgResponse) {
//        user.sendResponse(arg, res)
//    }

    override fun toMap(): Map<String, Any> = mapOf("userId" to this.user.id, "name" to this.name, "position" to this.position, "team" to this.team)

    fun isPrivatePosition(position: Int) = this.position == position || this.position < 0 && this.user.isAdmin
}
