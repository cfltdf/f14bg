package com.f14.TTA.component.param;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.TTAProperty;
import com.f14.TTA.component.ability.CardAbility;
import com.f14.TTA.component.ability.CivilCardAbility;
import com.f14.TTA.component.card.*;
import com.f14.TTA.consts.*;
import com.f14.TTA.executor.TTAGetAndPlayExecutor;
import com.f14.TTA.executor.active.TTAWillExecutor;
import com.f14.TTA.factory.TTAExecutorFactory;
import com.f14.TTA.listener.TTARequestSelectListener;
import com.f14.TTA.listener.TTARoundListener;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 玩家的回合参数
 *
 * @author F14eagle
 */
public class RoundParam {
    public TTAGameMode gameMode;
    public TTARoundListener listener;
    public TTAPlayer player;
    /**
     * 回合新拿到的卡牌列表
     */

    public List<TTACard> newcards = new ArrayList<>();
    /**
     * 判断是否是第一个内政行动
     */
    public boolean isFirstCivilAction = true;
    /**
     * 判断是否是第一个军事行动
     */
    public boolean isFirstMilitaryAction = true;
    /**
     * 当前步骤
     */

    public RoundStep currentStep = RoundStep.NONE;
    /**
     * 已进行的政治行动数量
     */
    public int politicalAction = 0;
    /**
     * 是否需要弃军事牌,默认为需要
     */
    public boolean needDiscardMilitary = true;
    /**
     * 建造时是否警告过
     */
    public boolean buildAlert = false;
    /**
     * 是否需要放弃政治阶段
     */
    public boolean passPoliticalPhase = false;

    public RoundParam(TTAGameMode gameMode, TTARoundListener listener, TTAPlayer player) {
        this.gameMode = gameMode;
        this.listener = listener;
        this.player = player;
    }

    /**
     * 添加临时资源的能力
     *
     * @param ability
     */
    public void addTemplateResource(CardAbility ability) {
        int num = ability.property.getProperty(CivilizationProperty.RESOURCE);
        gameMode.getReport().playerAddTemporaryResource(player, num);
        num = ability.property.getProperty(CivilizationProperty.SCIENCE);
        gameMode.getReport().playerAddTemporaryScience(player, num);
        player.getTempResManager().addTemplateResource(ability);
    }

    /**
     * 完成建造后触发的方法
     *
     * @param card
     */
    public void afterBuild(TTACard card) {
        if (card.cardType == CardType.WONDER) {
            WonderCard wonder = (WonderCard) card;
            if (wonder.isComplete()) {
                gameMode.getGame().playerCompleteWonder(player);
            }
        }
    }

    /**
     * 需要客户端回应的行动牌处理完成后触发的方法
     */
    public void afterPlayActionCard(ActionCard actionCard) {
        if (actionCard != null) {
            if (newcards.contains(actionCard)) {
                gameMode.getReport().playerActiveCardCache(player, CivilAbilityType.PLAY_NEW_ACTION_CARD);
                newcards.remove(actionCard);
                player.getParams().setRoundParameter(CivilAbilityType.PLAY_NEW_ACTION_CARD, 0);
            }
            gameMode.getGame().playerAddAction(player, actionCard.actionAbility.property);
            gameMode.getGame().playerRemoveHand(player, actionCard);
            gameMode.getGame().playerRequestEnd(player);
        }
    }

    /**
     * 出牌完成后触发的方法
     *
     * @param card
     * @throws BoardGameException
     */
    public void afterPlayCard(TTACard card) throws BoardGameException {
        // 处理所有出牌后触发的能力
        for (CivilCardAbility ability : player.getAbilityManager().getAbilitiesByType(CivilAbilityType.PA_PLAY_CARD)) {
            if (ability.test(card)) {
                gameMode.getGame().playerAddPoint(player, ability.property);
                gameMode.getGame().playerAddToken(player, ability.property);
                gameMode.getGame().playerAddAction(player, ability.property);
            }
        }
        for (CivilCardAbility ability : card.abilities) {
            if (ability.abilityType == CivilAbilityType.PA_GET_AND_PLAY) {
                TTAGetAndPlayExecutor executor = new TTAGetAndPlayExecutor(this, ability);
                executor.execute();
            }
        }
        if (player.getUncompleteWonder() != null && player.getUncompleteWonder().isComplete()) {
            gameMode.getGame().playerCompleteWonder(player);
        }
    }

