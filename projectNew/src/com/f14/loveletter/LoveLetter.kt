/**

 */
package com.f14.loveletter

import com.f14.F14bg.network.CmdFactory
import com.f14.bg.BoardGame
import com.f14.bg.consts.BgVersion
import com.f14.bg.exception.BoardGameException
import com.f14.bg.report.BgReport
import com.f14.bg.utils.BgUtils
import net.sf.json.JSONObject


/**
 * @author 奈奈
 */
class LoveLetter : BoardGame<LLPlayer, LLConfig, BgReport<LLPlayer>>() {
    override val mode
        get() = gameMode
    lateinit var gameMode: LLGameMode
        private set

    fun choosePlayer(player: LLPlayer, targetPlayer: LLPlayer) {
        this.report.action(player, "选择了" + targetPlayer.reportString)
    }


    @Throws(BoardGameException::class)
    override fun createConfig(obj: JSONObject): LLConfig {
        val config = LLConfig()
        config.versions.add(BgVersion.BASE)
        return config
    }


    override fun initConfig() {
        val config = LLConfig()
        config.versions.add(BgVersion.BASE)
        this.config = config
    }

    override fun initConst() = Unit

    override fun initReport() {
        this.report = BgReport(this)
    }

    fun playerDiscard(p: LLPlayer): Boolean {
        val card = p.firstCard!!
        p.discardCards.add(card)
        p.firstCard = null
        this.sendPlayerHandsInfo(p, null)
        this.sendPlayerPlayCardInfo(p, null)
        this.report.action(p, "弃掉了" + card.reportString)
        return card.point == 8.0
    }

    fun playerDiscard(p: LLPlayer, selectedCard: LLCard, leftCard: LLCard) {
        p.firstCard = leftCard
        p.secondCard = null
        p.discardCards.add(selectedCard)
        this.report.action(p, "打出了" + selectedCard.reportString)
        this.sendPlayerHandsInfo(p, null)
        this.sendPlayerPlayCardInfo(p, null)
    }

    fun playerExchange(player: LLPlayer, targetPlayer: LLPlayer) {
        val (card, targetCard) = player.firstCard to targetPlayer.firstCard
        player.firstCard = targetCard
        targetPlayer.firstCard = card
        this.report.action(player, "与 ${targetPlayer.reportString} 交换了手牌!")
        this.sendPlayerHandsInfo(player, null)
        this.sendPlayerHandsInfo(targetPlayer, null)
    }

    fun playerPass(p: LLPlayer) {
        this.report.action(p, "出局", true)
        p.firstCard?.let { card ->
            p.discardCards.add(card)
            this.report.action(p, "弃掉了" + card.reportString)
        }
        p.firstCard = null
        p.passed = true
        this.sendPlayerBaseInfo(p, null)
        this.sendPlayerHandsInfo(p, null)
        this.sendPlayerPlayCardInfo(p, null)
    }

    fun playerWin(player: LLPlayer) {
        player.score += 1
        gameMode.winner = player
        this.report.action(player, "赢得一局", true)
    }

    fun playerWish(player: LLPlayer, point: Double) {
        val rm = getResourceManager<LLResourceManager>()
        val card = rm.cards.first { it.point == point }
        this.report.action(player, "猜测了${card.reportString}")
    }

    fun sendBaseGameInfo(receiver: LLPlayer?) {
        CmdFactory.createGameResponse(LLGameCmd.GAME_CODE_BASE_INFO, -1).public("deckNum", gameMode.deckNum).public("discardNum", gameMode.discardNum).public("discardCardIds", gameMode.removedDeck?.discards?.joinToString(separator = ",", transform = LLCard::id)).send(this, receiver)
    }

    @Throws(BoardGameException::class)
    override fun sendGameInfo(receiver: LLPlayer?) {
        this.sendBaseGameInfo(receiver)
    }

    @Throws(BoardGameException::class)
    override fun sendInitInfo(receiver: LLPlayer?) = Unit

    fun sendPlayerBaseInfo(player: LLPlayer, receiver: LLPlayer?) {
        CmdFactory.createGameResponse(LLGameCmd.GAME_CODE_PLAYER_INFO, player.position).public("playerInfo", player.toMap()).send(this, receiver)
    }

    fun sendPlayerHandsInfo(player: LLPlayer, receiver: LLPlayer?) {
        CmdFactory.createGameResponse(LLGameCmd.GAME_CODE_PLAYER_HAND, player.position).public("firstCard", player.firstCard != null).private("firstCardId", player.firstCard?.id).public("secondCard", player.secondCard != null).private("secondCardId", player.secondCard?.id).send(this, receiver)
    }

    fun sendPlayerPlayCardInfo(player: LLPlayer, receiver: LLPlayer?) {
        CmdFactory.createGameResponse(LLGameCmd.GAME_CODE_PLAYER_PLAY_CARD, player.position).public("cardIds", BgUtils.card2String(player.discardCards)).send(this, receiver)
    }

    @Throws(BoardGameException::class)
    override fun sendPlayerPlayingInfo(receiver: LLPlayer?) {
        this.players.forEach { player ->
            // 发送玩家的基本信息
            this.sendPlayerBaseInfo(player, receiver)
            // 发送玩家手牌信息
            this.sendPlayerHandsInfo(player, receiver)
            // 发送玩家最后出牌的信息
            this.sendPlayerPlayCardInfo(player, receiver)
        }
    }

    @Throws(BoardGameException::class)
    override fun setupGame() {
        this.config.playerNumber = this.currentPlayerNumber
        this.gameMode = LLGameMode(this)
    }

    fun deal(player: LLPlayer) {
        val c = gameMode.deck.draw()
        when (player.firstCard) {
            null -> player.firstCard = c
            else -> player.secondCard = c
        }
        this.sendBaseGameInfo(null)
//        this.sendPlayerBaseInfo(player, null)
        this.sendPlayerHandsInfo(player, null)

    }

    @Throws(BoardGameException::class)
    fun dealCards() {
        players.forEach { it.firstCard = gameMode.deck.draw() }
    }

    @Throws(BoardGameException::class)
    fun discardCards() {
        gameMode.deck.discard(gameMode.deck.draw(1))
        if (this.currentPlayerNumber == 2) {
            gameMode.removedDeck = LLCardDeck(emptyList()).also {
                it.discard(gameMode.deck.draw(3))
            }
        }
        this.sendBaseGameInfo(null)
        this.sendPlayerPlayingInfo(null)
    }

    fun protectPlayer(p: LLPlayer, protect: Boolean) {
        p.protect = protect
        this.sendPlayerBaseInfo(p, null)
    }
}
