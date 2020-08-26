package com.f14.TTA;

import com.f14.TTA.component.TTAProperty;
import com.f14.TTA.component.card.*;
import com.f14.TTA.consts.ActionType;
import com.f14.TTA.consts.CardType;
import com.f14.TTA.consts.CivilAbilityType;
import com.f14.TTA.consts.CivilizationProperty;
import com.f14.bg.report.BgCacheReport;

import java.util.List;
import java.util.Map;

/**
 * TTA的游戏报告
 *
 * @author F14eagle
 */
public class TTAReport extends BgCacheReport<TTAPlayer> {

    public TTAReport(TTA bg) {
        super(bg);
    }

    /**
     * 玩家竞得地主
     *
     * @param player
     * @param totalValue
     */
    public void bidTichu(TTAPlayer player, int totalValue) {
        this.action(player, "以 " + totalValue + " 分为代价成为地主", true);
    }

    /**
     * 时代更迭
     *
     * @param currentAge
     */
    public void newAge(int currentAge) {
        this.info(TTACard.getAgeString(currentAge) + "时代 开始", true);
    }

    /**
     * 玩家选择是否接受条约
     *
     * @param player
     * @param card
     * @param accept
     */
    public void playerAcceptPact(TTAPlayer player, TTACard card, boolean accept) {
        StringBuilder sb = new StringBuilder();
        if (accept) {
            sb.append("接受");
        } else {
            sb.append("拒绝");
        }
        sb.append("了条约").append(card.getReportString()).append("的签订");
        this.action(player, sb.toString());
    }

    /**
     * 玩家使用卡牌能力
     *
     * @param player
     * @param card
     */
    public void playerActiveCard(TTAPlayer player, TTACard card) {
        StringBuilder sb = new StringBuilder();
        sb.append("使用了").append(card.getReportString()).append("的能力");
        if (card.activeAbility != null) {
            if (card.activeAbility.useActionPoint) {
                sb.append(",消耗了").append(card.activeAbility.actionCost).append("个")
                        .append(card.activeAbility.actionType.getChinese()).append("行动点");
            }
        }
        this.action(player, sb.toString());
    }

    /**
     * 玩家使用卡牌能力
     *
     * @param player
     * @param target
     * @param card
     * @param actionType
     * @param actionCost
     */
    public void playerActiveCard(TTAPlayer player, TTAPlayer target, TTACard card, ActionType actionType,
                                 int actionCost) {
        StringBuilder sb = new StringBuilder();
        sb.append("对").append(target.getReportString()).append("使用了").append(card.getReportString());
        if (actionType != null) {
            sb.append(",消耗了").append(actionCost).append("个").append(actionType.getChinese()).append("行动点");
        }
        this.action(player, sb.toString());
    }

    /**
     * 玩家使用卡牌能力(缓存)
     *
     * @param player
     * @param abilityType
     */
    public void playerActiveCardCache(TTAPlayer player, CivilAbilityType abilityType) {
        TTACard card = player.getAbilityManager().getAbilityCard(abilityType);
        this.playerActiveCardCache(player, card);
    }

    /**
     * 玩家使用卡牌能力(缓存)
     *
     * @param player
     * @param card
     */
    public void playerActiveCardCache(TTAPlayer player, TTACard card) {
        StringBuilder sb = new StringBuilder();
        sb.append("使用了").append(card.getReportString()).append("的能力");
        if (card.activeAbility != null) {
            if (card.activeAbility.useActionPoint) {
                sb.append(",消耗了").append(card.activeAbility.actionCost).append("个")
                        .append(card.activeAbility.actionType.getChinese()).append("行动点");
            }
        }
        this.addAction(player, sb.toString());
    }