    /**
     * 拿牌后触发的方法
     *
     * @param card
     */
    public void afterTakeCard(TTACard card) {
        // 检查所有拿牌后触发的方法
        for (CivilCardAbility ability : player.getAbilityManager().getAbilitiesByType(CivilAbilityType.PA_TAKE_CARD)) {
            if (ability.test(card)) {
                gameMode.getGame().playerAddPoint(player, ability.property);
            }
        }
    }

    /**
     * 完成升级后触发的方法
     *
     * @param toCard
     * @return
     */
    public void afterUpgrade(TTACard toCard) {
        // 如果使用了actionCard则从手牌中移除该卡,并关闭请求窗口
    }

    public void checkActionCardEnhance(ActionCard actionCard) {
        checkActionCardEnhance(actionCard.actionAbility.property);
    }

    public void checkActionCardEnhance(TTAProperty property) {
        property.clearAllBonus();
        for (CivilCardAbility a : player.getAbilityManager().getAbilitiesByType(CivilAbilityType.ADJUST_ACTION_CARD)) {
            for (CivilizationProperty p : property.getAllProperties().keySet()) {
                int num = a.property.getProperty(p);
                if (num > 0) {
                    if (property.getProperty(p) > 0) {
                        property.addPropertyBonus(p, num);
                    } else if (property.getProperty(p) < 0) {
                        property.addPropertyBonus(p, -num);
                    }
                }
            }
        }
    }

    public boolean checkAlert(TTACard card) {
        if (!buildAlert && card.needWorker() && player.isWillUprising()) {
            // 每次建造只会警告一次
            buildAlert = true;
            gameMode.getGame().sendAlert(player, "你的人民生活在水深火热之中,如果再让他们干活,你就死定了!");
            return false;
        }
        return true;
    }

    /**
     * @param card
     * @return
     */
    public boolean checkBach(TechCard card) {
        if (player.getParams().getInteger(CivilAbilityType.PA_UPGRADE_TO_THEATER) > 0) {
            CivilCardAbility ability = player.getAbilityManager().getAbility(CivilAbilityType.PA_UPGRADE_TO_THEATER);
            return ability.test(card);
        }
        return false;
    }

    /**
     * @return
     * @throws BoardGameException
     */
    public int checkBuildStep(int leftStep) throws BoardGameException {
        CivilCardAbility ability = player.getAbilityManager().getAbility(CivilAbilityType.PA_WONDER_STEP);
        if (leftStep > 1 && ability != null) {
            TTACard stepCard = player.getAbilityManager().getAbilityCard(CivilAbilityType.PA_WONDER_STEP);
            int maxStep = Math.min(ability.buildStep, leftStep);
            String sels = "1,2,3,4".substring(0, maxStep * 2 - 1);
            InterruptParam param = gameMode.insertListener(new TTARequestSelectListener(player, stepCard,
                    TTACmdString.REQUEST_SELECT, sels, "你可以建造奇迹的最多" + ability.buildStep + "步"));
            int sel = param.getInteger("sel");
            int buildStep = sel + 1;
            CheckUtils.check(buildStep <= 0, "你至少要建造奇迹的 1 步!");
            CheckUtils.check(buildStep > ability.buildStep, "你最多可以建造奇迹的 " + ability.buildStep + " 步!");
            return buildStep;
        }
        return 1;
    }

    /**
     * 检查玩家是否可以革命
     *
     * @param isVersion2
     * @param revolution
     * @return
     * @throws BoardGameException
     */

    public ActionType checkCanRevolution(boolean isVersion2, int revolution) throws BoardGameException {
        if (player.getAbilityManager().hasAbilitiy(CivilAbilityType.PA_MILITARY_REVOLUTION)) {
            // 以军事行动点进行革命
            // 检查军事行动点是否已经使用过
            // 老版革命必须是该回合的首个内政/军事行动
            CheckUtils.check(!isVersion2 && !isFirstMilitaryAction, "以革命的方式改变政府,必须是当前回合唯一的军事行动!");
            CheckUtils.check(
                    player.getAvailableMilitaryAction() < player.getProperty(CivilizationProperty.MILITARY_ACTION),
                    "以革命的方式改变政府,必须保留所有的军事行动点!");
            return ActionType.MILITARY;
        } else {
            // 以内政行动点进行革命
            // 检查内政行动点是否已经使用过
            // 老版革命必须是该回合的首个内政/军事行动
            CheckUtils.check(!isVersion2 && !isFirstCivilAction, "以革命的方式改变政府,必须是当前回合唯一的内政行动!");
            CheckUtils.check(player.getAvailableCivilAction() < player.getProperty(CivilizationProperty.CIVIL_ACTION),
                    "以革命的方式改变政府,必须保留所有的内政行动点!");
            return ActionType.CIVIL;
        }
    }

