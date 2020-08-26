package com.f14.loveletter

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.OrderActionListener
import com.f14.bg.report.BgReport

class LLRoundListener(var gameMode: LLGameMode, private var startPlayer: LLPlayer) : OrderActionListener<LLPlayer, LLConfig, BgReport<LLPlayer>>(gameMode) {
    private var ending: Boolean = false
    private var leftPlayer: Int = 0

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val p = action.getPlayer<LLPlayer>()
        val cardId = action.getAsString("cardId")
        val firstCard = p.firstCard ?: throw BoardGameException("你不能出牌!")
        val secondCard = p.secondCard ?: throw BoardGameException("你不能出牌!")
        val (selectedCard, leftCard) = when (cardId) {
            firstCard.id -> firstCard to secondCard
            secondCard.id -> secondCard to firstCard
            else -> throw BoardGameException("找不到选择的牌!")
        }
        if (leftCard.point == 7.0 && (selectedCard.point == 5.0 || selectedCard.point == 6.0)) {
            throw BoardGameException("你不能出这张牌!")
        }
        gameMode.game.playerDiscard(p, selectedCard, leftCard)
        this.executeCard(p, selectedCard)
        when {
            this.leftPlayer + 1 >= gameMode.game.currentPlayerNumber || this.ending -> this.setAllPlayerResponsed()
            p.passed -> this.setPlayerResponsed(p)
            else -> this.setPlayerResponsedTemp(p)
        }

    }

    private fun executeCard(p: LLPlayer, selectedCard: LLCard) {
        fun choosePlayer(block: (LLPlayer) -> Unit) {
            val l = LLChoosePlayerListener(gameMode, p, selectedCard)
            val param = gameMode.insertListener(l)
            val targetPosition = param.getInteger("targetPosition")
            val targetPlayer = gameMode.game.getPlayer(targetPosition)
            if (!targetPlayer.protect) block(targetPlayer)
        }
        when (selectedCard.point) {
            8.0 -> this.playerPass(gameMode, p)
            4.0 -> gameMode.game.protectPlayer(p, true)
            1.0 -> choosePlayer { targetPlayer ->
                val al = LLWishListener(gameMode, p)
                val param = gameMode.insertListener(al)
                val point = param.getDouble("point")
                if (targetPlayer.firstCard?.point == point) {
                    this.playerPass(gameMode, targetPlayer)
                }
            }
            2.0 -> choosePlayer { targetPlayer ->
                val al = LLShowCardListener(gameMode, p, targetPlayer)
                gameMode.insertListener(al)
            }
            3.0 -> choosePlayer { targetPlayer ->
                if (p.firstCard!!.point > targetPlayer.firstCard!!.point) {
                    this.playerPass(gameMode, targetPlayer)
                } else if (p.firstCard!!.point < targetPlayer.firstCard!!.point) {
                    this.playerPass(gameMode, p)
                }
            }
            5.0 -> choosePlayer { targetPlayer ->
                if (gameMode.game.playerDiscard(targetPlayer)) {
                    this.playerPass(gameMode, targetPlayer)
                } else {
                    gameMode.game.deal(targetPlayer)
                    if (gameMode.deckNum == 0) this.ending = true
                }
            }
            6.0 -> choosePlayer { targetPlayer ->
                gameMode.game.playerExchange(p, targetPlayer)
            }
        }
    }


    override val playersByOrder
        get() = gameMode.game.getPlayersByOrder(startPlayer)

    override val validCode: Int
        get() = LLGameCmd.GAME_CODE_ROUND_PHASE

    @Throws(BoardGameException::class)
    override fun onPlayerTurn(player: LLPlayer) {
        super.onPlayerTurn(player)
        if (player.passed || this.leftPlayer + 1 == gameMode.game.currentPlayerNumber || this.ending) {
            return this.setPlayerResponsed(player)
        }
        gameMode.game.protectPlayer(player, false)
        gameMode.game.deal(player)
        if (gameMode.deckNum == 0) this.ending = true
    }

    private fun playerPass(gameMode: LLGameMode, player: LLPlayer) {
        gameMode.game.playerPass(player)
        this.leftPlayer++
    }

}
