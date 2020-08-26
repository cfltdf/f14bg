package com.f14.innovation.utils

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoConsts
import com.f14.innovation.consts.InnoIcon

object InnoUtils {

    /**
     * 将卡牌的level转换成string
     * @param cards
     * @return
     */
    fun cardLevel2String(cards: Collection<InnoCard>) = cards.joinToString(",") { it.level.toString() }

    /**
     * 检查等级参数的合法性
     * @param level
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkLevel(level: Int) {
        level in 1..InnoConsts.MAX_LEVEL || throw BoardGameException("无效的等级参数!")
    }

    /**
     * 取得这些牌中,有几种不同时期的牌的数量
     * @param cards
     * @return
     */
    fun getDifferentLevelCardsNum(cards: Collection<InnoCard>): Int {
        return cards.groupBy(InnoCard::level).keys.size
    }

    /**
     * 取得所有得分最高的玩家
     * @param players
     * @return
     */
    fun getHighestScorePlayers(players: List<InnoPlayer>): List<InnoPlayer> {
        return players.groupBy(InnoPlayer::score).maxBy{ it.key }?.value ?: emptyList()
    }

    /**
     * 取得所有得分最低的玩家
     * @param players
     * @return
     */
    fun getLowestScorePlayers(players: List<InnoPlayer>): List<InnoPlayer> {
        return players.groupBy(InnoPlayer::score).minBy{ it.key }?.value ?: emptyList()
    }

    /**
     * 取得所有牌中最高等级的牌
     * @param cards
     * @return
     */
    fun getMaxLevel(cards: Collection<InnoCard>) = cards.map(InnoCard::level).max() ?: 0

    /**
     * 取得指定牌中,num张最高级的牌中的最低等级
     * @param cards
     * @param num
     * @return
     */
    fun getMaxLevel(cards: Collection<InnoCard>, num: Int) = cards.map(InnoCard::level).sortedDescending()[minOf(cards.size, num)]

    /**
     * 取得所有牌中最小等级的牌
     * @param cards
     * @return
     */
    fun getMinLevel(cards: Collection<InnoCard>) = cards.map(InnoCard::level).min() ?: 99

    /**
     * 取得所有指定符号最多的玩家
     * @param players
     * @param icon
     * @return
     */
    fun getMostIconPlayers(players: List<InnoPlayer>, icon: InnoIcon): List<InnoPlayer> {
        return players.groupBy { it.getIconCount(icon) }.maxBy{ it.key }?.value
                ?: emptyList()
    }

    /**
     * 取得玩家人数对应的成就胜利条件
     * @return
     */
    fun getVictoryAchieveNumber(gameMode: InnoGameMode) = when {
        gameMode.game.isTeamMatch -> 6    // 组队赛时总是返回6
        else -> when (gameMode.game.currentPlayerNumber) {
            2 -> 6
            3 -> 5
            4 -> 4
            else -> -1
        }
    }

    /**
     * 判断卡牌中是否有指定的颜色
     * @param cards
     * @param colors
     * @return
     */
    fun hasColor(cards: Collection<InnoCard>, vararg colors: InnoColor): Boolean {
        return cards.any { it.color in colors }
    }

    /**
     * 判断这些卡牌中是否有相同的颜色
     * @param cards
     * @return
     */
    fun hasSameColor(cards: Collection<InnoCard>): Boolean {
        return cards.groupBy(InnoCard::color).values.any { it.size > 1 }
    }

}
