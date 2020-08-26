package com.f14.tichu.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer
import com.f14.tichu.componet.RoundManager
import com.f14.tichu.componet.TichuCardGroup
import com.f14.tichu.consts.Combination
import com.f14.tichu.consts.TichuGameCmd
import com.f14.tichu.consts.TichuType

class TichuBombActionListener(gameMode: TichuGameMode, trigPlayer: TichuPlayer, private val roundManager: RoundManager) : TichuInterruptListener(gameMode, trigPlayer) {

    private var bomb: TichuCardGroup? = null

    init {
        this.addListeningPlayer(trigPlayer)
    }

    override fun createInterruptParam() = super.createInterruptParam().also {
        it["bomb"] = bomb
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val subact = action.getAsString("subact")
        val player = action.getPlayer<TichuPlayer>()
        when (subact) {
            "pass" -> // 可以不出炸弹,直接结束
                this.setPlayerResponsed(player)
            "smallTichu" -> // 叫小地主
                gameMode.game.playerCallTichu(player, TichuType.SMALL_TICHU)
            else -> // 否则就需要检查出炸弹了
                this.playBomb(action)
        }
    }


    override val actionString: String
        get() = "BOMB_ROUND"

    override val validCode: Int
        get() = TichuGameCmd.GAME_CODE_BOMB_PHASE

    /**
     * 玩家出炸弹
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun playBomb(action: GameAction) {
        val player = action.getPlayer<TichuPlayer>()
        val cardIds = action.getAsString("cardIds").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择要出的牌!")
        val cards = player.hands.getCards(cardIds)
        // 整理组合
        val group = TichuCardGroup(player, cards)
        if (group.combination != Combination.BOMBS) {
            throw BoardGameException("你只能选择炸弹!")
        }
        this.roundManager.checkPlayCard(group)
        this.bomb = group

        // 出牌的逻辑在回调函数中完成
        this.setPlayerResponsed(player)
    }

}
