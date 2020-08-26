package com.f14.net.socket.cmd

object CommandFactory {

    fun createCommand(flag: Int, roomId: Int, content: String?): ByteCommand {
        var content = content
        val cmd = ByteCommand()
        cmd.head = ByteCommand.BYTE_HEAD
        cmd.flag = flag.toShort()
        cmd.roomId = roomId
        cmd.tail = ByteCommand.BYTE_TAIL
        if (content == null || content.length == 0) {
            content = "-"
        }
        cmd.content = content
        return cmd
    }
}
