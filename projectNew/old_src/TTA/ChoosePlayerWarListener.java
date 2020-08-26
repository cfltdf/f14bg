package com.f14.TTA.listener;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.ability.CivilCardAbility;
import com.f14.TTA.component.card.AttackCard;
import com.f14.TTA.component.card.PactCard;
import com.f14.TTA.component.param.RoundParam;
import com.f14.TTA.consts.CardSubType;
import com.f14.TTA.consts.CivilAbilityType;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 侵略/战争时选择玩家的中断监听器
 *
 * @author F14eagle
 */
public class ChoosePlayerWarListener extends ChoosePlayerListener {
    protected RoundParam param;
    protected List<String> pactIds;
    protected int actionCost = 0;

    public ChoosePlayerWarListener(RoundParam param, AttackCard card) {
        super(param.player, card);
        this.param = param;
    }

    /**
     * 检查目标是否会受到该战败时效果的影响,
     *
     * @param target
     * @throws BoardGameException
     */
    protected void checkLoserEffect(TTAPlayer target) throws BoardGameException {
        switch (this.getUsedCard().loserEffect.eventType) {
            case LOSE_LEADER:
                CheckUtils.check(target.getLeader() == null, "目标玩家没有领袖!");
                break;
            case LOSE_UNCOMPLETE_WONDER:
                CheckUtils.check(target.getUncompleteWonder() == null, "目标玩家没有在建造的奇迹!");
                break;
            case LOSE_COLONY:
                CheckUtils.check(target.getPlayedCard(this.getUsedCard().loserEffect).isEmpty(), "目标玩家没有殖民地!");
                break;
            case CHOOSE_INFILTRATE:
                CheckUtils.check(target.getLeader() == null && target.getUncompleteWonder() == null, "目标玩家没有领袖或在建造的奇迹!");
                break;
            // 其他情况暂时偷懒一下,不做校验了
            default:
                break;
        }
    }

    @Override
    protected void choosePlayer(TTAGameMode gameMode, TTAPlayer player, TTAPlayer target) throws BoardGameException {
        // 检查目标玩家是否可以被选择
        List<CivilCardAbility> abs = super.getAbilityManager().getAbilitiesByType(CivilAbilityType.PA_CANNOT_BE_TARGET);
        for (CivilCardAbility a : abs) {
            CheckUtils.check(!a.test(usedCard), "你不能选择这个玩家作为目标!");
        }
        // 检查进攻方是否存在不能对盟军进攻的能力
        Map<CivilCardAbility, PactCard> abilities = super.getAbilityManager()
                .getPactAbilitiesWithRelation(CivilAbilityType.PA_CANNOT_ATTACK_ALIAN);
        for (CivilCardAbility a : abilities.keySet()) {
            PactCard pact = abilities.get(a);
            CheckUtils.check(pact.alian == target, "你不能进攻签订条约的盟友!");
        }
        // 不能对队友使用(并不
        /*
         * if (mode.getBoardGame().isTichuMode() &&
		 * mode.getBoardGame().isTeammates(player, target)) { throw new
		 * BoardGameException("你不能选择队友作为目标!"); }
		 */

        // 检查玩家是否能够对target使用该牌
        player.checkUseCard(this.usedCard);
        // 检查对目标使用时实际需要消耗的行动点数
        if (this.usedCard.actionCost != null) {
            actionCost = this.usedCard.actionCost.getActionCost(target);
            abs = super.getAbilityManager().getAbilitiesByType(CivilAbilityType.PA_ADJUST_MILITARY_COST);
            for (CivilCardAbility a : abs) {
                if (a.test(usedCard)) {
                    actionCost += a.amount;
                }
            }
            // 检查目标玩家是否拥有可以调整行动点消耗的能力
            abs = super.getAbilityManager().getAbilitiesByType(CivilAbilityType.PA_ADDITIONAL_MILITARY_COST);
            if (!abs.isEmpty()) {
                int addma = 0;
                for (CivilCardAbility a : abs) {
                    if (a.test(usedCard)) {
                        // amount为增加ma的倍数
                        addma += (actionCost * a.amount);
                    }
                }
                // 加上ma的调整值
                actionCost += addma;
            }
            player.checkActionPoint(this.usedCard.actionCost.actionType, actionCost);
        }
        // 检查目标是否会受到该战败时效果的影响,如果不会,则提示玩家不能使用
        this.checkLoserEffect(target);

        // 检查进攻方是否有进攻盟友后打破条约的能力
        pactIds = new ArrayList<>();
        for (PactCard c : super.getAbilityManager()
                .getPactAbilitiesWithRelation(CivilAbilityType.PA_END_WHEN_ATTACK_ALIAN).values()) {
            if (c.alian == target) {
                pactIds.add(c.id);
            }
        }

        // 检测新版军力,若目标不小于自身则不能发动侵略
        if (this.usedCard.cardSubType == CardSubType.AGGRESSION && gameMode.getGame().isVersion2()) {
            int mdiff = player.getAttackerMilitary(usedCard, target) - target.getDefenceMilitary();
            mdiff -= player.getPactMilitary(pactIds) - target.getPactMilitary(pactIds);
            CheckUtils.check(mdiff <= 0, "新版不能侵略军力不低于你的对手!");
        }

    }

    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = super.createInterruptParam();
        param.set("pactIds", pactIds);
        param.set("actionCost", actionCost);
        return param;
    }


    @Override
    public AttackCard getUsedCard() {
        return (AttackCard) super.getUsedCard();
    }
}
