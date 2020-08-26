package com.f14.RFTG.utils

import com.f14.RFTG.card.Ability
import com.f14.RFTG.card.Condition
import com.f14.RFTG.card.RaceCard

/**
 * 银河竞逐专用的工具类
 * @author F14eagle
 */
object RaceUtils {

    /**
     * 检查列表中是否存在相同cardNo的卡牌
     * @param cards
     * @return
     */
    fun checkDuplicate(cards: List<RaceCard>) = cards.groupBy(RaceCard::cardNo).any { it.value.size > 1 }

    /**
     * 取得适用于指定能力的牌
     * @param cards
     * @param ability
     * @return
     */
    fun getValidWorldNum(cards: List<RaceCard>, ability: Ability) = cards.count(ability::test)

    /**
     * 取得适用于指定能力的牌
     * @param cards
     * @param condition
     * @return
     */
    fun getValidWorldNum(cards: List<RaceCard>, condition: Condition) = cards.count(condition::test)
}
