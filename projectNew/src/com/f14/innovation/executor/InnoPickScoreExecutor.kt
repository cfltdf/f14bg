package com.f14.innovation.executor

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCardDeck
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import kotlin.math.min

/**
 * 选择分数牌的行动

 * @author F14eagle
 */
class InnoPickScoreExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 按照设置的不同条件来选择分数牌
        val player = this.targetPlayer
        val deck: InnoCardDeck? = when {
            "MAX_LEVEL" == this.initParam!!.type -> // 从最高时期的分数牌中选择
                player.scores.maxLevelCardDeck
            "MIN_LEVEL" == this.initParam.type -> // 从最低时期的分数牌中选择
                player.scores.minLevelCardDeck
            else -> // 否则取得指定等级的牌堆
                player.scores.getCardDeck(this.initParam.level)
        }
        deck?.takeUnless(InnoCardDeck::empty)?.let {
            if (this.initParam.num > 0) {
                // 抽出指定数量的牌
                // 洗一下该牌堆
                it.reshuffle()
                val num = min(this.initParam.num, it.size)
                val cards = it.cards.subList(0, num)
                // 取得分数牌对象,放在返回参数中
                this.resultParam.addCards(cards)
            } else {
                // 抽出所有牌
                this.resultParam.addCards(it.cards)
            }
        }
    }

}
