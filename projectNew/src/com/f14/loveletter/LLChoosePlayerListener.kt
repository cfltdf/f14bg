package com.f14.loveletter

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ActionListener
import com.f14.bg.listener.ListenerType
import com.f14.bg.report.BgReport

class LLChoosePlayerListener(private var gameMode: LLGameMode, private var player: LLPlayer, private var card: LLCard) : ActionListener<LLPlayer, LLConfig, BgReport<LLPlayer>>(gameMode, ListenerType.INTERRUPT) {

    private var targetPlayer: LLPlayer? = null

    init {
        this.addListeningPlayer(player)
    }

    fun canChoose(p: LLPlayer) = !p.passed && !(p.position == player.position && this.card.point != 5.0)

    /**
     * 在玩家开始监听前的检验方法
     * @param player
     * @return 如果返回true, 则需要玩家回应, false则不需玩家回应
     */
    override fun beforeListeningCheck(player: LLPlayer): Boolean {
        val chooser = gameMode.game.players.filter(this::canChoose)
        if (chooser.size == 1) this.targetPlayer = chooser.single()
        return chooser.size > 1
    }


    override fun createInterruptParam() = super.createInterruptParam().apply {
        set("position", player.position)
        set("targetPosition", targetPlayer?.position)
    }

    override fun createStartListenCommand(player: LLPlayer) = super.createStartListenCommand(player).public("actionString", this.actionString).public("cardId", card.id)

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val position = action.getAsInt("position")
        val targetPlayer = gameMode.game.getPlayer(position)
        if (targetPlayer.passed) throw BoardGameException("不能选择已出局的玩家!")
        if (targetPlayer === player && card.point != 5.0) throw BoardGameException("不能选择自己!")
        gameMode.game.choosePlayer(player, targetPlayer)
        this.targetPlayer = targetPlayer
        this.setPlayerResponsed(player)
    }


    private val actionString: String
        get() = ""

    override val validCode: Int
        get() = LLGameCmd.GAME_CODE_GIVE_SCORE

}
