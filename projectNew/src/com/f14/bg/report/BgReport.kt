package com.f14.bg.report

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.consts.GameType
import com.f14.F14bg.network.CmdFactory
import com.f14.bg.BoardGame
import com.f14.bg.VPResult
import com.f14.bg.action.ISendableWith
import com.f14.bg.player.Player
import com.f14.bg.utils.BgUtils
import com.google.gson.GsonBuilder
import net.sf.json.JSONArray
import net.sf.json.JSONObject
import org.apache.log4j.Logger
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

open class BgReport<P : Player>(protected val game: BoardGame<P, *, *>) : ISendableWith<P?> by game {
    protected val log = Logger.getLogger(this.javaClass)!!
    protected val messages: MutableList<MessageObject> = ArrayList()

    protected lateinit var startTime: Date
    protected lateinit var endTime: Date
    protected lateinit var gameType: GameType
    protected lateinit var playerList: String
    protected var playerNumber: Int = 0

    /**
     * 记录玩家信息
     * @param player
     * @param message
     */
    open fun action(player: P, message: String) {
        this.action(player, message, false)
    }

    /**
     * 记录玩家信息
     * @param player
     * @param message
     */
    fun action(player: P, message: String, isAlert: Boolean) {
        this.addMessage(this.currentTime, "${player.reportString} $message", isAlert)
    }

    /**
     * 记录玩家信息
     * @param player
     */
    open fun action(player: P, msgPublic: String, msgPrivate: String) {
        this.action(player, msgPublic, msgPrivate, false)
    }

    /**
     * 记录玩家信息
     * @param player
     */
    fun action(player: P, msgPublic: String, msgPrivate: String, isAlert: Boolean) {
        this.addMessage(this.currentTime, "${player.reportString} $msgPublic", isAlert, player.position, "${player.reportString} $msgPrivate")
    }

    /**
     * 添加信息
     * @param mo
     * @return
     */
    protected fun addMessage(mo: MessageObject) {
        log.info(mo)
        this.messages.add(mo)
        // 发送指令到客户端
        this.sendMessageResponse(mo, null)
    }

    /**
     * 添加信息
     * @param message
     */
    protected fun addMessage(message: String) {
        this.addMessage(message, false)
    }

    /**
     * 添加信息
     * @param message
     * @param isAlert
     */
    protected fun addMessage(message: String, isAlert: Boolean) {
        this.addMessage("", message, isAlert)
    }

    /**
     * 添加信息
     * @param time
     * @param message
     */
    protected fun addMessage(time: String, message: String) {
        this.addMessage(time, message, false)
    }

    /**
     * 添加信息
     * @param time
     * @param message
     * @param isAlert
     * @return
     */
    protected fun addMessage(time: String, message: String, isAlert: Boolean) {
        val mo = MessageObject(time, message, isAlert)
        this.addMessage(mo)
    }

    /**
     * 添加信息
     * @param time
     * @param msgPublic
     * @param isAlert
     * @param position
     * @param msgPrivate
     */
    protected fun addMessage(time: String, msgPublic: String, isAlert: Boolean, position: Int, msgPrivate: String) {
        val mo = MessageObject(time, msgPublic, isAlert, position, msgPrivate)
        this.addMessage(mo)
    }

    /**
     * 将游戏标题信息插入到消息列表最前面
     */
    protected fun addTitle() {
        val titles = mutableListOf(
                MessageObject("", "游戏类型: " + this.gameType, false),
                MessageObject("", "游戏人数: " + this.playerNumber, false),
                MessageObject("", "玩家分布: " + this.playerList, false),
                MessageObject("", "开始时间: " + TIME_FORMAT.format(this.startTime), false),
                MessageObject("", "结束时间: " + TIME_FORMAT.format(this.endTime), false)
        )
        if (this.game.room.isMatchMode) {
            titles += listOf(MessageObject("", "游戏在比赛模式中进行，游戏内禁言且玩家匿名!"))
//            + game.players.map {
//                MessageObject("", it.name + " 是 " + it.user.name)
//            }
        }
        this.messages.addAll(0, titles)
    }

    /**
     * 游戏结束
     */
    fun end() {
        // 设置战报中游戏的一些数据
        this.endTime = Date()
        this.gameType = game.room.type
        this.playerNumber = game.currentPlayerNumber
        this.playerList = game.players.joinToString(" | ") { "${(it.position + 1)}:${it.name}" }
        this.system("游戏结束")
        // 结束时记录标题信息
        this.addTitle()
    }

