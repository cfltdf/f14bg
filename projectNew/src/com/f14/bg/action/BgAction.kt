package com.f14.bg.action

import com.f14.bg.BGConst
import com.f14.bg.exception.BoardGameException
import com.f14.bg.hall.GameRoom
import com.f14.bg.hall.User
import com.f14.bg.player.Player
import com.f14.bg.utils.BgUtils
import net.sf.json.JSONObject

sealed class BgAction(content: String) {
    val parameters: JSONObject = JSONObject.fromObject(content)
    val type: Int by parameters
    val code: Int by parameters

//    init {
//        this.type = this.getAsInt("type")
//        this.code = this.getAsInt("code")
//    }

    /**
     * 取得boolean类型参数
     * @param key
     * @return
     */
    fun getAsBoolean(key: String) = BgUtils.withDefault(false) { this.parameters.getBoolean(key) }

    /**
     * 取得int类型参数
     * @param key
     * @return
     */
    fun getAsInt(key: String) = BgUtils.withDefault(BGConst.INT_NULL) { this.parameters.getInt(key) }

    /**
     * 取得long类型参数
     * @param key
     * @return
     */
    fun getAsLong(key: String) = BgUtils.withDefault(BGConst.INT_NULL.toLong()) { this.parameters.getLong(key) }

    /**
     * 取得double类型参数
     * @param key
     * @return
     */
    fun getAsDouble(key: String) = BgUtils.withDefault(Double.NEGATIVE_INFINITY) { this.parameters.getDouble(key) }

    /**
     * 取得字符串类型参数
     * @param key
     * @return
     */
    fun getAsString(key: String) = BgUtils.withDefault("") { this.parameters.getString(key) }

    class GameAction(val user: User, private val room: GameRoom, content: String) : BgAction(content) {
        /**
         * 取得执行行动的玩家
         * @return
         */
        fun <P : Player> getPlayer(): P = user.getPlayer(room) ?: throw BoardGameException("用户不在此房间内!")
    }

    class HallAction(val user: User, content: String) : BgAction(content)

    class ClientAction(content: String) : BgAction(content)
}
