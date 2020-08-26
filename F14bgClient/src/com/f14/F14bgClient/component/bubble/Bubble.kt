package com.f14.F14bgClient.component.bubble

import java.awt.BorderLayout
import java.awt.Insets
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JWindow
import javax.swing.border.EtchedBorder

import com.f14.F14bgClient.manager.NotifyManager

/**
 * 重构JWindow用于显示单一气泡提示框

 */
open class Bubble : JWindow() {

    protected var _iconLabel = JLabel()
    protected var _message = JTextArea()

    var timeout: Long = 0
        private set
    private var onFocus = false

    init {
        initComponents()
    }

    protected open fun initComponents() {
        setSize(NotifyManager.BUBBLE_WIDTH, NotifyManager.BUBBLE_HEIGHT)
        _message.font = NotifyManager.MESSAGE_FONT
        val externalPanel = JPanel(BorderLayout(1, 1))
        externalPanel.background = NotifyManager.BG_COLOR
        // 通过设定水平与垂直差值获得内部面板
        val innerPanel = JPanel(BorderLayout(NotifyManager.GAP, NotifyManager.GAP))
        innerPanel.background = NotifyManager.BG_COLOR
        _message.background = NotifyManager.BG_COLOR
        _message.margin = Insets(4, 4, 4, 4)
        _message.lineWrap = true
        _message.wrapStyleWord = true
        // 创建具有指定高亮和阴影颜色的阴刻浮雕化边框
        val etchedBorder = BorderFactory.createEtchedBorder() as EtchedBorder
        // 设定外部面板内容边框为风化效果
        externalPanel.border = etchedBorder
        // 加载内部面板
        externalPanel.add(innerPanel)
        _message.foreground = NotifyManager.MESSAGE_COLOR
        // _message.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        innerPanel.add(_iconLabel, BorderLayout.WEST)
        innerPanel.add(_message, BorderLayout.CENTER)
        contentPane.add(externalPanel)

        _message.addMouseListener(object : MouseListener {

            override fun mouseClicked(e: MouseEvent) {}

            override fun mouseEntered(e: MouseEvent) {
                this@Bubble.onFocus = true
            }

            override fun mouseExited(e: MouseEvent) {
                this@Bubble.onFocus = false
            }

            override fun mousePressed(e: MouseEvent) {}

            override fun mouseReleased(e: MouseEvent) {}

        })
    }

    fun setIcon(icon: Icon) {
        this._iconLabel.icon = icon
    }

    open fun setMessage(message: String) {
        this._message.text = message
    }

    fun setTimeout(timeout: Int) {
        this.timeout = timeout.toLong()
    }

    fun refreshTimeout(timeout: Long) {
        if (this.onFocus) {
            this.timeout = 0
        } else {
            this.timeout += timeout
        }
    }

    companion object {
        private val serialVersionUID = 1L
    }

}