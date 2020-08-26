package com.f14.loveletter

import com.f14.bg.hall.GameRoom
import com.f14.bg.hall.User
import com.f14.bg.player.Player
import java.util.*

class LLPlayer(user: User, room: GameRoom) : Player(user, room), Comparable<LLPlayer> {

    var score: Int = 0
    var passed: Boolean = false
    var protect: Boolean = false

    var firstCard: LLCard? = null

    var secondCard: LLCard? = null
    var discardCards: MutableList<LLCard> = ArrayList()

    fun clear() {
        this.passed = false
        this.protect = false
        this.firstCard = null
        this.secondCard = null
        this.discardCards.clear()
    }

    override fun compareTo(other: LLPlayer): Int {
        return when {
            this === other -> 0
            this.passed && other.passed -> 0
            this.passed && !other.passed -> -1
            !this.passed && other.passed -> 1
            this.firstCard!!.point > other.firstCard!!.point -> 1
            this.firstCard!!.point < other.firstCard!!.point -> -1
            else -> this.playedPoints.compareTo(other.playedPoints)
        }
    }

    private val playedPoints
        get() = this.discardCards.sumByDouble(LLCard::point)

    override fun reset() {
        super.reset()
        this.score = 0
        this.clear()
    }

    override fun toMap(): Map<String, Any> = super.toMap() + mapOf("score" to score, "passed" to passed, "protect" to protect)

}
