package com.f14.tichu.utils

import com.f14.bg.exception.BoardGameException
import com.f14.tichu.componet.TichuCard
import com.f14.tichu.componet.TichuCardGroup
import com.f14.tichu.consts.AbilityType
import com.f14.tichu.consts.Combination

/**
 * 组合辅助类
 * @author F14eagle
 */
object CombinationUtil {

    /**
     * 取得最大的牌
     * @param cards
     * @return
     */
    private fun getBiggestCard(cards: Collection<TichuCard>): TichuCard {
        return cards.maxBy(TichuCard::point) ?: throw BoardGameException("???")
    }

    /**
     * 取得这些牌中有指定能力的牌
     * @param cards
     * @return
     */
    fun getCard(cards: Collection<TichuCard>, abilityType: AbilityType): TichuCard? {
        return cards.firstOrNull { it.abilityType == abilityType }
    }

    /**
     * 取得各个点数的牌的张数
     * @param cards
     * @return
     */
    fun getCardMap(cards: Collection<TichuCard>): Map<Double, Int> {
        return cards.groupBy(TichuCard::point).mapValues { it.value.size }
    }

    /**
     * 检查这些牌中指定点数的牌的数量
     * @param cards
     * @param point
     * @return
     */
    fun getCardNumber(cards: Collection<TichuCard>, point: Double): Int {
        return cards.count { it.point == point }
    }

    /**
     * 取得关键牌
     * @param group
     * @return
     */
    fun getKeyCard(group: TichuCardGroup): TichuCard {
        return if (group.combination == Combination.FULLHOUSE) {
            // FullHouse是看三张牌的那个
            val point = getCardMap(group.cards).asSequence().first { it.value == 3 }.key
            group.cards.first { it.point == point }
        } else {
            // 其他都是看最大的那张
            getBiggestCard(group.cards)
        }
    }

    /**
     * 检查这些牌中是否有指定能力的牌
     * @param cards
     * @return
     */
    fun hasCard(cards: Collection<TichuCard>, abilityType: AbilityType): Boolean {
        return getCard(cards, abilityType) != null
    }

    /**
     * 检查这些牌中是否有指定点数的牌,不算百搭
     * @param cards
     * @param point
     * @return
     */
    fun hasCard(cards: Collection<TichuCard>, point: Double): Boolean {
        // 不能算百搭
        return cards.any { it.point == point && it.abilityType != AbilityType.PHOENIX }
    }

    /**
     * 检查这些牌是否是Fullhouse
     * @param cards
     * @return
     */
    fun isFullHouse(cards: Collection<TichuCard>): Boolean {
        if (cards.isEmpty()) {
            return false
        }
        // FullHouse必须是5张牌
        if (cards.size != 5) {
            return false
        }
        val m = getCardMap(cards)
        // FullHouse只能有2种的牌
        if (m.size != 2) {
            return false
        }
        // FullHouse只能是3张+2张的组合
        return m.values.all { it in 2..3 }
    }

    /**
     * 检查这些牌是否是姐妹对
     * @param cards
     * @return
     */
    fun isGroupPair(cards: Collection<TichuCard>): Boolean {
        if (cards.isEmpty()) {
            return false
        }
        // 姐妹对必须是双数
        if (cards.size % 2 != 0) {
            return false
        }
        var point = -1.0
        val m = getCardMap(cards)
        if (m.values.any { it != 2 }) {
            return false
        }
        return (m.keys.max()!!.toInt() - m.keys.min()!!.toInt()) == m.keys.count() - 1
        val it = cards.iterator()
        while (it.hasNext()) {
            val c1 = it.next()
            val c2 = it.next()
            if (c1.point != c2.point) {
                return false
            }
            if (point >= 0) {
                // 姐妹对必须数字相连
                if (c1.point != point + 1) {
                    return false
                }
            }
            point = c1.point
        }
        return true
    }

    /**
     * 检查这些牌是否是相同点数
     * @param cards
     * @return
     */
    fun isSamePoint(cards: Collection<TichuCard>): Boolean {
        if (cards.isEmpty()) {
            return false
        }
        return getCardMap(cards).count() == 1
        val point = cards.iterator().next().point
        return cards.none { it.point != point }
    }

    /**
     * 检查这些牌是否是相同花色
     * @param cards
     * @return
     */
    fun isSameType(cards: Collection<TichuCard>): Boolean {
        if (cards.isEmpty()) {
            return false
        }
        val type = cards.iterator().next().cardType
        return cards.none { it.cardType != type }
    }

    /**
     * 检查这些牌是否是顺子
     * @param cards
     * @return
     */
    fun isStraight(cards: Collection<TichuCard>): Boolean {
        return if (cards.isEmpty()) false else cards.size >= 5 && isStraightPoint(cards, false)
        // 顺子至少是5张牌
    }

    /**
     * 检查这些牌是否是连续的
     * @param cards
     * @param hasPhoniex
     * @return
     */
    fun isStraightPoint(cards: Collection<TichuCard>, hasPhoniex: Boolean): Boolean {
        val m = getCardMap(cards.filterNot { it.abilityType == AbilityType.PHOENIX })
        if (m.values.any { it > 1 }) return false
        return (m.keys.max()!!.toInt() - m.keys.min()!!.toInt()) <= m.size - 1 + (if (hasPhoniex) 1 else 0)
        var point = -1.0
        // 凤只可以用一次
        var usedPhoenix = !hasPhoniex
        for (card in cards) {
            // 狗不能用作顺子
            if (card.point == 0.0) {
                return false
            }
            if (point > 0) {
                // 顺子必须是连续的数字
                if (card.point != point + 1) {
                    // 如果不连续,则允许使用一次凤
                    if (usedPhoenix) {
                        return false
                    } else {
                        usedPhoenix = true
                    }
                }
            }
            point = card.point
        }
        return true
    }
}
