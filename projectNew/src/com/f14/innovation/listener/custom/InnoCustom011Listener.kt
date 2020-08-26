package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChooseHandListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #011-衣服 监听器

 * @author F14eagle
 */
class InnoCustom011Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        // 检查玩家手牌中是否存在置顶牌中没有的颜色,如果不存在就不需要行动
        val hasAvailableCard = player.hands.getCards().any { !player.hasCardStack(it.color!!) }
        // 如果没有可用的牌,就不需要行动
        return hasAvailableCard && super.beforeListeningCheck(player)
    }

    @Throws(BoardGameException::class)
    override fun checkChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.checkChooseCard(player, cards)
        // 只能选择手牌中最低时期的牌
        if (cards.any { player.hasCardStack(it.color!!) }) throw BoardGameException(this.getMsg(player))
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 融合选择的手牌
        for (card in cards) {
            gameMode.game.playerMeldHandCard(player, card)
        }
    }

}
