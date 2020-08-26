package com.f14.tichu.componet

import com.f14.tichu.TichuPlayer
import com.f14.tichu.consts.AbilityType
import com.f14.tichu.consts.Combination
import com.f14.tichu.utils.CombinationUtil

/**
 * Tichu中的一组牌
 * @author F14eagle
 */
class TichuCardGroup(val owner: TichuPlayer, cards: Collection<TichuCard>) : Comparable<TichuCardGroup> {
    val cards: List<TichuCard> = cards.toList()
    val combination: Combination?
    val keyCard: TichuCard

    init {
        // 检查并设定该牌的组合
        this.combination = when (this.cards.size) {
            0 -> // 0张什么都不是
                null
            1 -> // 一张牌的只有单张
                Combination.SINGLE
            2 -> // 两张牌的只可能是一对
                when {
                    CombinationUtil.isSamePoint(cards) -> Combination.PAIR
                    else -> null
                }
            3 -> // 三张牌的只可能是三条
                when {
                    CombinationUtil.isSamePoint(cards) -> Combination.TRIO
                    else -> null
                }
            4 -> // 四张的可能是炸弹,或者姐妹对
                when {
                    CombinationUtil.isSamePoint(cards) && !this.hasCard(AbilityType.PHOENIX) -> Combination.BOMBS
                    CombinationUtil.isGroupPair(cards) -> Combination.GROUP_PAIRS
                    else -> null
                }
            5 -> // 5张的可能是fullhouse,顺子或者同花顺
                when {
                    CombinationUtil.isFullHouse(cards) -> Combination.FULLHOUSE
                    CombinationUtil.isStraight(cards) -> when {
                        CombinationUtil.isSameType(cards) -> Combination.BOMBS
                        else -> Combination.STRAIGHT
                    }
                    else -> null
                }
            else -> // 更多牌,只有顺子和姐妹对的可能
                when {
                    CombinationUtil.isStraight(cards) -> when {
                        CombinationUtil.isSameType(cards) -> Combination.BOMBS
                        else -> Combination.STRAIGHT
                    }
                    CombinationUtil.isGroupPair(cards) -> Combination.GROUP_PAIRS
                    else -> null
                }
        }
        this.keyCard = CombinationUtil.getKeyCard(this)
    }

    override fun compareTo(other: TichuCardGroup) = when {
        this.combination == null || other.combination == null -> 0
        this.combination != other.combination -> when {
            this.combination == Combination.BOMBS -> 1
            other.combination == Combination.BOMBS -> -1
            else -> 0
        }
        else -> {
            // 比较关键数的大小
            val p1 = this.compareValue
            val p2 = other.compareValue
            p1.compareTo(p2)
        }
    }

    /**
     * 取得比较用的关键数
     * @return
     */
    val compareValue: Double
        get() = when (this.combination) {
            null -> 0.0
            // 炸弹是先看张数,再看最大的那张
            Combination.BOMBS -> this.cards.size * 100 + this.keyCard.point
            // 其他都是看最大的那张牌
            else -> this.keyCard.point
        }

    /**
     * 判断是否拥有指定能力的牌
     * @param abilityType
     * @return
     */
    fun hasCard(abilityType: AbilityType): Boolean {
        return CombinationUtil.hasCard(cards, abilityType)
    }

    /**
     * 检查这些牌中是否有指定点数的牌
     * @param point
     * @return
     */
    fun hasCard(point: Double): Boolean {
        return CombinationUtil.hasCard(cards, point)
    }

    /**
     * 判断是否是合理的组合
     * @return
     */
    val isValidCombination: Boolean
        get() = this.combination != null

}
