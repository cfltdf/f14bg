package com.f14.F14bg.network

import com.f14.F14bg.consts.CmdConst
import com.f14.bg.action.BgResponse
import com.f14.bg.chat.Message
import com.f14.bg.consts.NotifyType
import com.f14.net.socket.cmd.CommandFactory


/**
 * 指令工厂类

 * @author F14eagle
 */
object CmdFactory {
    /**
     * 创建客户端的指令
     * @param content
     * @return
     */
    fun createClientCommand(content: String) = CommandFactory.createCommand(CmdConst.CLIENT_CMD, 0, content)

    /**
     * 创建基本的指令
     * @param content
     * @return
     */
    fun createCommand(roomId: Int, content: String) = CommandFactory.createCommand(CmdConst.APPLICATION_FLAG, roomId, content)

    /**
     * 创建聊天指令
     * @param message
     * @return
     */
    fun createChatResponse(message: Message) = BgResponse(CmdConst.CHAT_CMD, CmdConst.CHAT_CODE_MESSAGE, 0)
            .public("message", message)

    /**
     * 创建客户端相关操作的指令,气泡通知
     * @param code
     * @return
     */
    fun createClientNotifyResponse(code: Int, notifyType: NotifyType) = BgResponse(CmdConst.CLIENT_CMD, code, -1)
            .public("notifyType", notifyType)

    /**
     * 创建客户端相关操作的指令
     * @param code
     * @return
     */
    fun createClientResponse(code: Int) = BgResponse(CmdConst.CLIENT_CMD, code, -1)

    /**
     * 创建请求输入的游戏指令
     * @param code
     * @param position
     * @return
     */
    fun createGameResponse(code: Int, position: Int) = BgResponse(CmdConst.GAME_CMD, code, position)

    /**
     * 创建请求输入的系统指令
     * @param code
     * @param position
     * @return
     */
    fun createSystemResponse(code: Int, position: Int) = BgResponse(CmdConst.SYSTEM_CMD, code, position)

}
