package com.f14.innovation.listener

import com.f14.bg.consts.ConditionResult
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoActiveType
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 选择牌的监听器

 * @author F14eagle
 */
abstract class InnoChooseCardListener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoInterruptListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    protected var cards: List<InnoCard>? = null

    /**
     * 选择牌完成后触发的方法
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun afterProcessChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        // 处理完成后,需要检查是否存在后继的方法需要处理
        // 该方法存放在abilityGroup的conditionAbilities属性中,key=TRUE
        if (this.abilityGroup != null) {
            // 先检查THEN的方法,该方法中需要传入resultParam参数
            abilityGroup.getConditionAbilityGroup(ConditionResult.THEN)?.let {
                // 取得AbilityGroup就执行
                // 将选择的牌作为参数传入
                this.resultParam.nextAbilityGroups[ConditionResult.THEN] = this.targetPlayer
                this.cards = cards
            }
            // 然后检查TRUE的方法,该方法中不需要resultParam参数
            abilityGroup.getConditionAbilityGroup(ConditionResult.TRUE)?.let {
                // 取得AbilityGroup就执行
                this.resultParam.nextAbilityGroups[ConditionResult.TRUE] = player
            }
            // 如果选择的卡牌数量等于最多需要选择的卡牌数量,则检查MAX方法
            if (this.initParam!!.maxNum in 1..cards.size) {
                abilityGroup.getConditionAbilityGroup(ConditionResult.MAX)?.let {
                    // 取得AbilityGroup就执行
                    this.resultParam.nextAbilityGroups[ConditionResult.MAX] = player
                }
            }
            // 然后检查ANYWAY的方法,该方法中不需要resultParam参数
            abilityGroup.getConditionAbilityGroup(ConditionResult.ANYWAY)?.let {
                // 取得AbilityGroup就执行
                this.resultParam.nextAbilityGroups[ConditionResult.ANYWAY] = player
            }
        }
    }

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        val initParam = this.initParam
        if (initParam != null) {
            if (this.abilityGroup?.activeType == InnoActiveType.DEMAND) {
                // 如果是DEMAND技能,则只要存在可以选的牌,就需要回应
                if (this.getAvailableCardNum(player) == 0) return false
            } else {
                // 如果需要选择牌,并且牌数量不足时,则不需要回应
                if (initParam.num > 0 && this.getAvailableCardNum(player) < initParam.num) return false
            }
        }
        return super.beforeListeningCheck(player)
    }

    /**
     * 处理选择牌之前触发的方法
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun beforeProcessChooseCard(player: InnoPlayer, cards: List<InnoCard>) = Unit

    /**
     * 判断是否可以选择该卡牌
     * @param card
     * @return
     */
    protected open fun canChooseCard(player: InnoPlayer, card: InnoCard) =// 检查是否可以选择该卡牌
            this.ability?.test(card) != false

    /**
     * 判断是否可以选择该卡牌
     * @return
     */
    protected open fun cannotChooseCard(player: InnoPlayer, cards: List<InnoCard>) = cards.none { this.canChooseCard(player, it) }

    override fun createInterruptParam() = super.createInterruptParam().also { it["cards"] = this.cards }

    /**
     * 取得所有可供选择牌的数量
     * @param player
     * @return
     */
    protected abstract fun getAvailableCardNum(player: InnoPlayer): Int
}
