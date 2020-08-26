package com.f14.bg.player


import java.util.*

/**
 * 玩家组

 * @author F14eagle
 */
open class PlayerGroup<P : Player> {

    protected var players: MutableCollection<P> = LinkedHashSet()
    var score = 0

    fun addPlayer(player: P) {
        this.players.add(player)
    }

    fun addScore(score: Int): Int {
        this.score += score
        return this.score
    }

    fun clear() {
        this.players.clear()
        this.score = 0
    }

    /**
     * 判断玩家是否都在组中

     * @param players

     * @return
     */
    fun containPlayers(vararg players: P): Boolean {
        return players.all(this.players::contains)
    }


}
