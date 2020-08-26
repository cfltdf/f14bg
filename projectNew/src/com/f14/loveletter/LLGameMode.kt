package com.f14.loveletter

import com.f14.bg.GameMode
import com.f14.bg.exception.BoardGameException
import com.f14.bg.report.BgReport
import com.f14.bg.utils.BgUtils

class LLGameMode(override val game: LoveLetter) : GameMode<LLPlayer, LLConfig, BgReport<LLPlayer>>(game) {

    lateinit var deck: LLCardDeck
    var removedDeck: LLCardDeck? = null

    lateinit var winner: LLPlayer


    @Throws(BoardGameException::class)
    override fun endGame() {
        super.endGame()
        // 结束时算分
        val endPhase = LLEndPhase()
        endPhase.execute(this)
    }

    val deckNum: Int
        get() = this.deck.size

    val discardNum: Int
        get() = this.deck.discards.size

    override fun initRound() {
        super.initRound()
        game.players.forEach(LLPlayer::clear)
        this.deck.reset()
    }

    override val isGameOver: Boolean
        get() = game.players.map(LLPlayer::score).any { it >= this.game.config.score }

    @Throws(BoardGameException::class)
    override fun round() {
        game.dealCards()
        game.discardCards()
        // 正式开始回合的出牌阶段,直到剩余最后1个玩家
        this.waitForRoundPhase()

        // 回合结束时,显示玩家得分,并等待玩家确认
        this.waitForResultPhase()
    }

    @Throws(BoardGameException::class)
    override fun setupGame() {
        this.winner = game.getPlayer(0)
        val rm = this.game.getResourceManager<LLResourceManager>()
        this.deck = LLCardDeck(BgUtils.cloneList(rm.cards))
        this.deck.shuffle()
    }

    @Throws(BoardGameException::class)
    private fun waitForResultPhase() {
        this.addListener(LLResultListener(this))
    }

    @Throws(BoardGameException::class)
    private fun waitForRoundPhase() {
        this.addListener(LLRoundListener(this, this.winner))
    }

}