    /**
     * 检查牌是否是当前回合拿的,如果是,则抛出异常
     *
     * @param card
     * @throws BoardGameException
     */
    public void checkNewlyCard(TTACard card) throws BoardGameException {
        if (player.getParams().getInteger(CivilAbilityType.PLAY_NEW_ACTION_CARD) > 0) {
            return;
        }
        CheckUtils.check(this.newcards.contains(card), "你不能在当前回合打这张刚拿的牌!");
    }

    /**
     * 判断当前是否是政治行动阶段
     *
     * @throws BoardGameException
     */
    public void checkPoliticalPhase() throws BoardGameException {
        CheckUtils.check(currentStep != RoundStep.POLITICAL, "当前不是政治行动阶段,不能进行该行动!");
    }

    /**
     * 检查玩家是否拥有足够的科技点数,如果不够则抛出异常
     *
     * @throws BoardGameException
     */
    public void checkResearchCost(TechCard card, CostParam param) throws BoardGameException {
        // 玩家最终检查
        player.checkEnoughReource(param.cost);
        // 科技协作检查
        Map<CivilCardAbility, PactCard> abilities = player.getAbilityManager()
                .getPactAbilitiesWithRelation(CivilAbilityType.PA_SCIENCE_ASSIST);
        for (CivilCardAbility ca : abilities.keySet()) {
            CheckUtils.check(abilities.get(ca).alian.getSciencePoint() < ca.amount, "你的盟友没有足够的科技点数!");
        }
    }

    /**
     * 检查玩家是否可以对card进行升级的行动
     *
     * @param card
     * @throws BoardGameException
     */
    public void checkUpgrade(TTACard card) throws BoardGameException {
        switch (card.cardType) {
            case BUILDING:
            case PRODUCTION:
            case UNIT:
                break;
            default:
                throw new BoardGameException("你不能在这张卡牌上进行升级行动!");
        }
        CivilCard c = (CivilCard) card;
        CheckUtils.check(c.getWorkers() <= 0, "这个张卡牌上没有工人,不能升级!");
    }

    public void checkWillCard() {
        if (player.getWillCard() != null) {
            TTACard card = player.getWillCard();
            player.setWillCard(null);
            if (card.activeAbility.checkCanActive(RoundStep.RESIGNED, player)) {
                try {
                    TTAWillExecutor executor = TTAExecutorFactory.createWillExecutor(this, player, card);
                    executor.execute();
                } catch (BoardGameException e) {
                    // player.sendException(mode.getBoardGame().getRoom().id, e);
                }
            }
        }

    }

    public void checkWillCard(TTAPlayer player) {
        RoundParam param = this.listener.getParam(player.getPosition());
        param.checkWillCard();
    }

    /**
     * 新版1回合只能更换1次阵型
     *
     * @throws BoardGameException
     */
    public void chekChangeTactics() throws BoardGameException {
        CheckUtils.check(gameMode.isVersion2 && player.getParams().getBoolean(CardType.TACTICS), "你这回合已经更换过阵型!");
    }

    /**
     * 取得玩家打特定科技牌时的费用
     *
     * @param card
     * @return
     */

    public CostParam getResearchCost(TechCard card, boolean bSecondary, int costModify) {
        CostParam param = new CostParam();
        int cost = player.getResearchCost(card, bSecondary);
        // 科技协作能力
        Map<CivilCardAbility, PactCard> abilities = player.getAbilityManager()
                .getPactAbilitiesWithRelation(CivilAbilityType.PA_SCIENCE_ASSIST);
        for (CivilCardAbility ca : abilities.keySet()) {
            cost += ca.property.getProperty(CivilizationProperty.SCIENCE);
        }
        // 科技消耗最低为0
        cost = Math.max(0, cost + costModify);
        param.cost.addProperty(CivilizationProperty.SCIENCE, cost);
        // 计算临时科技
        if (cost > 0 && this.currentStep == RoundStep.NORMAL) {
            gameMode.getGame().checkTemplateResource(player, card, param, CivilizationProperty.SCIENCE);
        }
        return param;
    }

    /**
     * 取得玩家打特定科技牌时的费用
     *
     * @param card
     * @return
     */

    public CostParam getResearchCost(TechCard card, int costModify) {
        return getResearchCost(card, false, costModify);
    }

