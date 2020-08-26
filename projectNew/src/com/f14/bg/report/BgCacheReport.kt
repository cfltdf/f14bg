package com.f14.bg.report

import com.f14.bg.BoardGame
import com.f14.bg.player.Player
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

open class BgCacheReport<P : Player>(bg: BoardGame<P, *, *>) : BgReport<P>(bg) {
    /**
     * 所有玩家的战报缓存记录
     */
    protected val messageCache: MutableMap<P, MutableList<MessageObject>> = HashMap()

    /**
     * 玩家执行动作,并输出该玩家所有缓存的信息
     */
    override fun action(player: P, message: String) {
        val cache = this.getCacheMessages(player)
        val prefix = if (message.isNotEmpty()) listOf(message) else emptyList()
        val public = (prefix + cache.map(MessageObject::msgPublic)).joinToString(",")
        if (cache.none { it.msgPrivate != null }) {
            this.clearPlayerCache(player)
            super.action(player, public)
        } else {
            val private = (prefix + cache.map { it.msgPrivate ?: it.msgPublic }).joinToString(",")
            this.clearPlayerCache(player)
            super.action(player, public, private)
        }
    }

    /**
     * 玩家执行动作,并输出该玩家所有缓存的信息
     */
    override fun action(player: P, msgPublic: String, msgPrivate: String) {
        if (msgPrivate.isEmpty()) {
            this.action(player, msgPublic)
        } else {
            val prefixPublic = if (msgPublic.isNotEmpty()) listOf(msgPublic) else emptyList()
            val prefixPrivate = if (msgPrivate.isNotEmpty()) listOf(msgPrivate) else emptyList()
            val cache = this.getCacheMessages(player)
            val public = (prefixPublic + cache.map(MessageObject::msgPublic)).joinToString(",")
            val private = (prefixPrivate + cache.map { it.msgPrivate ?: it.msgPublic }).joinToString(",")
            this.clearPlayerCache(player)
            super.action(player, public, private)
        }
    }

    /**
     * 添加玩家行动到缓存中
     * @param player
     * @param message
     */
    fun addAction(player: P, message: String) {
        val mo = MessageObject("", message, false)
        this.getCacheMessages(player).add(mo)
    }

    /**
     * 添加玩家行动到缓存中
     * @param player
     */
    fun addAction(player: P, msgPublic: String, msgPrivate: String) {
        val mo = MessageObject("", msgPublic, false, player.position, msgPrivate)
        this.getCacheMessages(player).add(mo)
    }

    /**
     * 清空玩家的缓存信息
     * @param player
     */
    protected fun clearPlayerCache(player: P) {
        this.getCacheMessages(player).clear()
    }

    /**
     * 取得玩家的所有缓存信息
     * @param player
     * @return
     */
    protected fun getCacheMessages(player: P): MutableList<MessageObject> {
        return this.messageCache.computeIfAbsent(player) { ArrayList() }
    }

    /**
     * 添加玩家行动到缓存中
     * @param player
     * @param message
     */
    fun insertAction(player: P, message: String) {
        val mo = MessageObject("", message, false)
        this.getCacheMessages(player).add(0, mo)
    }

    /**
     * 添加玩家行动到缓存中
     * @param player
     */
    fun insertAction(player: P, msgPublic: String, msgPrivate: String) {
        val mo = MessageObject("", msgPublic, false, player.position, msgPrivate)
        this.getCacheMessages(player).add(0, mo)
    }

    /**
     * 输出玩家当前缓存内容(如果没有缓存内容则不输出)
     * @param player
     */
    fun printCache(player: P) {
        if (this.getCacheMessages(player).isNotEmpty()) {
            this.action(player, "")
        }
    }

}
