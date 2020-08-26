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
import com.f14.innovation.utils.InnoUtils
import java.util.*

/**
 * #049-海盗密码 监听器

 * @author F14eagle
 */
class InnoCustom049Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseStackListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    private var availableCards: MutableList<InnoCard> = ArrayList()

    init {
        this.setAvailableCards()
    }

    @Throws(BoardGameException::class)
    override fun checkChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.checkChooseCard(player, cards)
        // 这里应该只会有1张牌,需要检查这张牌是所有可选牌中最低等级的牌
        val card = cards[0]
        if (card.level != InnoUtils.getMinLevel(this.availableCards)) {
            throw BoardGameException("你不能选择这张牌!")
        }
    }

    /**
     * 设置所有可选的卡牌
     */
    private fun setAvailableCards() {
        val player = this.targetPlayer
        player.topCards.filter { this.canChooseCard(player, it) }.forEach { this.availableCards.add(it) }
    }

}
