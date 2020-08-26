package com.f14.innovation

import com.f14.bg.common.ListMap
import com.f14.innovation.achieve.InnoAchieveChecker
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.InnoCardDeck
import com.f14.innovation.component.InnoCardGroup
import com.f14.innovation.consts.InnoAchieveTrigType

/**
 * Innovation的成就管理器

 * @author F14eagle
 */
class InnoAchieveManager(private var gameMode: InnoGameMode) {
    val achieveCards = InnoCardGroup()
    val specialAchieveCards = InnoCardDeck()
    private val achieveCheckers = ListMap<InnoAchieveTrigType, InnoAchieveChecker>()
    private val cardCheckerRelates = ListMap<InnoCard, InnoAchieveChecker>()

    /**
     * 添加特殊成就牌
     * @param card
     */
    private fun addSpecialAchieve(card: InnoCard) {
        this.specialAchieveCards.addCard(card)
        card.achieveAbility?.let { ability ->
            val checker = InnoClassFactory.createAchieveChecker(ability, gameMode)
            ability.trigTypes.forEach { trigType ->
                this.getAchieveCheckers(trigType).add(checker)
                this.getRelatAchieveCheckers(card).add(checker)
            }
        }
    }

    /**
     * 执行特殊成就的检查
     * @param trigType
     * @param player
     */
    fun executeAchieveChecker(trigType: InnoAchieveTrigType, player: InnoPlayer) {
        this.getAchieveCheckers(trigType).filter { it.check(player) }
                // 如果检查通过,则玩家得到成就
                .mapNotNull(this::getRelateCard).forEach { gameMode.game.playerAddSpecialAchieveCard(player, it) }
    }

    /**
     * 取得成就检查器的list
     * @param trigType
     * @return
     */
    private fun getAchieveCheckers(trigType: InnoAchieveTrigType): MutableList<InnoAchieveChecker> {
        return this.achieveCheckers.getList(trigType)
    }

    /**
     * 取得成就牌对应的成就检查器list
     * @param card
     * @return
     */
    private fun getRelatAchieveCheckers(card: InnoCard): MutableList<InnoAchieveChecker> {
        return this.cardCheckerRelates.getList(card)
    }

    /**
     * 取得成就检查器对应的成就牌
     * @param checker
     * @return
     */
    private fun getRelateCard(checker: InnoAchieveChecker): InnoCard? {
        val cards = this.cardCheckerRelates.getKeyByValue(checker)
        return cards.firstOrNull()
    }

    /**
     * 装载所有成就牌
     * @param achieveCards
     */
    fun loadAchieveCards(achieveCards: Collection<InnoCard>) {
        for (card in achieveCards) {
            if (card.special) {
                // 特殊成就添加到特殊成就牌堆中
                this.addSpecialAchieve(card)
            } else {
                // 将普通的成就牌添加到成就牌堆中
                this.achieveCards.addCard(card)
            }
        }
    }

    /**
     * 移除特殊成就
     * @param card
     */
    fun removeSpecialAchieve(card: InnoCard) {
        this.specialAchieveCards.removeCard(card)
        this.getRelatAchieveCheckers(card).forEach(this.achieveCheckers::remove)
        this.cardCheckerRelates.removeKey(card)
    }

}