    /**
     * 玩家调整蓝色标志物
     *
     * @param player
     * @param num
     */
    public void playerAddBlueToken(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "得到" + num + "个蓝色标志物");
        } else if (num < 0) {
            this.addAction(player, "失去" + -num + "个蓝色标志物");
        }
    }

    /**
     * 玩家得到打出的牌
     *
     * @param player
     * @param card
     */
    public void playerAddCard(TTAPlayer player, TTACard card) {
        this.action(player, "得到" + card.getReportString());
    }

    /**
     * 玩家得到打出的牌
     *
     * @param player
     * @param card
     */
    public void playerAddCardCache(TTAPlayer player, TTACard card) {
        this.addAction(player, "得到" + card.getReportString());
    }

    /**
     * 玩家调整内政行动点
     *
     * @param player
     * @param num
     */
    public void playerAddCivilAction(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "得到" + num + "个内政行动点");
        } else if (num < 0) {
            this.addAction(player, "失去" + -num + "个内政行动点");
        }
    }

    /**
     * 玩家调整文明点数
     *
     * @param player
     * @param num
     */
    public void playerAddCulturePoint(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "得到" + num + "个文明点数");
        } else if (num < 0) {
            this.addAction(player, "失去" + -num + "个文明点数");
        }
    }

    /**
     * 玩家添加事件卡
     *
     * @param player
     * @param addedCard
     * @param eventCard
     */
    public void playerAddEvent(TTAPlayer player, TTACard addedCard, EventCard eventCard) {
        String msgPublic = "将一张" + TTACard.getAgeString(addedCard.level) + "时代的牌放入未来事件牌堆,然后从当前事件牌堆中翻开了"
                + eventCard.getReportString();
        String msgPrivate = "将" + addedCard.getReportString() + "放入未来事件牌堆,然后从当前事件牌堆中翻开了" + eventCard.getReportString();
        this.action(player, msgPublic, msgPrivate);
    }

    /**
     * 玩家调整食物
     *
     * @param player
     * @param num
     */
    public void playerAddFood(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "得到" + num + "个食物");
        } else if (num < 0) {
            this.addAction(player, "失去" + -num + "个食物");
        }
    }

    /**
     * 玩家调整军事行动点
     *
     * @param player
     * @param num
     */
    public void playerAddMilitaryAction(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "得到" + num + "个军事行动点");
        } else if (num < 0) {
            this.addAction(player, "失去" + -num + "个军事行动点");
        }
    }

    /**
     * 玩家按照property中的资源/食物/文明点数/科技点数调整对应的数值
     *
     * @param player
     * @param property
     */
    public void playerAddPoint(TTAPlayer player, TTAProperty property) {
        int p = property.getProperty(CivilizationProperty.SCIENCE);
        if (p != 0) {
            this.playerAddSciencePoint(player, p);
        }
        p = property.getProperty(CivilizationProperty.CULTURE);
        if (p != 0) {
            this.playerAddCulturePoint(player, p);
        }
        p = property.getProperty(CivilizationProperty.FOOD);
        if (p != 0) {
            this.playerAddFood(player, p);
        }
        p = property.getProperty(CivilizationProperty.RESOURCE);
        if (p != 0) {
            this.playerAddResource(player, p);
        }
    }

    /**
     * 玩家调整资源
     *
     * @param player
     * @param num
     */
    public void playerAddResource(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "得到" + num + "个资源");
        } else if (num < 0) {
            this.addAction(player, "失去" + -num + "个资源");
        }
    }

    /**
     * 玩家调整科技点数
     *
     * @param player
     * @param num
     */
    public void playerAddSciencePoint(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "得到" + num + "个科技点数");
        } else if (num < 0) {
            this.addAction(player, "失去" + -num + "个科技点数");
        }
    }

    /**
     * 玩家调整临时资源
     */
    public void playerAddTemporaryResource(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "得到" + num + "个临时资源");
        } else if (num < 0) {
            this.addAction(player, "花费" + -num + "个临时资源");
        }
    }

    /**
     * 玩家调整临时科技点数
     *
     * @param player
     * @param num
     */
    public void playerAddTemporaryScience(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "得到" + num + "个临时科技点数");
        } else if (num < 0) {
            this.addAction(player, "花费" + -num + "个临时科技点数");
        }
    }

    /**
     * 玩家调整黄色标志物
     *
     * @param player
     * @param num
     */
    public void playerAddYellowToken(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "得到" + num + "个黄色标志物");
        } else if (num < 0) {
            this.addAction(player, "失去" + -num + "个黄色标志物");
        }
    }

    /**
     * 玩家埋牌到目标牌下
     *
     * @param player
     * @param card
     * @param destCard 目标牌
     */
    public void playerAttachCard(TTAPlayer player, TTACard card, TTACard destCard) {
        this.addAction(player, "把" + card.getReportString() + "叠放在" + destCard.getReportString() + "下方");
    }

    /**
     * 玩家殖民出价
     *
     * @param player
     * @param totalValue
     */
    public void playerBid(TTAPlayer player, int totalValue) {
        StringBuilder sb = new StringBuilder();
        if (totalValue > 0) {
            sb.append("出价").append(totalValue);
        } else {
            sb.append("放弃");
        }
        this.action(player, sb.toString());
    }

    /**
     * 玩家打出奖励牌
     */
    public void playerBonusCardPlayed(TTAPlayer player, List<TTACard> cards) {
        if (cards.size() > 0) {
            StringBuilder sbPublic = new StringBuilder("使用了");
            StringBuilder sbPrivate = new StringBuilder("使用了");
            int num = 0;
            for (TTACard c : cards) {
                if (c.cardType != CardType.DEFENSE_BONUS) {
                    num += 1;
                } else {
                    sbPublic.append(c.getReportString());
                }
                sbPrivate.append(c.getReportString());
            }
            if (num > 0) {
                if (num < cards.size()) {
                    sbPublic.append("和");
                }
                sbPublic.append(num).append("张军事牌");
            }
            this.addAction(player, sbPublic.toString(), sbPrivate.toString());
        }
    }

    /**
     * 玩家废除条约
     *
     * @param player
     * @param card
     */
    public void playerBreakPact(TTAPlayer player, TTACard card) {
        this.action(player, "废除了条约" + card.getReportString());
    }

    /**
     * 玩家建造建筑/部队
     *
     * @param player
     * @param actionType
     * @param actionCost
     * @param card
     * @param num
     */
    public void playerBuild(TTAPlayer player, ActionType actionType, int actionCost, TTACard card, int num) {
        StringBuilder sb = new StringBuilder();
        if (actionCost != 0) {
            sb.append("消耗了").append(actionCost).append("个").append(actionType.getChinese()).append("行动点");
        }
        sb.append("建造了").append(num).append("个").append(card.getReportString());
        this.action(player, sb.toString());
    }

    /**
     * 玩家建造建筑/部队
     *
     * @param player
     * @param card
     * @param num
     */
    public void playerBuild(TTAPlayer player, TTACard card, int num) {
        this.action(player, "建造了" + num + "个" + card.getReportString());
    }

    /**
     * 玩家建造建筑/部队(记录在缓存)
     *
     * @param player
     * @param card
     * @param num
     */
    public void playerBuildCache(TTAPlayer player, TTACard card, int num) {
        this.insertAction(player, "建造了" + num + "个" + card.getReportString());
    }

    /**
     * 玩家建造奇迹
     *
     * @param player
     * @param actionType
     * @param actionCost
     * @param card
     * @param buildStep
     */
    public void playerBuildWonder(TTAPlayer player, ActionType actionType, int actionCost, WonderCard card,
                                  int buildStep) {
        StringBuilder sb = new StringBuilder();
        if (actionCost != 0) {
            sb.append("消耗了").append(actionCost).append("个").append(actionType.getChinese()).append("行动点");
        }
        sb.append("建造了").append(card.getReportString()).append("的").append(buildStep).append("个步骤");
        if (card.isComplete()) {
            this.addAction(player, "完成了" + card.getReportString() + "的建造!");
        }
        this.action(player, sb.toString());
    }

    /**
     * 玩家建造奇迹(记录在缓存)
     *
     * @param player
     * @param card
     * @param buildStep
     */
    public void playerBuildWonderCache(TTAPlayer player, WonderCard card, int buildStep) {
        this.insertAction(player, "建造了" + card.getReportString() + "的" + buildStep + "个步骤");
        if (card.isComplete()) {
            this.addAction(player, "完成了" + card.getReportString() + "的建造!");
        }
    }

    /**
     * 玩家暴动提示
     *
     * @param player
     */
    public void playerCannotDrawWarning(TTAPlayer player) {
        this.action(player, "发生暴动,不能摸军事牌!");
    }

    /**
     * 玩家改变政府
     *
     * @param player
     * @param revolution
     * @param card
     */
    public void playerChangeGoverment(TTAPlayer player, boolean revolution, GovermentCard card) {
        StringBuilder sb = new StringBuilder();
        if (revolution) {
            sb.append("使用革命的方式,");
        } else {
            sb.append("使用和平演变的方式,");
        }
        sb.append("将政府更换成了").append(card.getReportString());
        this.action(player, sb.toString());
    }

    /**
     * 玩家改变政府
     *
     * @param player
     * @param revolution
     * @param card
     */
    public void playerChangeGovermentCache(TTAPlayer player, boolean revolution, GovermentCard card) {
        StringBuilder sb = new StringBuilder();
        if (revolution) {
            sb.append("使用革命的方式,");
        } else {
            sb.append("使用和平演变的方式,");
        }
        sb.append("将政府更换成了").append(card.getReportString());
        this.insertAction(player, sb.toString());
    }

    /**
     * 玩家选择条约方
     *
     * @param player
     * @param card
     * @param pactSide
     */
    public void playerChoosePactSide(TTAPlayer player, TTACard card, String pactSide) {
        this.action(player, "选择成为条约" + card.getReportString() + "的 " + pactSide + " 方");
    }

    /**
     * 学习阵型
     *
     * @param player
     * @param actionType
     * @param actionCost
     * @param card
     */
    public void playerCopyTatics(TTAPlayer player, ActionType actionType, int actionCost, TTACard card) {
        StringBuilder sb = new StringBuilder();
        if (actionCost != 0) {
            sb.append("消耗了").append(actionCost).append("个").append(actionType.getChinese()).append("行动点");
        }
        sb.append("学习了").append(card.getReportString());
        this.action(player, sb.toString());
    }

    /**
     * 玩家消耗行动点数
     *
     * @param player
     * @param actionType
     * @param actionCost
     * @param revolution
     */
    public void playerCostAction(TTAPlayer player, ActionType actionType, int actionCost, boolean revolution) {
        StringBuilder sb = new StringBuilder();
        if (revolution) {
            sb.append("消耗了所有的").append(actionType.getChinese()).append("行动点");
        } else {
            if (actionCost != 0) {
                sb.append("消耗了").append(actionCost).append("个").append(actionType.getChinese()).append("行动点");
            } else {
                return;
            }
        }
        this.addAction(player, sb.toString());
    }

    /**
     * 玩家按照property中的资源/食物/文明点数/科技点数支付对应的数值
     *
     * @param player
     * @param text
     */
    public void playerCostPoint(TTAPlayer player, TTAProperty cost, String text) {
        int p = cost.getProperty(CivilizationProperty.SCIENCE);
        if (p > 0) {
            this.addAction(player, text + p + "个科技点数");
        }
        p = cost.getProperty(CivilizationProperty.CULTURE);
        if (p > 0) {
            this.addAction(player, text + p + "个文化分数");
        }
        p = cost.getProperty(CivilizationProperty.FOOD);
        if (p > 0) {
            this.addAction(player, text + p + "个食物");
        }
        p = cost.getProperty(CivilizationProperty.RESOURCE);
        if (p > 0) {
            this.addAction(player, text + p + "个资源");
        }
    }

    /**
     * 玩家失去人口
     *
     * @param player
     * @param num
     */
    public void playerDecreasePopulation(TTAPlayer player, int num) {
        if (num > 0) {
            this.action(player, "失去了" + num + "个人口");
        }
    }

    /**
     * 玩家失去人口(缓存输出)
     *
     * @param player
     * @param num
     */
    public void playerDecreasePopulationCache(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "失去了" + num + "个人口");
        }
    }

    /**
     * 玩家摧毁建筑/部队
     *
     * @param player
     * @param actionType
     * @param actionCost
     * @param card
     * @param num
     */
    public void playerDestory(TTAPlayer player, ActionType actionType, int actionCost, TTACard card, int num) {
        StringBuilder sb = new StringBuilder();
        if (actionCost != 0) {
            sb.append("消耗了").append(actionCost).append("个").append(actionType.getChinese()).append("行动点");
        }
        sb.append("摧毁了").append(num).append("个").append(card.getReportString());
        this.action(player, sb.toString());
    }

    /**
     * 玩家摧毁建筑/部队
     *
     * @param player
     * @param detail
     */
    public void playerDestroy(TTAPlayer player, Map<TechCard, Integer> detail) {
        if (detail.size() > 0) {
            StringBuilder sb = new StringBuilder("摧毁了");
            boolean flag = false;
            for (TechCard c : detail.keySet()) {
                if (detail.get(c) > 0) {
                    if (!flag) {
                        sb.append(",");
                        flag = true;
                    }
                    sb.append(detail.get(c)).append("个").append(c.getReportString());
                }
            }
            this.addAction(player, sb.toString());
        }
    }

    /**
     * 玩家摧毁建筑/部队
     *
     * @param player
     * @param card
     * @param num
     */
    public void playerDestroy(TTAPlayer player, TechCard card, int num) {
        this.addAction(player, "摧毁了" + num + "个" + card.getReportString());
    }

    /**
     * 玩家弃军事手牌
     *
     * @param player
     */
    public void playerDiscardMilitaryHand(TTAPlayer player, List<TTACard> cards) {
        String strPublic = "弃了" + cards.size() + "张军事牌";
        StringBuilder sb = new StringBuilder("弃了");
        for (TTACard c : cards) {
            sb.append(c.getReportString());
        }
        String strPrivate = sb.toString();
        this.action(player, strPublic, strPrivate);
    }

    /**
     * 玩家摸军事牌
     *
     * @param player
     */
    public void playerDrawMilitary(TTAPlayer player, List<TTACard> cards) {
        String strPublic = "摸了" + cards.size() + "张军事牌";
        StringBuilder sb = new StringBuilder("摸了");
        for (TTACard c : cards) {
            sb.append(c.getReportString());
        }
        String strPrivate = sb.toString();
        this.addAction(player, strPublic, strPrivate);
    }

    /**
     * 玩家结束行动阶段
     *
     * @param player
     */
    public void playerEndAction(TTAPlayer player) {
        this.action(player, "结束了行动阶段");
    }

    /**
     * 玩家结束政治行动阶段
     *
     * @param player
     */
    public void playerEndPoliticalPhase(TTAPlayer player) {
        this.action(player, "结束了政治行动阶段");
    }

    /**
     * 处理临时资源和临时科技点数
     *
     * @param player
     * @param property
     */
    public void playerExecuteTemporaryResource(TTAPlayer player, TTAProperty property) {
        int num = property.getProperty(CivilizationProperty.SCIENCE);
        if (num > 0) {
            this.playerAddTemporaryScience(player, -num);
        }
        num = property.getProperty(CivilizationProperty.RESOURCE);
        if (num > 0) {
            this.playerAddTemporaryResource(player, -num);
        }
    }

    /**
     * 和平城交换事件
     *
     * @param player
     * @param card
     * @param exCard
     */
    public void playerExtrangeEvent(TTAPlayer player, TTACard card, TTACard exCard) {
        String msgPublic = "将一张" + TTACard.getAgeString(card.level) + "时代的牌交换了一张" + TTACard.getAgeString(exCard.level)
                + "时代的牌";
        String msgPrivate = "将" + card.getReportString() + "交换了" + exCard.getReportString();
        this.addAction(player, msgPublic, msgPrivate);
    }

    /**
     * 玩家得到殖民地
     *
     * @param player
     * @param territory
     * @param totalValue
     */
    public void playerGetColony(TTAPlayer player, EventCard territory, int totalValue) {
        if (totalValue != 0) {
            this.action(player, "以总数" + totalValue + "的殖民点数夺得了" + territory.getReportString());
        } else {
            this.action(player, "夺得了" + territory.getReportString());
        }
    }

    /**
     * 玩家扩张人口
     *
     * @param player
     * @param actionCost
     * @param num
     */
    public void playerIncreasePopulation(TTAPlayer player, int actionCost, int num) {
        if (num > 0) {
            StringBuilder sb = new StringBuilder();
            if (actionCost != 0) {
                sb.append("消耗了").append(actionCost).append("个内政行动点");
            }
            sb.append("扩张了").append(num).append("个人口");
            this.action(player, sb.toString());
        }
    }

    /**
     * 玩家扩张人口(缓存输出)
     *
     * @param player
     * @param num
     */
    public void playerIncreasePopulationCache(TTAPlayer player, int num) {
        if (num > 0) {
            this.insertAction(player, "扩张了" + num + "个人口");
        }
    }

    /**
     * 玩家出牌
     *
     * @param player
     * @param actionType
     * @param actionCost
     * @param card
     */
    public void playerPlayCard(TTAPlayer player, ActionType actionType, int actionCost, TTACard card) {
        StringBuilder sb = new StringBuilder();
        if (actionCost != 0) {
            sb.append("消耗了").append(actionCost).append("个").append(actionType.getChinese()).append("行动点");
        }
        sb.append("打出了").append(card.getReportString());
        this.action(player, sb.toString());
    }

    /**
     * 玩家出牌
     *
     * @param player
     * @param card
     */
    public void playerPlayCardCache(TTAPlayer player, TTACard card) {
        this.insertAction(player, "打出了" + card.getReportString());
    }

    /**
     * 玩家生产食物
     *
     * @param player
     * @param num
     */
    public void playerProduceFood(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "生产" + num + "个食物");
        }
    }

    /**
     * 玩家生产资源
     *
     * @param player
     * @param num
     */
    public void playerProduceResource(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "生产" + num + "个资源");
        }
    }

    /**
     * 玩家失去打出的牌
     *
     * @param player
     * @param card
     */
    public void playerRemoveCard(TTAPlayer player, TTACard card) {
        this.action(player, "失去了" + card.getReportString());
    }

    /**
     * 玩家失去打出的牌
     *
     * @param player
     * @param card
     */
    public void playerRemoveCardCache(TTAPlayer player, TTACard card) {
        this.addAction(player, "失去了" + card.getReportString());
    }

    /**
     * 玩家弃手牌
     *
     * @param player
     * @param cards
     */
    public void playerRemoveHand(TTAPlayer player, List<TTACard> cards) {
        if (cards.size() > 0) {
            StringBuilder sbPrivate = new StringBuilder("弃了");
            StringBuilder sbPublic = new StringBuilder("弃了");
            int num = 0;
            for (TTACard c : cards) {
                if (c.actionType == ActionType.MILITARY) {
                    num += 1;
                } else {
                    sbPublic.append(c.getReportString());
                }
                sbPrivate.append(c.getReportString());
            }
            if (num > 0) {
                if (num < cards.size()) {
                    sbPublic.append("和");
                }
                sbPublic.append(num).append("张军事牌");
            }
            this.action(player, sbPublic.toString(), sbPrivate.toString());
        }
    }

    /**
     * 玩家体面退出游戏
     */
    public void playerResign(TTAPlayer player) {
        this.action(player, "选择了体面退出游戏!", true);
    }

    /**
     * 玩家生产回合
     *
     * @param player
     */
    public void playerRoundScore(TTAPlayer player) {
        this.action(player, "进行了生产阶段");
    }

    /**
     * 玩家牺牲部队
     *
     * @param units
     */
    public void playerSacrifidUnit(TTAPlayer player, Map<TechCard, Integer> units) {
        StringBuilder sb = new StringBuilder("牺牲了");
        boolean flag = false;
        for (TechCard c : units.keySet()) {
            if (units.get(c) > 0) {
                if (!flag) {
                    sb.append(",");
                    flag = true;
                }
                sb.append(units.get(c)).append("个").append(c.getReportString());
            }
        }
        this.addAction(player, sb.toString());
    }

    /**
     * 玩家当前文明点数
     *
     * @param player
     * @param num
     */
    public void playerScoreCulturePoint(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "生产" + num + "个文明点数");
        }
    }

    /**
     * 玩家当前科技点数
     *
     * @param player
     * @param num
     */
    public void playerScoreSciencePoint(TTAPlayer player, int num) {
        if (num > 0) {
            this.addAction(player, "生产" + num + "个科技点数");
        }
    }

    /**
     * 玩家从摸牌区得到卡牌
     *
     * @param player
     * @param actionCost
     * @param card
     */
    public void playerTakeCard(TTAPlayer player, int actionCost, int idx, TTACard card) {
        StringBuilder sb = new StringBuilder();
        if (actionCost != 0) {
            sb.append("消耗了").append(actionCost).append("个内政行动点");
        }
        sb.append("拿取了").append(card.getReportString(idx + 1));
        this.action(player, sb.toString());
    }

    /**
     * 玩家失去卡牌
     *
     * @param player
     * @param card
     */
    public void playerTakeCardCache(TTAPlayer player, TTACard card, int idx) {
        this.addAction(player, "从巨轮上移除了" + card.getReportString(idx + 1));
    }

    /**
     * 玩家升级建筑/部队(记录在缓存)
     *
     * @param player
     * @param actionType
     * @param actionCost
     * @param fromCard
     * @param toCard
     * @param num
     */
    public void playerUpgrade(TTAPlayer player, ActionType actionType, int actionCost, TTACard fromCard, TTACard toCard,
                              int num) {
        StringBuilder sb = new StringBuilder();
        if (actionCost != 0) {
            sb.append("消耗了").append(actionCost).append("个").append(actionType.getChinese()).append("行动点");
        }
        sb.append("将").append(num).append("个").append(fromCard.getReportString()).append("升级为")
                .append(toCard.getReportString());
        this.action(player, sb.toString());
    }

    /**
     * 玩家升级建筑/部队
     *
     * @param player
     * @param fromCard
     * @param toCard
     * @param num
     */
    public void playerUpgradeCache(TTAPlayer player, TTACard fromCard, TTACard toCard, int num) {
        String sb = "将" + num + "个" + fromCard.getReportString() + "升级为" +
                toCard.getReportString();
        this.insertAction(player, sb);
    }

    /**
     * 玩家暴动提示
     *
     * @param player
     */
    public void playerUprisingWarning(TTAPlayer player) {
        this.action(player, "发生暴动,跳过生产阶段!");
    }

    /**
     * 玩家的殖民奖励
     *
     * @param player
     * @param colonyBonus
     */
    public void playerUseColoBonus(TTAPlayer player, int colonyBonus) {
        this.addAction(player, "使用了" + colonyBonus + "点殖民奖励");
    }

    /**
     * 打印战争结果
     *
     * @param player
     * @param target
     * @param card
     * @param playerTotal
     * @param targetTotal
     */
    public void printWarResult(TTAPlayer player, TTAPlayer target, TTACard card, int playerTotal, int targetTotal,
                               int result) {
        String sb = "在" + card.getReportString() + "中以总军力 " + playerTotal + ":" +
                targetTotal +
                (result > 0 ? "战胜了" : result < 0 ? "战败于" : "战平") +
                target.getReportString();
        this.action(player, sb);
    }

    /**
     * 公共阵型
     *
     * @param player
     */
    public void publicTactics(TTAPlayer player, TTACard card) {
        this.action(player, "的" + card.getReportString() + "已可被学习");
    }

    /**
     * 提示卡牌列上的卡牌
     *
     * @param cards
     */
    public void refreshCardRow(List<TTACard> cards) {
        StringBuilder sb = new StringBuilder();
        if (cards.size() > 0) {
            sb.append("巨轮上有卡牌");
            for (int idx = 0; idx < cards.size(); ++idx) {
                if (idx == 5 || idx == 9) {
                    sb.append("||");
                }
                if (cards.get(idx) != null) {
                    sb.append(cards.get(idx).getReportString(idx + 1));
                }
            }
        } else {
            sb.append("巨轮上没有卡牌");
        }
        this.info(sb.toString());
    }

}
