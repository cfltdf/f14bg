package com.f14.tichu.componet

import com.f14.bg.utils.BgUtils
import com.f14.tichu.TichuPlayer
import com.f14.tichu.consts.AbilityType
import com.f14.tichu.consts.CardType
import com.f14.tichu.utils.CombinationUtil
import java.util.*

class TichuCardCheck(private var player: TichuPlayer, cards: Collection<TichuCard>) {

    private val cards = cards.sorted()
    private val pointCards: Map<Double, List<TichuCard>> = cards.filter { it.abilityType == null }.groupBy(TichuCard::point)
    private val typeCards: Map<CardType, List<TichuCard>> = cards.filter { it.abilityType == null }.groupBy { it.cardType!! }
    private val hasPhoenix = CombinationUtil.hasCard(cards, AbilityType.PHOENIX) // 牌组中是否有凤
    private val phonexNum = if (hasPhoenix) 1 else 0

    init {
        //        for (card in cards.filter { it.abilityType == null }) { // 所有特殊功能牌都不算在内
//            pointCards.add(card.point, card)
//            typeCards.add(card.cardType!!, card)
//        }
    }

    /**
     * 取得所有的炸弹
     * @return
     */
    val bombs: Collection<TichuCardGroup>
        get() = listOf(
                // 首先取得4张的炸弹
                pointCards.values.filter { it.size == 4 },
                // 取得能组成同花顺的炸弹...
                typeCards.values.flatMap { cards -> (0 until cards.size).map(cards::drop) }
                        // 同花顺至少需要5张牌
                        // 需要检查最多可能张数的同花顺
                        .flatMap { (5..it.size).asSequence().map(it::take).takeWhile(CombinationUtil::isStraight).toList() }).flatten().map { TichuCardGroup(player, it) }

    /**
     * 取得所有拥有指定数字的炸弹
     * @param point
     * @return
     */
    fun getBombs(point: Double) = this.bombs.filter { it.cards.any { it.point == point } }

    /**
     * 取得所有拥有指定数字的对牌
     * @param point
     * @return
     */
    fun getPairs(point: Double): Collection<TichuCardGroup> {
        val cards = this.pointCards[point] ?: emptyList()
        return when {
            cards.size >= 2 -> listOf(TichuCardGroup(player, cards.subList(0, 2)))
            else -> emptyList()
        }
    }

    /**
     * 判断是否有指定点数,并且比compareValue大的炸弹
     * @param point
     * @param compareValue
     * @return
     */
    fun hasBomb(point: Double, compareValue: Double): Boolean {
        val groups = this.getBombs(point)
        return groups.any { it.compareValue > compareValue }
    }

    /**
     * 判断是否有指定点数的牌
     * @param point
     * @return
     */
    fun hasCard(point: Double) = this.pointCards[point]?.isNotEmpty() ?: false

    /**
     * 判断是否有指定点数,并且比compareValue大的牌
     * @param  *
     * @param compareValue
     * @return
     */
    fun hasCard(point: Double, compareValue: Double) = point > compareValue && hasCard(point)

    /**
     * 判断是否有指定点数,并且比compareValue大的fullhouse
     * @param point
     * @param compareValue
     * @return
     */
    fun hasFullhouses(point: Double, compareValue: Double): Boolean {
        val cards = this.pointCards[point] ?: emptyList()
        val size = cards.size
        if (size + this.phonexNum >= 2) {
            val pnum = if (this.hasPhoenix && size != 1) 1 else 0
            // 检查是否存在比compareValue大的三张,当然不能包括自己
            val result = this.pointCards.filterKeys { it > compareValue && it != point }.any { it.value.size + pnum >= 3 }
            if (result) return true
//            for (list in this.pointCards.filterKeys { it > compareValue && it != point }.values) {
//                var s = list.size
//                if (this.hasPhoenix && size != 1) s += 1
//                if (s >= 3) return true
//            }
        }
        if (size + this.phonexNum >= 3) {
            // 如果指定点数的是3张牌,则该牌必须比指定的数大
            if (point > compareValue) {
                val pnum = if (this.hasPhoenix && size != 2) 1 else 0
                // 检查是否存在对牌,当然也不能包括自己
                val result = this.pointCards.filterKeys { it != point }.any { it.value.size + pnum >= 2 }
                if (result) return true
//                for (list in this.pointCards.filterKeys { it != point }.values) {
//                    var s = list.size
//                    if (this.hasPhoenix && size != 2) s += 1
//                    if (s >= 2) return true
//                }
            }
        }
        return false
    }

