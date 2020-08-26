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
 * #080-机动性 监听器

 * @author F14eagle
 */
class InnoCustom080Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseStackListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    private val availableCards = HashSet<InnoCard>()

    init {
        this.initAvailableCards()
    }

    @Throws(BoardGameException::class)
    override fun afterProcessChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        this.availableCards.removeAll(cards)
        super.afterProcessChooseCard(player, cards)
    }

    @Throws(BoardGameException::class)
    override fun checkChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.checkChooseCard(player, cards)
        // 选择的牌必须是可选牌中最高时期的2张牌
        // 取得允许选择的最低时期
        val level = InnoUtils.getMaxLevel(availableCards)
        for (card in cards) {
            if (!availableCards.contains(card)) {
                throw BoardGameException("你不能选择这张牌!")
            }
            if (card.level < level) {
                throw BoardGameException("请选择最高等级的牌!")
            }
        }
    }

    /**
     * 取得所有可供选择牌的数量

     * @param player

     * @return
     */
    override fun getAvailableCardNum(player: InnoPlayer): Int {
        return this.availableCards.size
    }

    /**
     * 初始化可选牌
     */
    private fun initAvailableCards() {
        val player = this.targetPlayer
        player.topCards.filterTo(availableCards) { this.canChooseCard(player, it) }
    }

}
