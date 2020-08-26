package com.f14.TTA.listener

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.card.PactCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CardSubType
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.EventType
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class ChoosePlayerWarListener(param: RoundParam, private val card: AttackCard) : ChoosePlayerListener(param.gameMode, param.player, card) {
    private var pactIds: MutableList<String> = ArrayList()
    private var actionCost = 0

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 总是进行选择（防止手滑）
        return true
    }

    /**
     * 检查目标是否会受到该战败时效果的影响,
     * @param target
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun checkLoserEffect(target: TTAPlayer) = when (this.card.loserEffect.eventType) {
        EventType.LOSE_LEADER -> CheckUtils.check(target.leader == null, "目标玩家没有领袖!")
        EventType.LOSE_UNCOMPLETE_WONDER -> CheckUtils.check(target.uncompletedWonder == null, "目标玩家没有在建造的奇迹!")
        EventType.LOSE_COLONY -> CheckUtils.check(target.getPlayedCard(this.card.loserEffect).isEmpty(), "目标玩家没有殖民地!")
        EventType.CHOOSE_INFILTRATE -> CheckUtils.check(target.leader == null && target.uncompletedWonder == null, "目标玩家没有领袖或在建造的奇迹!")
        EventType.CHOOSE_CARD -> CheckUtils.check(target.civilHands.empty, "目标玩家没有内政手牌!")
    // 其他情况暂时偷懒一下,不做校验了
        else -> {
        }
    }

    @Throws(BoardGameException::class)
    override fun choosePlayer(player: TTAPlayer, target: TTAPlayer) {
        // 检查目标玩家是否可以被选择
        val abs = target.abilityManager.getAbilitiesByType(CivilAbilityType.PA_CANNOT_BE_TARGET)
        for (a in abs) {
            CheckUtils.check(!a.test(usedCard), "你不能选择这个玩家作为目标!")
        }
        // 检查进攻方是否存在不能对盟军进攻的能力
        val abilities = player.abilityManager.getPactAbilitiesWithRelation(CivilAbilityType.PA_CANNOT_ATTACK_ALIAN)
        for (pact in abilities.values) {
            CheckUtils.check(pact.alian === target, "你不能进攻签订条约的盟友!")
        }

        // 检查玩家是否能够对target使用该牌
        player.checkUseCard(this.card)
        // 检查对目标使用时实际需要消耗的行动点数
        actionCost = this.card.actionCost.getActionCost(target)
        actionCost += player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_ADJUST_MILITARY_COST).filter { it.test(card) }.sumBy(CivilCardAbility::amount)
        // 检查目标玩家是否拥有可以调整行动点消耗的能力
        actionCost += target.abilityManager.getAbilitiesByType(CivilAbilityType.PA_ADDITIONAL_MILITARY_COST).filter { it.test(card) }
                // amount为增加ma的倍数
                .sumBy { actionCost * it.amount + it.property.getProperty(CivilizationProperty.MILITARY_ACTION) }
        player.checkActionPoint(this.card.actionCost.actionType, actionCost)
        // 检查目标是否会受到该战败时效果的影响,如果不会,则提示玩家不能使用
        this.checkLoserEffect(target)

        // 检查进攻方是否有进攻盟友后打破条约的能力
        player.abilityManager.getPactAbilitiesWithRelation(CivilAbilityType.PA_END_WHEN_ATTACK_ALIAN).values.filter { it.alian == target }.mapTo(pactIds, PactCard::id)

        // 检测新版军力,若目标不小于自身则不能发动侵略
        if (this.usedCard.cardSubType == CardSubType.AGGRESSION && gameMode.game.isVersion2 && !gameMode.game.isTeamMatch) {
            var mdiff = player.getAttackerMilitary(usedCard, target) - target.defenceMilitary
            mdiff -= player.getPactMilitary(pactIds) - target.getPactMilitary(pactIds)
            CheckUtils.check(mdiff <= 0, "新版不能侵略军力不低于你的对手!")
        }

    }

    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["pactIds"] = pactIds
        param["actionCost"] = actionCost
        return param
    }
}
