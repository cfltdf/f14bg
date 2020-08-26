package com.f14.tichu.componet

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer
import com.f14.tichu.consts.AbilityType
import com.f14.tichu.consts.Combination
import com.f14.tichu.utils.CombinationUtil
import java.util.*
import kotlin.math.min

/**
 * 出牌回合的一些内容管理

 * @author F14eagle
 */
class RoundManager(internal var gameMode: TichuGameMode) {
    var round = 0
    var cardGroups: MutableList<MutableList<TichuCardGroup>> = ArrayList()

    /**
     * 添加出牌
     * @param group
     */
    fun addCardGroup(group: TichuCardGroup) {
        this.currentCardGroups.add(group)
    }

    /**
     * 出牌时检查是否有狗,并设置相应的值
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkDog(cards: List<TichuCard>) =// 检查出的牌中是否有狗
            CombinationUtil.getCard(cards, AbilityType.DOG) != null

    /**
     * 出牌时检查是否有雀
     * @param cards
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkMahJong(cards: List<TichuCard>) = // 检查出的牌中是否有雀
            CombinationUtil.getCard(cards, AbilityType.MAH_JONG) != null

    /**
     * 检查是否可以跳过
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkPass(player: TichuPlayer) {
        this.lastCardGroup ?: throw BoardGameException("请出牌!")
        // 如果存在许愿数,则检查玩家是否拥有该许愿数的可出牌组合
        if (this.checkWishedPoint(player)) throw BoardGameException("存在许愿,你必须出牌!")
    }

    /**
     * 出牌时检查是否有凤,并设置相应的值
     * @param cards
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkPhoenix(cards: MutableList<TichuCard>, action: GameAction) {
        // 检查出的牌中是否有凤
        val pheonix = CombinationUtil.getCard(cards, AbilityType.PHOENIX) ?: return
        if (cards.size == 1) {
            // 如果是单张,则自动设置凤的值,比任意牌大0.5
            val group = this.lastCardGroup
            pheonix.point = when (group) {
                null -> 1.5
                else -> min(17.0, group.keyCard.point + 0.5) // 值最多不能超过17,因为龙的值是20
            }
        } else {
            // 如果是多张,则从参数中取得玩家指定的凤的值
            val point = action.getAsInt("point")
            if (point < 2 || point > 14) throw BoardGameException("请选择正确的值!")
            pheonix.point = point.toDouble()
            // 将牌重新排序
            cards.sort()
        }
    }

    /**
     * 判断是否可以这样出牌
     * @param group
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkPlayCard(group: TichuCardGroup) {
        // 判断是否符合出牌规则...
        if (!group.isValidCombination) throw BoardGameException("你不能这样出牌!")
        val lastGroup = this.lastCardGroup
        // 如果没人出过牌,则可以出牌
        if (lastGroup != null) {
            // 只能按照相同组合出牌,除了炸蛋...
            if (group.combination != Combination.BOMBS && (group.combination != lastGroup.combination || group.cards.size != lastGroup.cards.size)) {
                throw BoardGameException("你不能这样出牌!")
            }
            if (group <= lastGroup) throw BoardGameException("出牌必须比上一把大!")
        }
        // 出炸弹时无需检查许愿情况
        if (this.gameMode.wishedPoint > 0 && group.combination != Combination.BOMBS) {
            // 如果存在许愿,则检查出牌中是否有许愿的数
            if (!group.hasCard(this.gameMode.wishedPoint.toDouble()) && this.checkWishedPoint(group.owner)) {
                throw BoardGameException("存在许愿,你必须出许愿的牌!")
            }
        }
    }

    /**
     * 检查玩家是否拥有符合许愿的牌
     * @param player
     * @return
     */
    fun checkWishedPoint(player: TichuPlayer): Boolean = when {
        this.gameMode.wishedPoint <= 0 -> false
        !player.hasCard(this.gameMode.wishedPoint.toDouble()) -> false
        else -> {
            val wishedPoint = this.gameMode.wishedPoint.toDouble()
            val group = this.lastCardGroup
            val compareValue = group?.compareValue ?: 0.0
            val check = TichuCardCheck(player, player.hands.cards)
            when (group) {
                null -> check.hasCard(wishedPoint, compareValue)
                else -> group.combination != Combination.BOMBS && check.getBombs(wishedPoint).isNotEmpty() || when (group.combination) {
                    Combination.SINGLE -> check.hasCard(wishedPoint, compareValue)
                    Combination.PAIR -> check.hasPairs(wishedPoint, compareValue)
                    Combination.TRIO -> check.hasTrios(wishedPoint, compareValue)
                    Combination.FULLHOUSE -> check.hasFullhouses(wishedPoint, compareValue)
                    Combination.STRAIGHT -> check.hasStraight(wishedPoint, compareValue, group.cards.size)
                    Combination.GROUP_PAIRS -> check.hasPairGroup(wishedPoint, compareValue, group.cards.size)
                    Combination.BOMBS -> check.hasBomb(wishedPoint, compareValue)
                    null -> false
                }
            }
        }
    }

    /**
     * 取得当前回合的出的牌
     * @return
     */
    val currentCardGroups: MutableList<TichuCardGroup>
        get() = this.cardGroups.last()

    /**
     * 取得当前回合打出牌的分数
     * @return
     */
    val currentScore: Int
        get() = this.currentCardGroups.flatMap(TichuCardGroup::cards).sumBy(TichuCard::score)

    /**
     * 取得当前回合最后一次出的牌
     * @return
     */

    val lastCardGroup: TichuCardGroup?
        get() = this.currentCardGroups.lastOrNull()

    /**
     * 判断当前轮出的牌中是否有指定能力的牌
     * @param abilityType
     * @return
     */
    fun isCurrentHasCard(abilityType: AbilityType) = this.currentCardGroups.any { it.hasCard(abilityType) }

    /**
     * 新的回合开始
     */
    fun newRound() {
        this.cardGroups.add(ArrayList())
        round++
    }

}