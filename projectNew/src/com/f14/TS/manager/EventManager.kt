package com.f14.TS.manager

import com.f14.TS.TSGameMode
import com.f14.TS.component.TSCard
import com.f14.TS.consts.*
import com.f14.bg.common.ListMap

import java.util.*

class EventManager(private val gameMode: TSGameMode) {

    val activedCards: MutableSet<TSCard> = LinkedHashSet()

    val superPowerCards = ListMap<SuperPower, TSCard>()

    val roundCounter: MutableMap<String, Int> = HashMap()

    /**
     * 添加生效的卡牌
     * @param superPower
     * @param card
     */
    fun addActivedCard(superPower: SuperPower, card: TSCard) {
        // 检查该牌是否会影响到当前已经生效的卡牌
        val canceledCards = this.getActivedCards().filter { it.isCanceledByCard(card) }
        if (canceledCards.isNotEmpty()) {
            // 移除这些牌的效果
            for (c in canceledCards) {
                gameMode.game.removeActivedCard(c)
            }
        }
        // 添加到生效列表中
        this.activedCards.add(card)
        this.superPowerCards.add(superPower, card)
        // 设置生效回合数为0
        this.setActiveRound(superPower, card, 0)
    }

    /**
     * 判断是否可以触发指定卡牌的能力
     * @param card
     * @return
     */
    fun canActiveCard(card: TSCard): Boolean = when {
        card.tsCardNo == 7 && gameMode.game.ussrPlayer.hasEffect(EffectType.USSR_NO_SOCIALIST) && gameMode.round <= 2 -> false
        card.tsCardNo == 10 && gameMode.game.ussrPlayer.hasEffect(EffectType.USSR_NO_BLOCKADE) -> false
        card.tsCardNo == 28 && gameMode.game.ussrPlayer.hasEffect(EffectType.USSR_NO_SUEZ) -> false
        card.preventedCardNos.isNotEmpty() && card.preventedCardNos.any(this::isCardActived) -> false // 先检查是否存在阻止卡牌生效的卡牌
        card.requireCardNos.isNotEmpty() && card.requireCardNos.none(this::isCardActived) -> false // 判断需求前置卡牌
        !card.canHeadLine && gameMode.actionPhase === TSActionPhase.HEADLINE -> false // 如果是联合国,则不能在头条阶段生效
        gameMode.game.getPlayer(card.superPower)?.params?.getInteger(TSConsts.DOUBLE_AGENT_NO) == card.tsCardNo -> false // 双重间谍
        else -> true
    }

    /**
     * 判断是否可以触发指定卡牌的能力
     * @param card
     * @return
     */
    fun canPlayOpCard(card: TSCard) = when {
        !card.canHeadLine && gameMode.actionPhase === TSActionPhase.HEADLINE -> false// 如果是联合国,则不能在头条阶段生效
        else -> true
    }

    /**
     * 取得指定超级大国的生效卡牌(NONE则为全局)
     * @param tsCardNo
     * @return
     */
    fun getActivedCard(tsCardNo: Int): TSCard? {
        return this.getActivedCards().firstOrNull { it.tsCardNo == tsCardNo }
    }

    /**
     * 取得所有生效的卡牌
     * @return
     */
    fun getActivedCards(): Collection<TSCard> {
        return this.activedCards
    }

    /**
     * 取得指定超级大国的生效卡牌(NONE则为全局)
     * @param target
     * @return
     */
    fun getActivedCards(target: SuperPower): Collection<TSCard> {
        return this.superPowerCards.getList(target)
    }

    /**
     * 取得生效次数对应的key值
     * @param superPower
     * @param card
     */

    private fun getActiveKey(superPower: SuperPower, card: TSCard): String {
        return "$superPower-${card.tsCardNo}"
    }

    /**
     * 取得已生效的回合数
     * @param superPower
     * @param card
     * @return
     */
    private fun getActiveRound(superPower: SuperPower, card: TSCard): Int {
        val key = this.getActiveKey(superPower, card)
        return this.roundCounter[key] ?: 0
    }

    /**
     * 判断指定卡牌编号的卡牌是否已经生效
     * @param cardNo
     * @return
     */
    fun isCardActived(cardNo: Int): Boolean {
        return this.getActivedCards().any { it.tsCardNo == cardNo }
    }

    /**
     * 刷新生效回合数
     */
    fun refreshActiveRound(power: SuperPower) {
        // 设置生效回合数为1
        this.getActivedCards(power)
                .filter { it.durationResult?.durationSession === TSDurationSession.ACTION_ROUND }
                .forEach { this.setActiveRound(power, it, 1) }
    }

    /**
     * 移除生效的卡牌
     * @param card
     * @return
     */
    fun removeActivedCard(card: TSCard) {
        this.activedCards.remove(card)
        this.superPowerCards.remove(card)
    }

    /**
     * 移除指定玩家行动轮的所有生效的卡牌
     * @param power
     * @return
     */
    fun removeRoundEffectCards(power: SuperPower): Collection<TSCard> {
        val cards = LinkedHashSet<TSCard>()
        this.getActivedCards(power).forEach {
            when (it.durationResult?.durationSession) {
                TSDurationSession.INSTANT // 立即生效的效果将被直接移除
                -> cards.add(it)
                TSDurationSession.ACTION_ROUND // 在下一个行动轮结束时移除
                -> if (this.getActiveRound(power, it) > 0) cards.add(it)
                else -> {
                }
            }
        }
        cards.forEach(this::removeActivedCard)
        return cards
    }

    /**
     * 移除所有回合生效的卡牌
     * @return
     */
    fun removeTurnEffectCards(): Collection<TSCard> {
        val cards = this.activedCards.filter { it.durationResult?.durationSession === TSDurationSession.TURN }.toSet()
        cards.forEach(this::removeActivedCard)
        return cards
    }

    /**
     * 设置已生效的回合数
     * @param superPower
     * @param card
     * @param i
     * @return
     */
    private fun setActiveRound(superPower: SuperPower, card: TSCard, i: Int) {
        val key = this.getActiveKey(superPower, card)
        this.roundCounter[key] = i
    }
}
