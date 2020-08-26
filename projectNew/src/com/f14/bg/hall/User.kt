package com.f14.bg.hall

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.F14bg.network.PlayerHandler
import com.f14.bg.action.BgResponse
import com.f14.bg.action.ISendable
import com.f14.bg.action.ISendableWith
import com.f14.bg.chat.IChatable
import com.f14.bg.chat.IChatableWith
import com.f14.bg.chat.Message
import com.f14.bg.component.Convertable
import com.f14.bg.player.Player
import com.f14.f14bgdb.model.UserModel
import com.f14.net.socket.cmd.ByteCommand
import kotlinx.coroutines.Deferred
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */

class User(var handler: PlayerHandler, userModel: UserModel) : Convertable, ISendable, ISendableWith<Int>, IChatable, IChatableWith<Int> {
    val id: Long = userModel.id!!
    val loginName: String = userModel.loginName!!
    val name: String = userModel.userName!!
    var isAdmin = false
    var disconnectJob: Deferred<Unit>? = null
    private val players: MutableMap<Int, Player> = LinkedHashMap()

    /**
     * 添加玩家对象
     * @param player
     */
    fun addPlayer(gameRoom: GameRoom, player: Player) = this.players.set(gameRoom.id, player)

    /**
     * 检查是否可以向该用户发送通知
     * @return
     */
    fun canSendCreateRoomNotify() = this.players.values.none { it.room.isPlayingGame(this) }

    /**
     * 切断玩家的连接
     */
    fun closeConnection() = this.handler.close()

    /**
     * 取得用户在指定房间里的玩家对象
     * @param <P>
     * @param gameRoom
     * @return
     */
    fun <P : Player> getPlayer(gameRoom: GameRoom): P? = this.getPlayer(gameRoom.id)

    /**
     * 取得用户在指定房间里的玩家对象
     * @param <P>
     * @param roomId
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    fun <P : Player> getPlayer(roomId: Int): P? = this.players[roomId] as P?

    /**
     * 取得用户所在的所有的房间id
     * @return
     */
    val roomIds: Collection<Int>
        get() = synchronized(this.players, this.players::keys)

    /**
     * 判断玩家是否已经进入房间
     * @return
     */
    fun hasRoom(): Boolean = this.players.filterValues { it.name == this.name }.isNotEmpty()

    /**
     * 移除玩家对象
     * @param <P>
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    fun <P : Player> removePlayer(gameRoom: GameRoom) = this.players.remove(gameRoom.id) as? P

    /**
     * 向玩家发送指令
     * @param cmd
     */
    private fun sendCommand(cmd: ByteCommand) = this.handler.sendCommand(cmd)

    /**
     * 向玩家发送消息
     * @param arg 发送消息的房间id
     * @param message
     */
    override fun sendMessage(arg: Int, message: Message) = CmdFactory.createChatResponse(message).send(this, arg)

    /**
     * 向玩家发送消息
     * @param message
     */
    override fun sendMessage(message: Message) = this.sendMessage(0, message)

    /**
     * 向玩家发送回应
     * @param arg 发送回应的房间id
     * @param res
     */
    override fun sendResponse(arg: Int, res: BgResponse) {
        val content = this.createContent(arg, res)
        this.sendCommand(when {
            res.type == CmdConst.CLIENT_CMD -> CmdFactory.createClientCommand(content)
            else -> CmdFactory.createCommand(arg, content)
        })
    }

    private fun createContent(arg: Int, res: BgResponse) = when {
        this.getPlayer<Player>(arg)?.isPrivatePosition(res.position) ?: false -> res.privateString
        else -> res.publicString
    }

    /**
     * 向玩家发送回应
     * @param res
     */
    override fun sendResponse(res: BgResponse) = this.sendResponse(0, res)

    private val roomString
        get() = this.players.values.map(Player::room).filterNot(GameRoom::isMatchMode).joinToString(" 与 ", transform = GameRoom::name)

    fun toMap(room: GameRoom): Map<String, Any> = mapOf(
            "loginName" to this.loginName,
            "name" to (this.getPlayer<Player>(room)?.name ?: this.name),
            "userId" to this.id,
            "isAdmin" to this.isAdmin,
            "room" to roomString
    )

    override fun toMap(): Map<String, Any> = mapOf(
            "loginName" to this.loginName,
            "name" to this.name,
            "userId" to this.id,
            "isAdmin" to this.isAdmin,
            "room" to roomString
    )


}