    /**
     * 游戏即将结束时的提醒
     */
    fun gameOverWarning() {
        this.system("游戏即将结束!")
    }

    /**
     * 取得当前日期 年-月-日
     * @return
     */
    protected val currentDate: String
        get() = DATE_FORMAT.format(Date())

    /**
     * 取得当前时间 时:分:秒
     * @return
     */
    protected val currentTime: String
        get() = TIME_FORMAT.format(Date())

    /**
     * 记录普通信息
     * @param message
     */
    fun info(message: String) {
        this.info(message, false)
    }

    /**
     * 记录普通信息
     * @param message
     */
    fun info(message: String, isAlert: Boolean) {
        this.addMessage(this.currentTime, "@ $message", isAlert)
    }

    /**
     * 画横线
     */
    fun line() {
        this.addMessage(LINE)
    }

    /**
     * 玩家回合结束
     * @param player
     */
    fun playerRoundEnd(player: P) {
        this.action(player, "回合结束!")
        this.line()
    }

    /**
     * 玩家回合开始
     * @param player
     */
    fun playerRoundStart(player: P) {
        this.line()
        this.action(player, "回合开始!")
    }

    /**
     * 输出所有信息
     */
    fun print() {
        val string = this.toJSONString()
        val array = JSONArray.fromObject(string)
        array.map { it as JSONObject }.map { JSONObject.toBean(it, MessageObject::class.java) as MessageObject }.forEach { println(it.toString()) }
    }

    /**
     * 记录游戏结果
     * @param result
     */
    fun result(result: VPResult) {
        this.system("玩家得分情况")
        for (vpc in result.vpCounters) {
            this.addMessage("玩家: " + vpc.player.name)
            this.addMessage("名次: " + vpc.rank)
            this.addMessage("顺位: " + (vpc.player.position + 1))
            this.addMessage("积分: " + vpc.score)
            this.addMessage("排名点: " + vpc.rankPoint)
            this.addMessage("得分: " + vpc.totalVP)
            this.addMessage("得分明细: ")
            for (obj in vpc.allVps) this.addMessage("    ${obj.label} : ${obj.vp}")
            this.addMessage(LINE)
        }
    }

    /**
     * 发送信息的指令
     * @param receiver
     * @param mos
     */
    protected fun sendMessageResponse(mos: List<MessageObject>, receiver: P) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_REPORT_MESSAGE, -1).public("messages", mos).send(this, receiver)
    }

    /**
     * 发送信息的指令
     * @param receiver
     */
    protected fun sendMessageResponse(mo: MessageObject, receiver: P?) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_REPORT_MESSAGE, mo.position).public("messages", BgUtils.toList(mo)).private("messages", BgUtils.toList(mo.toPrivate())).send(this, receiver)
    }

    /**
     * 发送最近的战报信息
     * @param receiver
     */
    @Suppress("UNCHECKED_CAST")
    fun sendRecentMessages(receiver: Player) {
        if (this.messages.isNotEmpty()) {
            val mos = this.messages.takeLast(RECENT_MESSAGE_NUMBER).map { if (receiver.position == it.position || receiver.position < 0 && receiver.user.isAdmin) it.toPrivate() else it }
            this.sendMessageResponse(mos, receiver as P)
        }
    }

    /**
     * 游戏开始
     */
    fun start() {
        this.startTime = Date()
        this.system("游戏开始")
    }

    /**
     * 记录系统信息
     * @param message
     */
    fun system(message: String) {
        this.system(message, false)
    }

    /**
     * 记录系统信息
     * @param message
     */
    fun system(message: String, isAlert: Boolean) {
        this.line()
        this.addMessage(this.currentTime, message, isAlert)
        this.line()
    }

    /**
     * 转换成JSON String
     * @return
     */
    fun toJSONString(): String {
        return GsonBuilder().create().toJson(this.messages.map(MessageObject::toPrivate))
    }

    companion object {
        /**
         * 分割线
         */
        protected const val LINE = "--------------------"
        /**
         * 发送最近战报信息的条数
         */
        protected const val RECENT_MESSAGE_NUMBER = 200
        protected val DATE_FORMAT: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        protected val TIME_FORMAT: DateFormat = SimpleDateFormat("HH:mm:ss")
    }
}
