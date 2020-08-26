package com.f14.TTA;

import com.f14.TTA.component.ability.CivilCardAbility;
import com.f14.TTA.component.card.CivilCard;
import com.f14.TTA.component.card.EventCard;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.card.WonderCard;
import com.f14.TTA.consts.CardType;
import com.f14.TTA.consts.CivilAbilityType;
import com.f14.TTA.consts.CivilizationProperty;
import com.f14.bg.GameEndPhase;
import com.f14.bg.GameMode;
import com.f14.bg.VPCounter;
import com.f14.bg.VPResult;
import com.f14.bg.utils.BgUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * TTA的结束阶段
 *
 * @author F14eagle
 */
public class TTAEndPhase extends GameEndPhase {


    @Override
    protected VPResult createVPResult(GameMode gameMode) {
        TTAGameMode gm = (TTAGameMode) gameMode;
        gm.getGame().setCurrentPlayer(gm.getGame().getStartPlayer());
        VPResult result = new VPResult(gm.getGame());
        // 记录组队的得分
        Map<Integer, Integer> teamScore = new HashMap<>();
        int resignedPlayerCount = gm.getResignedPlayerNumber();
        // 终盘结算的事件牌
        Collection<EventCard> events;
        if (gm.getGame().getConfig().bonusCardFlag) {
            // 进阶模式下,需要计算奖励牌堆中的得分事件
            events = BgUtils.cloneList(gm.bonusCards);
        } else {
            // 完整模式下,需要检查所有剩余的事件牌堆,计算其中的得分事件
            events = new ArrayList<>();
            for (TTACard c : gm.cardBoard.getCurrentEventDeck().getCards()) {
                if (c.cardType == CardType.EVENT && c.level == 3) {
                    events.add((EventCard) c);
                }
            }
            for (TTACard c : gm.cardBoard.getFutureEventDeck().getCards()) {
                if (c.cardType == CardType.EVENT && c.level == 3) {
                    events.add((EventCard) c);
                }
            }
        }
        // 计算各自的得分
        for (TTAPlayer player : gm.getGame().getPlayers()) {
            log.debug("玩家 [" + player.user.getName() + "] 的分数:");
            VPCounter vpc = new VPCounter(player);
            result.addVPCounter(vpc);
            if (player.resigned) {
                vpc.addDisplayVp("体面退出游戏", (gm.getResignedPlayerPosition(player) - resignedPlayerCount) * 100);
            } else {
                vpc.addDisplayVp("文明点数", player.getCulturePoint());
                if (gm.getGame().getRealPlayerNumber() > (gm.getGame().isTeamMatch() ? 2 : 1)) {
                    // 圣家堂得分
                    WonderCard wc = player.getUncompleteWonder();
                    if (wc != null && wc.activeAbility != null) {
                        switch (wc.activeAbility.abilityType) {
                            case PA_SAGRADA_FAMILIA:
                                int step = wc.currentStep;
                                vpc.addDisplayVp(wc.name,
                                        step * wc.activeAbility.property.getProperty(CivilizationProperty.CULTURE));
                                break;
                            default:
                                break;
                        }
                    }
                    // 新版盖茨得分
                    CivilCard lc = player.getLeader();
                    if (lc != null && lc.activeAbility != null) {
                        switch (lc.activeAbility.abilityType) {
                            case PA_NEW_GATES_ABILITY:
                                CivilCardAbility ca = super.getAbilityManager()
                                        .getAbility(CivilAbilityType.PA_NEW_GATES_ABILITY);
                                vpc.addDisplayVp(lc.name, ca.getAvailableProperty(player, CivilizationProperty.CULTURE));
                                break;
                            default:
                                break;
                        }
                    }
                    // 始皇陵得分
                    if (super.getAbilityManager().hasAbilitiy(CivilAbilityType.PA_SHIHUANG_TOMB)) {
                        WonderCard c = (WonderCard) player.getAbilityManager()
                                .getAbilityCard(CivilAbilityType.PA_SHIHUANG_TOMB);
                        switch (c.activeAbility.abilityType) {
                            case PA_SHIHUANG_TOMB:
                                vpc.addDisplayVp(c.name,
                                        c.getBlues() * c.activeAbility.property.getProperty(CivilizationProperty.CULTURE));
                                break;
                            default:
                                break;
                        }
                    }
                    // 计算得分
                    for (EventCard ec : events) {
                        vpc.addDisplayVp(ec.name, player.getScoreCulturePoint(ec.getScoreAbilities()));
                    }
                }
            }
            log.debug("总计 : " + vpc.getTotalDisplayVP());
            // 记录队伍的得分
            Integer score = teamScore.get(player.getTeam());
            if (score == null) {
                score = vpc.getTotalDisplayVP();
                // } else if (gm.getBoardGame().isTichuMode()) {
                // score = Math.max(score, vpc.getTotalDisplayVP());
            } else {
                score += vpc.getTotalDisplayVP();
            }
            teamScore.put(player.getTeam(), score);
        }
        // 设置玩家的总得分
        for (VPCounter vpc : result.vpCounters) {
            int total = teamScore.get(vpc.player.getTeam());
            int self = vpc.getTotalDisplayVP();
            if (gm.getGame().isTichuMode()) {
                if (vpc.player.getTeam() == 0) {
                    vpc.addVp("总分", self);
                    vpc.addVp("地主让分", gm.tichuBid);
                } else if (self > total - self) {
                    vpc.addVp("总分", self);
                    vpc.addDisplayVp("队友得分", total - self);
                } else {
                    vpc.addDisplayVp("总分", self);
                    vpc.addVp("队友得分", total - self);
                }
            } else {
                vpc.addVp("总分", self);
                if (gameMode.getGame().isTeamMatch()) {
                    vpc.addVp("队友得分", total - self);
                }
            }
        }
        return result;
    }

    // public static void main(String[] args) {
    // Integer s = 0;
    // s += 1;
    // Map<Integer, Integer> teamScore = new HashMap<Integer, Integer>();
    // teamScore.put(1, s);
    // System.out.println(teamScore.get(1));
    // }

}
