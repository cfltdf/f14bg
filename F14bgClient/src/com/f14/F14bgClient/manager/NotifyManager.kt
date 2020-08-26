package com.f14.F14bgClient.manager

import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.Rectangle

import javax.swing.Icon

import com.f14.F14bgClient.component.bubble.Bubble
import com.f14.F14bgClient.component.bubble.InviteGameBubble
import com.f14.F14bgClient.consts.NotifyType
import com.f14.bg.action.BgAction

/**
 * 通知消息的管理器

 * @author F14eagle
 */
class NotifyManager {

    protected var notifyHelper: NotifyHelper

    init {
        this.notifyHelper = NotifyHelper()
    }

    /**
     * 显示气泡通知

     * @param act
     */
    fun showNotify(act: BgAction) {
        if (!notice)
            return
        val nt = act.getAsString("notifyType") ?: throw Exception("No Notify Type")
        val notifyType = NotifyType.valueOf(nt)
        val message = act.getAsString("message") ?: ""
        when (notifyType) {
            NotifyType.CREATE_ROOM -> {
                val roomId = act.getAsInt("roomId")
                this.showCreateRoomNotiry(message, roomId)
            }
            else -> this.showNotiry(message)
        }
    }

    /**
     * 显示气泡通知

     * @param message
     */
    fun showNotiry(message: String) {
        notifyHelper.setToolTip(message)
    }

    /**
     * 显示气泡通知

     * @param message
     */
    fun showCreateRoomNotiry(message: String, roomId: Int) {
        val b = InviteGameBubble()
        b.setMessage(message)
        b.roomId = roomId
        notifyHelper.setToolTip(b)
    }

    /**


     * @author F14eagle
     */
    /**
     * 构造函数，初始化默认气泡提示设置

     */
    class NotifyHelper {

        /**
         * 此类处则动画处理

         */
        internal inner class Animation(var _single: Bubble) : Thread() {

            /**
             * 调用动画效果，移动窗体坐标

             * @param posx
             * *
             * @param startY
             * *
             * @param endY
             * *
             * @throws InterruptedException
             */
            @Throws(InterruptedException::class)
            private fun animateVertically(posx: Int, startY: Int, endY: Int) {
                _single.setLocation(posx, startY)
                if (endY < startY) {
                    var i = startY
                    while (i > endY) {
                        _single.setLocation(posx, i)
                        Thread.sleep(STEP_TIME.toLong())
                        i -= STEP
                    }
                } else {
                    var i = startY
                    while (i < endY) {
                        _single.setLocation(posx, i)
                        Thread.sleep(STEP_TIME.toLong())
                        i += STEP
                    }
                }
                _single.setLocation(posx, endY)
            }

            /**
             * 开始动画处理
             */
            override fun run() {
                try {
                    var animate = true
                    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    val screenRect = ge.maximumWindowBounds
                    val screenHeight = screenRect.height.toInt()
                    val startYPosition: Int
                    var stopYPosition: Int
                    if (screenRect.y > 0) {
                        animate = false
                    }
                    MAX_TOOLTIP_SCEEN = screenHeight / BUBBLE_HEIGHT
                    val posx = screenRect.width.toInt() - BUBBLE_WIDTH - 1
                    _single.setLocation(posx, screenHeight)
                    _single.isVisible = true
                    if (USE_TOP) {
                        _single.isAlwaysOnTop = true
                    }
                    if (animate) {
                        startYPosition = screenHeight
                        stopYPosition = startYPosition - BUBBLE_HEIGHT - 1
                        if (COUNT_OF_TOOLTIP > 0) {
                            stopYPosition = stopYPosition - MAX_TOOLTIP % MAX_TOOLTIP_SCEEN * BUBBLE_HEIGHT
                        } else {
                            MAX_TOOLTIP = 0
                        }
                    } else {
                        startYPosition = screenRect.y - BUBBLE_HEIGHT
                        stopYPosition = screenRect.y

                        if (COUNT_OF_TOOLTIP > 0) {
                            stopYPosition = stopYPosition + MAX_TOOLTIP % MAX_TOOLTIP_SCEEN * BUBBLE_HEIGHT
                        } else {
                            MAX_TOOLTIP = 0
                        }
                    }

                    COUNT_OF_TOOLTIP++
                    MAX_TOOLTIP++

                    animateVertically(posx, startYPosition, stopYPosition)
                    // 当鼠标不在该气泡上超过DISPLAY_TIME,则气泡消失
                    while (_single.timeout < DISPLAY_TIME) {
                        Thread.sleep(1000)
                        _single.refreshTimeout(1000)
                    }
                    animateVertically(posx, stopYPosition, startYPosition)

                    COUNT_OF_TOOLTIP--
                    _single.isVisible = false
                    _single.dispose()
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }

            }
        }

        /**
         * 设定显示的图片及信息

         * @param icon
         * *
         * @param msg
         */
        fun setToolTip(icon: Icon?, msg: String) {
            val single = Bubble()
            if (icon != null) {
                single.setIcon(icon)
            }
            single.setMessage(msg)
            Animation(single).start()
        }

        /**
         * 设定显示的信息

         * @param msg
         */
        fun setToolTip(msg: String) {
            setToolTip(null, msg)
        }

        /**
         * 显示指定的气泡

         * @param bubble
         */
        fun setToolTip(bubble: Bubble) {
            Animation(bubble).start()
        }

    }

    companion object {
        // 气泡提示宽
        var BUBBLE_WIDTH = 300
        // 气泡提示高
        var BUBBLE_HEIGHT = 100
        // 设定循环的步长
        var STEP = 30

        // 每步时间
        var STEP_TIME = 30

        // 显示时间
        var DISPLAY_TIME = 6000

        // 目前申请的气泡提示数量
        var COUNT_OF_TOOLTIP = 0

        // 当前最大气泡数
        var MAX_TOOLTIP = 0

        // 在屏幕上显示的最大气泡提示数量
        var MAX_TOOLTIP_SCEEN: Int = 0

        // 字体
        var MESSAGE_FONT: Font

        // 边框颜色
        var BG_COLOR: Color

        // 背景颜色
        var BORDER: Color

        // 消息颜色
        var MESSAGE_COLOR: Color

        // 差值设定
        var GAP: Int = 0

        // 是否要求至顶（jre1.5以上版本方可执行）
        var USE_TOP = true

        var notice = true

        init {
            // 设定字体
            MESSAGE_FONT = Font("宋体", 0, 12)
            // 设定边框颜色
            BG_COLOR = Color(255, 255, 225)
            BORDER = Color.BLACK
            MESSAGE_COLOR = Color.BLACK
            USE_TOP = true
            // 通过调用方法，强制获知是否支持自动窗体置顶
            // try {
            // JWindow.class.getMethod("setAlwaysOnTop",
            // new Class[] { Boolean.class });
            // } catch (Exception e) {
            // USE_TOP = false;
            // }
        }

        @JvmStatic fun main(args: Array<String>) {
            val manager = NotifyManager()
            for (i in 0..4) {
                manager.showCreateRoomNotiry("快加我" + i, i)
            }
        }
    }
}