    /**
     * 判断是否有指定点数和长度,并且比compareValue大的姐妹对
     * @param point
     * @param compareValue
     * @param length
     * @return
     */
    fun hasPairGroup(point: Double, compareValue: Double, length: Int): Boolean {
        val l = length / 2
        val from = compareValue.toInt() + 2 - l
        val to = 15 - l
        if (from > to || from > point) return false
        val cm = CombinationUtil.getCardMap(cards)
        return (from..to)
                .filter { it + l > point }
                .map { (it until (it + l)).map(Int::toDouble) }
                .filter { it.all(cm::containsKey) }
                .any { list ->
                    when (list.count { cm[it] == 1 }) {
                        0 -> true
                        1 -> this.hasPhoenix
                        else -> false
                    }
                }
        // List<Double> keys = new ArrayList<Double>(this.pointCards.keySet());
        // 整理出所有点数的牌各1或2张
        val cards = ArrayList<TichuCard>()
        for (d in 2..14) {
            val cs = this.pointCards[d.toDouble()] ?: emptyList()
            val size = cs.size
            if (size >= 1) {
                var c = TichuCard()
                c.point = d.toDouble()
                // 保证每个相隔的牌类型不同
                if (cards.size % 2 == 1) {
                    c.cardType = CardType.JADE
                }
                cards.add(c)
                // 如果只有1张则添加1张空牌,如果有2张则添加2张
                c = TichuCard()
                c.point = if (size == 1) 0.0 else d.toDouble()
                // 保证每个相隔的牌类型不同
                if (cards.size % 2 == 1) {
                    c.cardType = CardType.JADE
                }
                cards.add(c)

            }
        }
        val groups = ArrayList<TichuCardGroup>()
        val size = cards.size
        // 姐妹对至少需要4张牌
        for (i in 0..size - 4) {
            // 需要检查最多可能张数的姐妹对
            var j = i + 4
            while (j <= size) {
                val cs = cards.subList(i, j)
                val temp = ArrayList<TichuCard>()
                val check = ArrayList<TichuCard>()
                // 将每组牌的第1张和第2张分成2个list
                var k = 0
                while (k < cs.size) {
                    temp.add(cs[k])
                    check.add(cs[k + 1])
                    k += 2
                }

                // 必须是指定长度的姐妹对
                if (temp.size * 2 == length) {
                    // 检查第1组是否是姐妹对
                    if (CombinationUtil.isStraightPoint(temp, false)) {
                        // 如果是,则检查是否使用了凤
                        // 检查第2组牌中为0的牌数量
                        val needNum = CombinationUtil.getCardNumber(check, 0.0)
                        if (this.hasPhoenix && needNum <= 1 || !this.hasPhoenix && needNum == 0) {
                            // 为姐妹对创建副本
                            val instance = ArrayList(temp)
                            instance.addAll(BgUtils.cloneList(temp))
                            instance.sort()
                            val group = TichuCardGroup(player, instance)
                            groups.add(group)
                        } else {
                            break
                        }
                    } else {
                        // 如果不是姐妹对,则从下一张牌开始检查
                        break
                    }
                }
                j += 2
            }
        }
        // 检查顺子中是否有存在point的,并且该顺子的compareValue比指定的大
        return groups.filter { it.compareValue > compareValue }.flatMap(TichuCardGroup::cards).any { it.point == point }
    }

    /**
     * 判断是否有指定点数,并且比compareValue大的对牌
     * @param point
     * @param compareValue
     * @return
     */
    fun hasPairs(point: Double, compareValue: Double): Boolean {
        if (point <= compareValue) {
            return false
        }
        val cards = this.pointCards[point] ?: emptyList()
        val size = cards.size + this.phonexNum
        return size >= 2
    }

    /**
     * 判断是否有指定点数和长度,并且比compareValue大的顺子
     * @param point
     * @param compareValue
     * @param length
     * @return
     */
    fun hasStraight(point: Double, compareValue: Double, length: Int): Boolean {
        val from = compareValue.toInt() + 2 - length
        val to = 15 - length
        if (from > to || from > point) return false
        val cm = CombinationUtil.getCardMap(cards)
        return (from..to).filter { it + length > point }.map { (it until it + length).map(Int::toDouble) }.any { list ->
            when (list.count { !cm.containsKey(it) }) {
                0 -> true
                1 -> this.hasPhoenix
                else -> false
            }
        }
    }

    /**
     * 判断是否有指定点数,并且比compareValue大的三张牌
     * @param point
     * @param compareValue
     * @return
     */
    fun hasTrios(point: Double, compareValue: Double): Boolean {
        if (point <= compareValue) {
            return false
        }
        val cards = this.pointCards[point] ?: emptyList()
        val size = cards.size + this.phonexNum
        return size >= 3
    }

}
