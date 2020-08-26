package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChooseStackListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #044-解剖学 监听器

 * @author F14eagle
 */
class InnoCustom044Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseStackListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        // 如果不存在归还的卡牌,则不用执行
        return this.returnedCard != null && super.beforeListeningCheck(player)
    }

    override fun canChooseCard(player: InnoPlayer, card: InnoCard): Boolean {
        // 选择的牌必须和归还的牌是同一时期的
        val returnCard = this.returnedCard
        return !(returnCard != null && returnCard.level != card.level) && super.canChooseCard(player, card)
    }

    /**
     * 取得归还的卡牌

     * @return
     */

    private val returnedCard: InnoCard?
        get() = when {
            this.resultParam.cards.empty -> null
            else -> this.resultParam.cards.cards[0]
        }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 选择你版图中一张与计分区所归还的卡牌相同时期的置顶牌归还!
        cards.map { gameMode.game.playerRemoveTopCard(player, it.color!!) }.forEach { gameMode.game.playerReturnCard(player, it) }
    }

}
