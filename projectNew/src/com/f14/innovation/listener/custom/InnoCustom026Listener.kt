package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.listener.InnoChooseScoreListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #026-光学 监听器

 * @author F14eagle
 */
class InnoCustom026Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseScoreListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        val choosePlayer = gameMode.game.players.any { this.canChoosePlayer(player, it) }
        val check = super.beforeListeningCheck(player)
        // 必须又能选择玩家,又能选择卡牌,才需要回应
        return choosePlayer and check
    }

    /**
     * 判断是否可以选择玩家

     * @param player

     * @param target

     * @return
     */
    private fun canChoosePlayer(player: InnoPlayer, target: InnoPlayer): Boolean {
        // 只能选择分数比自己低的玩家
        if (player === target) {
            return false
        }
        // 只能选择敌对玩家
        return !gameMode.game.isTeammates(player, target) && player.score > target.score
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val cardIds = action.getAsString("cardIds")
        val cards = player.scores.getCards(cardIds)
        this.checkChooseCard(player, cards)

        val targetPosition = action.getAsInt("choosePosition")
        val target = gameMode.game.getPlayer(targetPosition)
        if (!this.canChoosePlayer(player, target)) {
            throw BoardGameException("不能选择这个玩家!")
        }

        this.beforeProcessChooseCard(player, cards)
        this.processChooseCard(player, cards, target)
        this.afterProcessChooseCard(player, cards)
        this.checkPlayerResponsed(player)
    }

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_026

    /**
     * 处理玩家选择的牌

     * @param gameMode

     * @param player

     * @param cards

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>, target: InnoPlayer) {
        // 将计分区的牌转移给目标玩家
        cards.map { gameMode.game.playerRemoveScoreCard(player, it) }.forEach { gameMode.game.playerAddScoreCard(target, it) }
    }

}