    /**
     * 取得建造 建筑/农矿/部队 所用的实际费用
     *
     * @param card
     * @param costModify 直接的资源修正值
     * @return
     */

    public CostParam getResourceCost(TechCard card, int costModify) {
        CostParam param = new CostParam();
        int cost = player.getBuildResourceCost(card);
        // 计算影响玩家建筑费用的全局能力[PA_BUILD_COST_GLOBAL]
        Map<CivilCardAbility, TTAPlayer> abilities = gameMode.getPlayerAbilities(CivilAbilityType.PA_BUILD_COST_GLOBAL);
        for (CivilCardAbility ca : abilities.keySet()) {
            if (ca.test(card) && (ca.effectSelf || player != abilities.get(ca))) {
                cost += ca.property.getProperty(CivilizationProperty.RESOURCE);
            }
        }
        // 加上修正值
        cost = Math.max(0, cost + costModify);
        param.cost.addProperty(CivilizationProperty.RESOURCE, cost);
        // 计算临时资源
        if (cost > 0 && this.currentStep == RoundStep.NORMAL) {
            gameMode.getGame().checkTemplateResource(player, card, param, CivilizationProperty.RESOURCE);
        }
        return param;
    }

    /**
     * 取得建造奇迹所用的实际费用
     *
     * @param card
     * @param step 建造步骤
     * @return
     */

    public CostParam getResourceCost(WonderCard card, int step, int costModify) {
        CostParam param = new CostParam();
        int cost = card.stepCostResource(step);
        // 加上修正值
        cost = Math.max(0, cost + costModify);
        // 如果费用已经等于0则不用再计算临时资源
        param.cost.addProperty(CivilizationProperty.RESOURCE, cost);
        if (cost > 0 && this.currentStep == RoundStep.NORMAL) {
            gameMode.getGame().checkTemplateResource(player, card, param, CivilizationProperty.RESOURCE);
        }
        return param;
    }

    /**
     * 取得升级 建筑/农矿/部队 所用的实际费用
     *
     * @param fromCard
     * @param toCard
     * @return
     */

    public CostParam getUpgradeResourceCost(TechCard fromCard, TechCard toCard, int costModify) {
        CostParam param = new CostParam();
        int fromCost = player.getBuildResourceCost(fromCard);
        int toCost = player.getBuildResourceCost(toCard);
        // 升级费用为两者建造费用的差
        int cost = toCost - fromCost;
        // 计算影响玩家升级费用的全局能力
        Map<CivilCardAbility, TTAPlayer> abilities = gameMode
                .getPlayerAbilities(CivilAbilityType.PA_UPGRADE_COST_GLOBAL);
        for (CivilCardAbility ca : abilities.keySet()) {
            if (ca.test(toCard) && (ca.effectSelf || player != abilities.get(ca))) {
                cost += ca.property.getProperty(CivilizationProperty.RESOURCE);
            }
        }
        // 加上修正值
        cost = Math.max(0, cost + costModify);
        param.cost.addProperty(CivilizationProperty.RESOURCE, cost);
        // 如果费用已经等于0则不用再计算临时资源
        if (cost > 0) {
            gameMode.getGame().checkTemplateResource(player, toCard, param, CivilizationProperty.RESOURCE);
        }
        return param;
    }

    /**
     * @param doDiscard
     */
    public void regroupCardRow(boolean doDiscard) {
        if (gameMode.getGame().regroupCardRow(doDiscard)) {
            for (TTAPlayer player : gameMode.getRealPlayers()) {
                this.checkWillCard(player);
            }
        }
    }

    /**
     * 玩家使用行动点
     *
     * @param actionType 内政/军事
     * @param actionCost
     */
    public void useActionPoint(ActionType actionType, int actionCost) {
        if (actionCost > 0) {
            if (actionType == ActionType.CIVIL) {
                gameMode.getGame().playerAddCivilAction(player, -actionCost);
                this.isFirstCivilAction = false;
            } else {
                gameMode.getGame().playerAddMilitaryAction(player, -actionCost);
                this.isFirstMilitaryAction = false;
            }
        }
    }

    /**
     * @param useBach
     */
    public void useBach(boolean useBach) {
        if (useBach) {
            int num = player.getParams().getInteger(CivilAbilityType.PA_UPGRADE_TO_THEATER);
            player.getParams().setRoundParameter(CivilAbilityType.PA_UPGRADE_TO_THEATER, num - 1);
            gameMode.getReport().playerActiveCardCache(player, CivilAbilityType.PA_UPGRADE_TO_THEATER);
        }
    }
}
