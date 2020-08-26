package com.f14.F14bgClient.component.bubble

import com.f14.F14bgClient.event.FlashShellEvent
import com.f14.F14bgClient.event.FlashShellListener
import com.f14.F14bgClient.manager.ManagerContainer
import java.awt.Cursor
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

class InviteGameBubble : Bubble() {

    var roomId: Int = 0

    override fun initComponents() {
        super.initComponents()
        this._message.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        this._message.addMouseListener(InviteGameMouseListener())
    }

    override fun setMessage(message: String) {
        super.setMessage(message + "\n\n点我直接进入房间...")
    }

    /**
     * 加入房间
     */
    protected fun joinRoom() {
        this.isVisible = false
        this.setTimeout(999999)

        // 检查玩家是否已经加入了其他房间
        val shell = ManagerContainer.shellManager.currentRoomShell
        if (shell == null) {
            // 没有加入房间则直接加入指定的房间
            ManagerContainer.actionManager.joinRoomCheck(this.roomId)
        } else {
            // 否则需要先退出原先的房间后再加入
            shell.addFlashShellListener(object : FlashShellListener{
                override fun onShellDisposed(e: FlashShellEvent) {
                    ManagerContainer.actionManager.joinRoomCheck(this@InviteGameBubble.roomId)
                }
            })
            ManagerContainer.actionManager.leaveRequest(shell.roomId)
        }
    }

    internal inner class InviteGameMouseListener : MouseListener {

        override fun mouseClicked(e: MouseEvent) {
            // 点击就加入房间
            this@InviteGameBubble.joinRoom()
        }

        override fun mouseEntered(e: MouseEvent) {

        }

        override fun mouseExited(e: MouseEvent) {

        }

        override fun mousePressed(e: MouseEvent) {

        }

        override fun mouseReleased(e: MouseEvent) {

        }

    }

    companion object {
        private val serialVersionUID = 1L
    }

}
