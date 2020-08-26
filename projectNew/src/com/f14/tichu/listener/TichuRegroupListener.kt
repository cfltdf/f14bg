package com.f14.tichu.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer
import com.f14.tichu.componet.TichuCard
import com.f14.tichu.consts.TichuGameCmd
import com.f14.tichu.consts.TichuType
import java.util.*

/**
 * 换牌阶段
 * @author F14eagle
 */
class TichuRegroupListener(gameMode: TichuGameMode) : TichuActionListener(gameMode) {

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建参数
        for (player in gameMode.game.players) {
            val param = RegroupParam()
            this.setParam(player, param)
        }
        gameMode.report.info("换牌步骤开始")
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val subact = action.getAsString("subact")
        val player = action.getPlayer<TichuPlayer>()
        if ("smallTichu" == subact) {
            // 叫小地主
            gameMode.game.playerCallTichu(player, TichuType.SMALL_TICHU)
        } else {
            // 执行换牌
            this.regroupCards(action)
        }
    }

    override val validCode: Int
        get() = TichuGameCmd.GAME_CODE_REGROUP_PHASE

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        super.onAllPlayerResponsed()
        gameMode.exchangeParam.clear()
        // 执行换牌...
        for (player in gameMode.game.players) {
            val param = this.getParam<RegroupParam>(player)
            for ((p, card) in param.cards) {
                // 将牌换给指定的玩家
                player.hands.removeCard(card)
                p.hands.addCard(card)
                // 记录换牌的参数
                gameMode.exchangeParam.addCard(p, player, card)
            }
            gameMode.report.giveCards(player, param.cards)
        }
        // 将玩家的手牌重新排序
        for (player in gameMode.game.players) {
            player.hands.sort()
        }
        // 刷新所有玩家的卡牌信息
        gameMode.game.sendAllPlayersHandsInfo(null)
    }

    /**
     * 执行换牌
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun regroupCards(action: GameAction) {
        val player = action.getPlayer<TichuPlayer>()
        val param = this.getParam<RegroupParam>(player)
        param.reset()
        for (p in gameMode.game.players) {
            if (p !== player) {
                // 取得换给其他玩家的牌
                val id = action.getAsString("card" + p.position).takeUnless(String?::isNullOrEmpty)
                        ?: throw BoardGameException("请为所有其他玩家各选择一张牌!")
                val card = player.hands.getCard(id)
                param.cards[p] = card
            }
        }
        if (param.cards.size != gameMode.game.currentPlayerNumber - 1) {
            throw BoardGameException("请为所有其他玩家各选择一张牌!")
        }
        this.setPlayerResponsed(player)
    }

    /**
     * 换牌的参数
     * @author F14eagle
     */
    private inner class RegroupParam {
        val cards: MutableMap<TichuPlayer, TichuCard> = LinkedHashMap()

        fun reset() {
            this.cards.clear()
        }
    }

}
