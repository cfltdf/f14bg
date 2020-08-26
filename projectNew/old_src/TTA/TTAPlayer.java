package com.f14.TTA;

import com.f14.TTA.component.*;
import com.f14.TTA.component.ability.CivilCardAbility;
import com.f14.TTA.component.ability.ScoreAbility;
import com.f14.TTA.component.card.*;
import com.f14.TTA.component.card.TacticsCard.TacticsResult;
import com.f14.TTA.consts.*;
import com.f14.TTA.manager.TTAAbilityManager;
import com.f14.TTA.manager.TTAConstManager;
import com.f14.TTA.manager.TTATemplateResourceManager;
import com.f14.bg.common.ParamSet;
import com.f14.bg.component.ICondition;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.player.Player;
import com.f14.bg.utils.BgUtils;
import com.f14.bg.utils.CheckUtils;

import java.util.*;

/**
 * TTA的玩家
 *
 * @author F14eagle
 */
public class TTAPlayer extends Player {
    protected static final String PARAM_LEADER = "LEADER_";
    /**
     * 玩家下回合发动的临时属性(调整下回合CA,跳过政治阶段等)
     */

    public ParamSet roundTempParam = new ParamSet();
    /**
     * 玩家的得分情况
     */

    public TTAProperty points = new TTAProperty();
    /**
     * 政治手牌
     */

    public TTACardDeck civilHands = new TTACardDeck();
    /**
     * 军事手牌
     */

    public TTACardDeck militaryHands = new TTACardDeck();
    /**
     * 玩家指示物
     */

    public TokenPool tokenPool = new TokenPool();
    /**
     * 玩家卡牌能力管理器
     */

    public TTAAbilityManager abilityManager = new TTAAbilityManager();
    /**
     * 玩家临时资源管理器
     */
    public TTATemplateResourceManager tempResManager;
    /**
     * 当前的政府
     */

    public GovermentCard goverment;
    /**
     * 体面退出游戏
     */
    public boolean resigned = false;
    /**
     * 时代虚拟卡
     */
    public TTACard ageDummyCard;
    /**
     * 遗言（领袖）
     */

    public TTACard willCard;
    /**
     * 所属游戏模式
     */

    private TTAGameRank gameRank = null;
    /**
     * 玩家属性
     */

    private TTAProperty properties = new TTAProperty();
    /**
     * 玩家已打出的农矿,建筑,部队,建成的奇迹,殖民地,特殊科技
     */

    private TTACardDeck buildings = new TTACardDeck();
    /**
     * 玩家的回合参数
     */

    private CivilCard leader;

    private WonderCard uncompleteWonder;

    private TacticsCard tactics;

    private AttackCard war;

    private PactCard pact;

    public TTAPlayer() {
        this.tempResManager = new TTATemplateResourceManager(this);
    }

    /**
     * 调整玩家可用工人的数量
     *
     * @param num
     * @return
     */
    public int addAvailableWorker(int num) {
        int res = this.tokenPool.addAvailableWorker(num);
        this.checkUnhappyWorkers();
        return res;
    }

    /**
     * 玩家获得牌
     *
     * @param card
     * @return
     */

    public TTAProperty addCard(TTACard card) {
        TTAProperty res = new TTAProperty();
        int usedca = properties.getProperty(CivilizationProperty.CIVIL_ACTION) - this.getAvailableCivilAction();
        int usedma = properties.getProperty(CivilizationProperty.MILITARY_ACTION) - this.getAvailableMilitaryAction();
        TTACard removedCard = null;
        switch (card.cardType) {
            case PRODUCTION:
            case BUILDING:
            case UNIT:
            case WONDER:
            case EVENT: // EVENT中只可能出现领土牌
                // 将该卡牌添加到玩家的已打出建筑牌堆中并排序
                buildings.addCard(card);
                buildings.sortCards();
                break;
            case SPECIAL:
                // 特殊科技,同种类型的只能存在一张,打出新的时需要将原科技废除
                for (TTACard c : this.getBuildingsBySubType(card.cardSubType)) {
                    removedCard = c;
                }
                buildings.removeCard(removedCard);
                buildings.addCard(card);
                buildings.sortCards();
                break;
            case GOVERMENT:
                // 移除原政府,添加新政府
                removedCard = this.goverment;
                this.goverment = (GovermentCard) card;
                break;
            case LEADER:
                // 移除原领袖,添加新领袖
                removedCard = this.leader;
                this.leader = (CivilCard) card;
                break;
            case TACTICS: // 战术牌
                // 移除原战术牌,应用新的战术牌
                removedCard = this.tactics;
                this.tactics = (TacticsCard) card;
                break;
            case WAR: // 战争
                AttackCard war = (AttackCard) card;
                if (war.getOwner() == this) {
                    // 如果该战争牌属于当前玩家,则设置当前玩家的war
                    removedCard = this.war;
                    this.war = war;
                }
                buildings.addCard(war);
                break;
            case PACT: // 条约
                PactCard pact = (PactCard) card;
                if (pact.getOwner() == this) {
                    // 如果该条约牌属于当前玩家,则设置当前玩家的pact
                    removedCard = this.pact;
                    this.pact = pact;
                }
                this.buildings.addCard(pact);
                break;
            case TICHU:
                buildings.addCard(card);
            default:
                break;
        }
        // 处理添加卡牌时的事件
        res = this.onCardChange(card, removedCard, usedca, usedma);
        // 刷新属性

        if (removedCard != null && removedCard.cardType == CardType.GOVERMENT) {
            this.goverment.addReds(((GovermentCard) removedCard).getReds());
            this.goverment.addWhites(((GovermentCard) removedCard).getWhites());
        }
        return res;
    }

    /**
     * 调整当前可用的内政行动点数
     *
     * @param num
     */
    public void addCivilAction(int num) {
        if (this.goverment != null) {
            this.goverment.addWhites(num);
        }
    }

    /**
     * 调整当前的文明点数
     *
     * @param num
     */
    public void addCulturePoint(int num) {
        this.points.addProperty(CivilizationProperty.CULTURE, num);
    }

    /**
     * 调整玩家的食物
     *
     * @param num
     * @return
     */

    public ProductionInfo addFood(int num) {
        if (num > 0) {
            return this.getBlueToken(CardSubType.FARM, num);
        } else if (num < 0) {
            return this.payBlueToken(CardSubType.FARM, -num);
        } else {
            return new ProductionInfo(CivilizationProperty.FOOD);
        }
    }

    /**
     * 玩家得到手牌
     *
     * @param cards
     */
    public void addHand(List<TTACard> cards) {
        for (TTACard card : cards) {
            if (card.actionType == ActionType.CIVIL) {
                this.civilHands.addCard(card);
            } else {
                this.militaryHands.addCard(card);
            }
        }
    }

    /**
     * 调整当前可用的军事行动点数
     *
     * @param num
     */
    public void addMilitaryAction(int num) {
        if (this.goverment != null) {
            this.goverment.addReds(num);
        }
    }

    /**
     * 玩家添加属性,返回与原先玩家属性的差值
     *
     * @param properties
     * @return
     */

    public TTAProperty addProperties(TTAProperty properties) {
        TTAProperty res = new TTAProperty();
        Map<CivilizationProperty, Integer> orgvalues = this.getProperties().getAllProperties();
        this.getProperties().addProperties(properties);
        // 计算玩家属性更新后与原来属性的差值
        for (CivilizationProperty p : properties.getAllFactProperties().keySet()) {
            res.setProperty(p, this.getProperties().getProperty(p) - orgvalues.getOrDefault(p, 0));
        }
        return res;
    }

    /**
     * 调整玩家的资源
     *
     * @param num
     * @return
     */

    public ProductionInfo addResource(int num) {
        if (num > 0) {
            return this.getBlueToken(CardSubType.MINE, num);
        } else if (num < 0) {
            return this.payBlueToken(CardSubType.MINE, -num);
        } else {
            return new ProductionInfo(CivilizationProperty.RESOURCE);
        }
    }

    /**
     * 调整当前的科技点数
     *
     * @param num
     */
    public void addSciencePoint(int num) {
        this.points.addProperty(CivilizationProperty.SCIENCE, num);
    }

    /**
     * 玩家建造建筑/部队
     *
     * @param card
     * @return
     */
    public void build(CivilCard card) {
        this.tokenPool.addUnusedWorker(-1);
        card.addWorkers(1);
    }

    /**
     * 玩家建造奇迹
     *
     * @param step
     * @return 返回所有状态变化过的卡牌列表
     */
    public void buildWonder(int step) {
        if (uncompleteWonder != null) {
            tokenPool.takeAvailableBlues(step);
            // 在奇迹上放置蓝色指示物表示完成建造的步骤
            this.getUncompleteWonder().buildStep(step);
        }
    }

    /**
     * 检查能力是否还能够使用
     *
     * @param ability
     * @return
     */
    public boolean checkAbilitiy(CivilAbilityType ability) {
        return this.abilityManager.hasAbilitiy(ability) && !this.getParams().getBoolean(ability);
    }

    /**
     * 检查玩家是否拥有足够的内政/军事行动点,如果不够则抛出异常
     *
     * @param actionCost
     * @throws BoardGameException
     */
    public void checkActionPoint(ActionType actionType, int actionCost) throws BoardGameException {
        CheckUtils.check(actionCost > this.getAvailableActionPoint(actionType), actionType.getChinese() + "行动点不够,你还能使用 "
                + this.getAvailableActionPoint(actionType) + " 个" + actionType.getChinese() + "行动点!");
    }

    /**
     * 检查玩家各项资源够不够
     *
     * @param property
     * @throws BoardGameException
     */
    public void checkEnoughReource(TTAProperty property) throws BoardGameException {
        this.checkEnoughReource(property, 1);
    }

    /**
     * 检查玩家各项资源够不够
     *
     * @param property
     * @param multi
     * @throws BoardGameException
     */
    public void checkEnoughReource(TTAProperty property, int multi) throws BoardGameException {
        CheckUtils.check(this.getSciencePoint() < multi * property.getProperty(CivilizationProperty.SCIENCE),
                "你没有足够的科技点数!");
        CheckUtils.check(this.getCulturePoint() < multi * property.getProperty(CivilizationProperty.CULTURE),
                "你没有足够的文化分数!");
        CheckUtils.check(this.getTotalFood() < multi * property.getProperty(CivilizationProperty.FOOD), "你没有足够的食物!");
        CheckUtils.check(this.getTotalResource() < multi * property.getProperty(CivilizationProperty.RESOURCE),
                "你没有足够的资源!");
    }

    /**
     * 检查玩家是否可以拿取指定的卡牌,如果不能则抛出异常
     *
     * @param card @throws
     */
    public void checkTakeCard(TTACard card) throws BoardGameException {
        // 只有当前手牌数小于总内政行动点数时才能拿,奇迹牌不入手无需判断
        CheckUtils.check(card.cardType != CardType.WONDER && this.civilHands.size() >= this.getCivilHandLimit(),
                "你的内政牌数量已经达到上限!");

        switch (card.cardType) {
            case WONDER:
                // 如果拿的是奇迹牌,并且拥有在建的奇迹,则不能再拿
                CheckUtils.check(this.uncompleteWonder != null, "你的奇迹正在建造中,不能拿取新的奇迹!");
                break;
            case LEADER:
                // 如果是领袖牌,则需要判断是否已经拥有同等级的领袖,有则不能再拿
                CheckUtils.check(this.hasLeader(card.level), "你已经拥有该时代的领袖了!");
                break;
            default:
                // 如果是科技牌,则不能重复拿
                CheckUtils.check(card.isTechnologyCard() && this.hasSameCard(card), "你已经拥有该科技了!");
        }
    }

    /**
     * 检查并设置玩家不满意的工人数
     */
    protected void checkUnhappyWorkers() {
        // 需要同时调整玩家不开心的工人数
        int need = TTAConstManager.getNeedHappiness(this.tokenPool.getAvailableWorkers());
        int value = need - this.getProperty(CivilizationProperty.HAPPINESS);
        if (value > 0) {
            // 如果需要的幸福度不够,则设置不开心的工人数量
            this.properties.setProperty(CivilizationProperty.DISCONTENT_WORKER, value);
            this.tokenPool.setUnhappyWorkers(value);
        } else {
            // 否则就没有不开心的工人
            this.properties.setProperty(CivilizationProperty.DISCONTENT_WORKER, 0);
            this.tokenPool.setUnhappyWorkers(0);
        }
    }

    /**
     * 检查玩家是否可以使用card
     *
     * @param card
     * @throws BoardGameException
     */
    public void checkUseCard(TTACard card) throws BoardGameException {
        // 检查是否有能力限制使用卡牌
        for (CivilCardAbility ca : abilityManager.getAbilitiesByType(CivilAbilityType.PA_USE_CARD_LIMIT)) {
            CheckUtils.check(ca.test(card), "你不能使用这种类型的卡牌!");
        }
        // 检查使用行动点限制
        if (card.actionCost != null && card.actionCost.adjustType == null) {
            this.checkActionPoint(card.actionCost.actionType, card.actionCost.actionCost);
        }
    }

    /**
     * 清空手牌
     *
     * @return
     */
    public void clearAllHands() {
        this.militaryHands.clear();
        this.civilHands.clear();
    }

    /**
     * 清空玩家已打在桌面上的所有牌
     *
     * @return
     */
    public void clearAllPlayedCard() {
        this.buildings.clear();
        this.goverment = null;
        this.leader = null;
    }

    /**
     * 玩家完成奇迹的建造
     *
     * @return 返回所有状态变化过的卡牌列表
     */

    public TTAProperty completeWonder() {
        // 如果建造完成,则需要将转移该建成的奇迹
        // 将奇迹牌上的蓝色标志物返回资源库
        WonderCard wonder = this.getUncompleteWonder();
        this.setUncompleteWonder(null);
        if (wonder.activeAbility == null || wonder.activeAbility.abilityType != ActiveAbilityType.PA_SHIHUANG_TOMB) {
            int blues = wonder.getBlues();
            wonder.addBlues(-blues);
            tokenPool.putAvailableBlues(blues);
        }
        return this.addCard(wonder);
    }

    /**
     * 减少空闲人口,人口回到资源库
     *
     * @return 实际减少的人口数
     * @throws BoardGameException
     */
    public int decreasePopulation(int num) {
        int unum = Math.min(this.tokenPool.getUnusedWorkers(), num);
        this.addAvailableWorker(unum);
        this.tokenPool.addUnusedWorker(-unum);
        return unum;
    }

    /**
     * 玩家摧毁建筑/部队
     *
     * @param card
     * @param num
     * @return 返回拆掉的实际数量
     */
    public int destory(CivilCard card, int num) {
        int i = card.addWorkers(-num);
        this.tokenPool.addUnusedWorker(i);
        return i;
    }

    /**
     * 取得指定阶段中玩家可用的卡牌列表
     *
     * @param activeStep
     * @return
     */

    public Collection<TTACard> getActiveCards(RoundStep activeStep) {
        List<TTACard> cards = new ArrayList<>();
        for (TTACard c : this.abilityManager.getActiveCards()) {
            if (c.activeAbility.checkCanActive(activeStep, this)) {
                cards.add(c);
            }
        }
        return cards;
    }

    /**
     * 取得所有手牌
     *
     * @return
     */

    public List<TTACard> getAllHands() {
        List<TTACard> cards = new ArrayList<>();
        cards.addAll(this.civilHands.getCards());
        cards.addAll(this.militaryHands.getCards());
        return cards;
    }

    /**
     * 取得玩家已打在桌面上的所有牌
     *
     * @return
     */

    public List<TTACard> getAllPlayedCard() {
        return getAllPlayedCard(false);
    }

    /**
     * 取得玩家已打在桌面上的所有牌
     *
     * @return
     */

    public List<TTACard> getAllPlayedCard(boolean bUncompleteWonder) {
        List<TTACard> cards = new ArrayList<>();
        cards.addAll(this.buildings.getCards());
        if (this.goverment != null) {
            cards.add(this.goverment);
        }
        if (this.leader != null) {
            cards.add(this.leader);
        }
        if (this.uncompleteWonder != null && bUncompleteWonder) {
            cards.add(this.uncompleteWonder);
        }
        return cards;
    }

    /**
     * 玩家为侵略方时的战力调整
     *
     * @param card   侵略或战争卡
     * @param target 目标玩家
     * @return
     */
    public int getAttackerMilitary(TTACard card, TTAPlayer target) {
        int defensiveMilitary = this.properties.getProperty(CivilizationProperty.DEFENSIVE_MILITARY);
        if (card.cardSubType == CardSubType.AGGRESSION) {
            this.properties.addPropertyBonus(CivilizationProperty.MILITARY, -defensiveMilitary);
        }
        int ret = this.getProperty(CivilizationProperty.MILITARY);
        if (card.cardSubType == CardSubType.AGGRESSION) {
            this.properties.addPropertyBonus(CivilizationProperty.MILITARY, defensiveMilitary);
        }
        // 攻击盟友时调整战力的能力
        Map<CivilCardAbility, PactCard> pacts = abilityManager
                .getPactAbilitiesWithRelation(CivilAbilityType.PA_ATTACK_ALIAN_ADJUST);
        for (CivilCardAbility ca : pacts.keySet()) {
            if (pacts.get(ca).alian == target) {
                ret += ca.property.getProperty(CivilizationProperty.MILITARY);
            }
        }
        if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_HUBATIAN)) {
            ret /= 2;
        }
        return ret;
    }

    /**
     * 取得指定类型的行动点数
     *
     * @param actionType
     * @return
     */
    public int getAvailableActionPoint(ActionType actionType) {
        if (actionType == ActionType.CIVIL) {
            return this.getAvailableCivilAction();
        } else {
            return this.getAvailableMilitaryAction();
        }
    }

    public int getAvailableActionPoint(CivilizationProperty property) {
        switch (property) {
            case CIVIL_ACTION:
                return this.getAvailableCivilAction();
            case MILITARY_ACTION:
                return this.getAvailableMilitaryAction();
            default:
                return 0;
        }
    }

    /**
     * 取得当前可用的内政行动点数
     *
     * @return
     */
    public int getAvailableCivilAction() {
        if (this.goverment != null) {
            return this.goverment.getWhites();
        } else {
            return 0;
        }
    }

    /**
     * 取得当前可用的军事行动点数
     *
     * @return
     */
    public int getAvailableMilitaryAction() {
        if (this.goverment != null) {
            return this.goverment.getReds();
        } else {
            return 0;
        }
    }

    /**
     * 得到指定数量的资源,并将得到的蓝色指示物放在卡牌上
     *
     * @param cardSubType
     * @param num
     * @return
     */

    protected ProductionInfo getBlueToken(CardSubType cardSubType, int num) {
        ResourceTaker taker = new ResourceTaker(cardSubType);
        taker.putResource(num);
        return taker.execute();
    }

    /**
     * 取得玩家所有的防御/殖民地加值卡
     *
     * @return
     */

    public List<BonusCard> getBonusCards() {
        List<BonusCard> res = new ArrayList<>();
        for (TTACard c : this.getAllHands()) {
            if (c.cardType == CardType.DEFENSE_BONUS) {
                res.add((BonusCard) c);
            }
        }
        return res;
    }

    /**
     * 取得玩家指定类型的已建造建筑数量
     *
     * @param cardSubType
     * @return
     */
    public int getBuildingNumber(CardSubType cardSubType) {
        int res = 0;
        for (CivilCard c : this.getBuildingsBySubType(cardSubType)) {
            res += c.getWorkers();
        }
        return res;
    }

    /**
     * 取得玩家已打出的所有建筑牌堆
     *
     * @return
     */

    public TTACardDeck getBuildings() {
        return this.buildings;
    }

    /**
     * 取得玩家 的卡牌,并按等级从大到小排序
     *
     * @param cardSubType
     * @return
     */

    public List<CivilCard> getBuildingsBySubType(CardSubType cardSubType) {
        List<CivilCard> res = new ArrayList<>();
        for (TTACard c : this.getAllPlayedCard()) {
            if (c.cardSubType == cardSubType) {
                res.add((CivilCard) c);
            }
        }
        Collections.sort(res);
        Collections.reverse(res);
        return res;

    }

    /**
     * 取得玩家建造建筑时的费用
     *
     * @param card
     * @return
     */
    public int getBuildResourceCost(TechCard card) {
        int res = card.costResource;
        // 计算所有调整建造费用的能力
        for (CivilCardAbility a : abilityManager.getAbilitiesByType(CivilAbilityType.PA_BUILD_COST)) {
            if (a.test(card)) {
                res += a.getAvailableProperty(this, CivilizationProperty.RESOURCE);
            }
        }
        // 计算新版莎士比亚调整建筑费用的能力
        for (CivilCardAbility a : abilityManager.getAbilitiesByType(CivilAbilityType.PA_BUILD_COST_GROUP)) {
            if (a.testGroup(this.getAllPlayedCard(), card)) {
                res += a.getAvailableProperty(this, CivilizationProperty.RESOURCE);
            }
        }
        res = Math.max(0, res);
        return res;
    }

    /**
     * 从手牌中取得指定的牌,如果不存在则抛出异常
     *
     * @param cardId
     * @return
     * @throws BoardGameException
     */

    public TTACard getCard(String cardId) throws BoardGameException {
        return this.getCardFromList(cardId, this.getAllHands());
    }

    /**
     * 取得指定的牌,如果不存在则抛出异常
     *
     * @param cardId
     * @param cards
     * @return
     * @throws BoardGameException
     */

    protected TTACard getCardFromList(String cardId, List<TTACard> cards) throws BoardGameException {
        for (TTACard c : cards) {
            if (c.id.equals(cardId)) {
                return c;
            }
        }
        throw new BoardGameException("没有找到指定的对象!");
    }

    /**
     * 从手牌中取得指定的牌,如果不存在则抛出异常
     *
     * @param cardIds
     * @return
     * @throws BoardGameException
     */

    public List<TTACard> getCards(String cardIds) throws BoardGameException {
        String[] ids = cardIds.split(",");
        List<TTACard> res = new ArrayList<>();
        List<TTACard> hands = this.getAllHands();
        for (String id : ids) {
            res.add(this.getCardFromList(id, hands));
        }
        return res;
    }

    /**
     * 取得玩家内政手牌的上限
     *
     * @return
     */
    public int getCivilHandLimit() {
        // 内政点数+内政手牌数量调整
        return this.properties.getProperty(CivilizationProperty.CIVIL_ACTION)
                + this.properties.getProperty(CivilizationProperty.CIVIL_HANDS);
    }

    /**
     * 取得玩家所有建成奇迹的数量
     *
     * @return
     */
    public int getCompletedWonderNumber() {
        return this.getBuildingsBySubType(CardSubType.WONDER).size();
    }

    /**
     * 玩家粮食消费
     *
     * @return
     */
    public int getConsumption() {
        int foodSupply = TTAConstManager.getFoodSupply(this.tokenPool.getAvailableWorkers());
        for (CivilCardAbility a : abilityManager.getAbilitiesByType(CivilAbilityType.ADJUST_CONSUMPTION)) {
            foodSupply += a.getAvailableNumber(this);
        }
        return Math.max(foodSupply, 0);
    }

    /**
     * 取得当前的文明点数
     *
     * @return
     */
    public int getCulturePoint() {
        return this.points.getProperty(CivilizationProperty.CULTURE);
    }

    public int getDefenceMilitary() {
        int ret = this.getProperty(CivilizationProperty.MILITARY);
        if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_HUBATIAN)) {
            ret /= 2;
        }
        return ret;
    }

    /**
     * 取得双倍产值的卡牌集合,可叠加
     *
     * @param p
     * @return
     */

    public Map<CivilCard, Integer> getDoubledCards(CivilizationProperty p) {
        Map<CivilCard, Integer> doubledCards = new HashMap<>();
        for (CivilCardAbility ca : abilityManager.getAbilitiesByType(CivilAbilityType.DOUBLE_PRODUCE)) {
            if (ca.byProperty == p) {
                TTACard c = ca.getBestCard(this.getBuildings().getCards());
                if (c != null) {
                    doubledCards.put((CivilCard) c, doubledCards.getOrDefault(c, 1) * 2);
                }
            }
        }
        return doubledCards;
    }

    /**
     * @param card
     * @return
     */

    public TTAProperty getFinalRankValue(ActionCard card) {
        int rank = this.getRank(card.actionAbility.rankProperty, false);
        return card.getFinalRankValue(rank, getRealPlayerNumber());
    }

    /**
     * 取得玩家食物的总生产力
     *
     * @return
     */
    public int getFoodProduction() {
        // 基础农场产值

        return this.getProductionFromFarm().getTotalValue();
    }

    /**
     * 取得玩家的政府
     *
     * @return
     */

    public GovermentCard getGoverment() {
        return goverment;
    }

    /**
     * 按照条件取得玩家的手牌
     *
     * @param condition
     * @return
     */

    public List<TTACard> getHandCard(ICondition<TTACard> condition) {
        List<TTACard> res = this.getAllHands();
        res.removeIf(c -> !condition.test(c));
        return res;
    }

    /**
     * 取得玩家的领袖
     *
     * @return
     */

    public CivilCard getLeader() {
        return leader;
    }

    /**
     * 取得玩家抓军事牌的上限
     *
     * @return
     */
    public int getMilitaryDraw() {
        return this.properties.getProperty(CivilizationProperty.MILITARY_DRAW) + 3;
    }

    /**
     * 取得玩家军事手牌的上限
     *
     * @return
     */
    public int getMilitaryHandLimit() {
        // 军事点数+军事手牌数量调整
        return this.properties.getProperty(CivilizationProperty.MILITARY_ACTION)
                + this.properties.getProperty(CivilizationProperty.MILITARY_HANDS);
    }

    /**
     * 取得玩家打出的条约牌
     *
     * @return
     */

    public PactCard getPact() {
        return pact;
    }

    /**
     * 条约带来的军力
     *
     * @param pactIds
     * @return
     */
    public int getPactMilitary(List<String> pactIds) {
        int res = 0;
        for (TTACard c : this.getAllPlayedCard()) {
            if (pactIds.contains(c.id)) {
                res += c.property.getProperty(CivilizationProperty.MILITARY);
            }
        }
        return res;
    }

    /**
     * 按照条件取得玩家所有已打在桌面上的牌
     *
     * @param condition
     * @return
     */

    public List<TTACard> getPlayedCard(ICondition<TTACard> condition) {
        List<TTACard> res = this.getAllPlayedCard();
        res.removeIf(c -> !condition.test(c));
        return res;
    }

    /**
     * 按照cardId取得玩家已打在桌面上的牌
     *
     * @param cardId
     * @return
     * @throws BoardGameException
     */

    public TTACard getPlayedCard(String cardId) throws BoardGameException {
        return getPlayedCard(cardId, false);
    }

    /**
     * 按照cardId取得玩家已打在桌面上的牌
     *
     * @param cardId
     * @return
     * @throws BoardGameException
     */

    public TTACard getPlayedCard(String cardId, boolean bUncompleteWonder) throws BoardGameException {
        List<TTACard> cards = this.getAllPlayedCard(bUncompleteWonder);
        return this.getCardFromList(cardId, cards);
    }

    /**
     * 农场产能统计(计入其他卡牌对其的增益)
     *
     * @return
     */

    public ProductionInfo getProductionFromFarm() {
        Map<CivilCard, Integer> doubledCards = getDoubledCards(CivilizationProperty.FOOD);
        ProductionInfo res = new ProductionInfo(CivilizationProperty.FOOD);
        for (CivilCard c : this.getBuildingsBySubType(CardSubType.FARM)) {
            if (c.getWorkers() > 0) {
                res.put(c, doubledCards.getOrDefault(c, 1) + c.getWorkers() - 1);
            }
        }
        return res;
    }

    /**
     * 来自实验室的产能(比尔盖茨/特斯拉)
     *
     * @return
     */

    public ProductionInfo getProductionFromLab() {
        ProductionInfo res = new ProductionInfo(CivilizationProperty.RESOURCE);
        for (CivilCard c : this.getBuildingsBySubType(CardSubType.LAB)) {
            if (c.level > 0 && c.getWorkers() > 0) {
                res.put(c, c.getWorkers());
            }
        }
        return res;
    }

    /**
     * 矿山产能统计(计入其他卡牌对其的增益)
     *
     * @return
     */

    public ProductionInfo getProductionFromMine() {
        Map<CivilCard, Integer> doubledCards = getDoubledCards(CivilizationProperty.RESOURCE);
        ProductionInfo res = new ProductionInfo(CivilizationProperty.RESOURCE);
        for (CivilCard c : this.getBuildingsBySubType(CardSubType.MINE)) {
            if (c.getWorkers() > 0) {
                res.put(c, doubledCards.getOrDefault(c, 1) + c.getWorkers() - 1);
            }
        }
        return res;
    }

    /**
     * 取得玩家的所有属性
     *
     * @return
     */

    public TTAProperty getProperties() {
        return this.properties;
    }

    /**
     * 取得属性值
     *
     * @param property
     * @return
     */
    public int getProperty(CivilizationProperty property) {
        switch (property) {
            case DISCONTENT_WORKER: // 不满的工人数
                return tokenPool.getUnhappyWorkers();
            case BLUE_TOKEN:
                return tokenPool.getAvailableBlues();
            case FOOD:
                return this.getTotalFood();
            case RESOURCE:
                return this.getTotalResource();
            default:
                return properties.getProperty(property);
        }
    }

    /**
     * 取得玩家的某属性排名
     *
     * @return
     */
    public int getRank(CivilizationProperty byProperty, boolean weekest) {
        return gameRank.getPlayerRank(this, byProperty, weekest);
    }

    /**
     * 取得实际玩家数,影响因人数而异的卡牌能力
     *
     * @return
     */
    public int getRealPlayerNumber() {
        return gameRank.getPlayerNumber();
    }

    public int getResearchCost(TechCard card, boolean bSecondary) {
        int res = card.costScience;
        if (bSecondary) {
            res = ((GovermentCard) card).secondaryCostScience;
        }

        // 计算一般调整科技费用的能力(巴赫,老版丘吉尔等)
        for (CivilCardAbility a : abilityManager.getAbilitiesByType(CivilAbilityType.PA_PLAY_CARD_COST)) {
            if (a.test(card)) {
                res += a.getAvailableProperty(this, CivilizationProperty.SCIENCE);
            }
        }
        // 计算新版莎士比亚调整科技费用的能力
        for (CivilCardAbility a : abilityManager.getAbilitiesByType(CivilAbilityType.PA_PLAY_CARD_COST_GROUP)) {
            if (a.testGroup(this.getAllPlayedCard(), card)) {
                res += a.getAvailableProperty(this, CivilizationProperty.SCIENCE);
            }
        }
        return res;
    }

    /**
     * 取得玩家资源的总生产力
     *
     * @return
     */
    public int getResourceProduction() {
        // 基础矿山产值
        int res = this.getProductionFromMine().getTotalValue();

        // 特斯拉实验室产值
        if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_TESLA_ABILITY)) {
            res += this.getProductionFromLab().getTotalValue();
        }

        return res;
    }

    /**
     * 取得拿取资源后,将返回资源库的蓝色指示物个数
     *
     * @param num
     * @return
     */
    public int getReturnedBlues(int num) {
        ResourceTaker taker = new ResourceTaker(CardSubType.MINE);
        taker.takeResource(num);
        return taker.getReturnedBlues();
    }

    /**
     * 取得当前的科技点数
     *
     * @return
     */
    public int getSciencePoint() {
        return this.points.getProperty(CivilizationProperty.SCIENCE);
    }

    /**
     * 取得得分能力能够带给玩家的分数
     *
     * @return
     */
    public int getScoreCulturePoint(List<ScoreAbility> scoreAbilities) {
        int res = 0;
        for (ScoreAbility sa : scoreAbilities) {
            res += sa.getScoreCulturePoint(this);
        }
        return res;
    }

    /**
     * 取得当前的战术
     *
     * @return
     */

    public TacticsCard getTactics() {
        return tactics;
    }

    /**
     * 玩家算得的纯阵型加成值
     */

    public TacticsResult getTacticsResult(Map<TechCard, Integer> units) {
        // 新版成吉思汗能力
        return this.getTactics().getTacticsResult(units, this.abilityManager.hasAbilitiy(CivilAbilityType.PA_GENGHIS));
    }

    /**
     * 取得玩家的粮食总数
     *
     * @return
     */
    public int getTotalFood() {
        int res = 0;
        for (TTACard c : this.getBuildingsBySubType(CardSubType.FARM)) {
            res += ((CivilCard) c).getBlues() * c.property.getProperty(CivilizationProperty.FOOD);
        }
        return res;
    }

    /**
     * 取得玩家的资源总数
     *
     * @return
     */
    public int getTotalResource() {
        int res = 0;
        for (TTACard c : this.getBuildingsBySubType(CardSubType.MINE)) {
            res += ((CivilCard) c).getBlues() * c.property.getProperty(CivilizationProperty.RESOURCE);
        }
        if (this.getParams().getBoolean(CivilAbilityType.PA_TESLA_ABILITY)) {
            // 每个矿场上的资源等于其上蓝色指示物的数量 x 其资源产量
            for (TTACard c : this.getBuildingsBySubType(CardSubType.LAB)) {
                res += ((CivilCard) c).getBlues() * c.property.getProperty(CivilizationProperty.RESOURCE);
            }
        }
        return res;
    }

    /**
     * 取得当前在建的奇迹
     *
     * @return
     */

    public WonderCard getUncompleteWonder() {
        return uncompleteWonder;
    }

    /**
     * 设置当前在建的奇迹
     *
     * @param uncompleteWonder
     */
    public void setUncompleteWonder(WonderCard uncompleteWonder) {
        this.uncompleteWonder = uncompleteWonder;
    }

    /**
     * 取得玩家所有的部队信息
     *
     * @return
     */

    public List<Map<String, Object>> getUnitsInfo() {
        // 只返回部队的cardId和拥有的工人数量
        List<Map<String, Object>> res = new ArrayList<>();
        Condition con = new Condition();
        con.cardType = CardType.UNIT;
        for (TTACard c : this.getPlayedCard(con)) {
            Map<String, Object> o = new HashMap<>();
            o.put("cardId", c.id);
            o.put("num", c.getAvailableCount());
            res.add(o);
        }
        return res;
    }

    /**
     * 取得玩家打出的战争牌
     *
     * @return
     */

    public AttackCard getWar() {
        return war;
    }

    /**
     * 取得所有的工人数,包括空闲的工人
     *
     * @return
     */
    public int getWorkers() {
        int res = this.tokenPool.getUnusedWorkers();
        for (TTACard c : this.getAllPlayedCard()) {
            if (c.needWorker()) {
                res += ((CivilCard) c).getWorkers();
            }
        }
        return res;
    }

    /**
     * 判断玩家是否拥有指定等级的领袖
     *
     * @param level
     * @return
     */
    protected boolean hasLeader(int level) {
        return this.getParams().getBoolean(PARAM_LEADER + level);
    }

    /**
     * 判断玩家的手牌和打出的牌中是否拥有同名的牌
     *
     * @param card
     * @return
     */
    public boolean hasSameCard(TTACard card) {
        for (TTACard c : this.getAllHands()) {
            if (c.cardNo.equals(card.cardNo)) {
                return true;
            }
        }
        for (TTACard c : this.getAllPlayedCard()) {
            if (c.cardNo.equals(card.cardNo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否拥有部队
     *
     * @return
     */
    public boolean hasUnit() {
        Condition con = new Condition();
        con.cardType = CardType.UNIT;
        for (TTACard c : this.getPlayedCard(con)) {
            if (c.getAvailableCount() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 扩张人口
     *
     * @throws BoardGameException
     */
    public int increasePopulation(int num) {
        // 只有当存在可用工人时才能执行扩张人口
        int workers = -this.addAvailableWorker(-num);
        this.tokenPool.addUnusedWorker(workers);
        return workers;
    }

    /**
     * 初始化
     */
    public void init(TTAGameMode gameMode) {
        this.gameRank = gameMode.gameRank;
        TTAConstManager.initPlayerPoints(this.points, gameMode.getGame().getConfig().isNoLimit());
        TTAConstManager.initPlayerProperties(this.properties, gameMode.getGame().getConfig().isNoLimit());
        if (gameMode.getGame().isVersion2()) {
            this.tokenPool.init(18, 16, 1, 0);
        } else {
            this.tokenPool.init(18, 18, 1, 0);
        }
    }

    /**
     * 判断玩家是否会引起暴动
     *
     * @return
     */
    public boolean isUprising() {
        return this.tokenPool.getUnhappyWorkers() > this.tokenPool.getUnusedWorkers();
    }

    /**
     * 判断玩家是否被宣战
     *
     * @return
     */
    public boolean isWarTarget() {
        // 检查所有战争卡,是否有被作为目标的,如果有,则玩家被宣战中
        for (TTACard c : this.getAllPlayedCard()) {
            if (c.cardType == CardType.WAR && ((AttackCard) c).target == this) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断玩家当前人口的状态,是否在暴动的临界点
     *
     * @return
     */
    public boolean isWillUprising() {
        if (this.tokenPool.getUnhappyWorkers() > 0) {
            if (this.tokenPool.getUnhappyWorkers() >= this.tokenPool.getUnusedWorkers()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加/移除 卡牌时处理的事件
     *
     * @param cardAdd
     * @param cardRemove
     * @param usedma
     * @param usedca
     */

    private TTAProperty onCardChange(TTACard cardAdd, TTACard cardRemove, int usedca, int usedma) {
        TTAProperty propertyChange = new TTAProperty();
        // 将添加的牌的能力添加到玩家的能力管理器中
        if (cardAdd != null) {
            this.abilityManager.addCardAbilities(cardAdd);
            for (CivilCardAbility a : cardAdd.abilities) {
                if (a.abilityType == CivilAbilityType.PA_TEMPLATE_RESOURCE) {
                    // 添加其提供的临时资源(荷马等)
                    this.tempResManager.addTemplateResource(a);
                } else if (a.isFlag()) {
                    if (a.property != null) {
                        this.tempResManager.addAlternateTemplateResource(a);
                    }
                    this.getParams().setRoundParameter(a.abilityType, a.getLimit());
                } else if (a.abilityType == CivilAbilityType.PA_TESLA_ABILITY) {
                    this.getParams().setGameParameter(CivilAbilityType.PA_TESLA_ABILITY, true);
                }
            }
            if (cardAdd.activeAbility != null) {
                this.abilityManager.addActiveCard(cardAdd);
            }
        }
        // 将移除的牌的能力从玩家的能力管理器中移除
        if (cardRemove != null) {
            this.abilityManager.removeCardAbilities(cardRemove);
            for (CivilCardAbility a : cardRemove.abilities) {
                if (a.abilityType == CivilAbilityType.PA_TEMPLATE_RESOURCE) {
                    // 同时移除其提供的临时资源
                    this.tempResManager.removeTemplateResource(a);
                } else if (a.isFlag()) {
                    if (a.property != null) {
                        this.tempResManager.removeAlternateTemplateResource(a);
                    }
                    this.getParams().setRoundParameter(a.abilityType, 0);
                }
            }
            if (cardRemove.activeAbility != null) {
                this.abilityManager.removeActiveCard(cardRemove);
                if (cardRemove.activeAbility.activeStep == RoundStep.RESIGNED) {
                    this.willCard = cardRemove;
                }
            }
        }

        // 如果卡牌拥有调整行动点数的能力,则直接调整玩家当前的行动点数
        int ia = cardAdd == null ? 0 : cardAdd.property.getProperty(CivilizationProperty.CIVIL_ACTION);
        int ir = cardRemove == null ? 0 : cardRemove.property.getProperty(CivilizationProperty.CIVIL_ACTION);
        int i = ia - ir;
        if (i > 0) {
            // 如果i>0,则是添加点数,直接加上该点数
            propertyChange.addProperty(CivilizationProperty.CIVIL_ACTION, i);
        } else if (i < 0) {
            // 如果i<0,则是移除点数,则可以从已使用的点数中扣除该点
            i = Math.min(0, i + usedca); // i为扣除点数的数量,总是应该小于等于0
            propertyChange.addProperty(CivilizationProperty.CIVIL_ACTION, i);
        }
        ia = cardAdd == null ? 0 : cardAdd.property.getProperty(CivilizationProperty.MILITARY_ACTION);
        ir = cardRemove == null ? 0 : cardRemove.property.getProperty(CivilizationProperty.MILITARY_ACTION);
        i = ia - ir;
        if (i > 0) {
            // 如果i>0,则是添加点数,直接加上该点数
            propertyChange.addProperty(CivilizationProperty.MILITARY_ACTION, i);
        } else if (i < 0) {
            // 如果i<0,则是移除点数,则可以从已使用的点数中扣除该点
            i = Math.min(0, i + usedma); // i为扣除点数的数量,总是应该小于等于0
            propertyChange.addProperty(CivilizationProperty.MILITARY_ACTION, i);
        }

        // 如果卡牌拥有调整指示物的能力,则直接调整
        ia = cardAdd == null ? 0 : cardAdd.property.getProperty(CivilizationProperty.YELLOW_TOKEN);
        ir = cardRemove == null ? 0 : cardRemove.property.getProperty(CivilizationProperty.YELLOW_TOKEN);
        propertyChange.addProperty(CivilizationProperty.YELLOW_TOKEN, ia - ir);

        ia = cardAdd == null ? 0 : cardAdd.property.getProperty(CivilizationProperty.BLUE_TOKEN);
        ir = cardRemove == null ? 0 : cardRemove.property.getProperty(CivilizationProperty.BLUE_TOKEN);
        propertyChange.addProperty(CivilizationProperty.BLUE_TOKEN, ia - ir);

        return propertyChange;
    }

    /**
     * 支付指定数量的蓝色指示物,并将支付的蓝色指示物放回玩家的配件池
     *
     * @param cardSubType
     * @param num
     * @return
     */

    protected ProductionInfo payBlueToken(CardSubType cardSubType, int num) {
        ResourceTaker taker = new ResourceTaker(cardSubType);
        taker.takeResource(num);
        return taker.execute();
    }

    /**
     * 刷新军队提供的军事力奖励值
     */
    private void refreshMilitaryBonus(boolean defensive) {
        // 首先清空军力增加值
        this.properties.setPropertyBonus(CivilizationProperty.MILITARY, 0);
        // 如果玩家拥有无视战术牌效果的能力,则不计算任何附加能力
        if (!this.abilityManager.hasAbilitiy(CivilAbilityType.PA_IGNORE_TACTICS)) {
            if (this.getTactics() != null) {
                // 整理所有部队卡的数量
                Map<TechCard, Integer> units = new HashMap<>();
                for (TTACard c : this.getAllPlayedCard()) {
                    if (c.cardType == CardType.UNIT) {
                        units.put((TechCard) c, c.getAvailableCount());
                    }
                }
                // 得到战术卡组成的军队结果
                TacticsResult result = this.getTacticsResult(units);
                // 添加额外军力到玩家的军事力结果
                int militaryBonus = result.getTotalMilitaryBonus();
                if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_MEIRIN)) {
                    militaryBonus += (result.mainArmyNum + result.secondaryArmyNum) * this.getTactics().level;
                }
                this.properties.addPropertyBonus(CivilizationProperty.MILITARY, militaryBonus);

                // 检查玩家是否有获得额外军队军事力奖励的能力
                if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_ADDITIONAL_TACTICS_BONUS)) {
                    // 有则添加军队中最高的军事奖励值
                    this.properties.addPropertyBonus(CivilizationProperty.MILITARY, result.getBestArmyBonus());
                }
            }
        }
        if (defensive) {
            int defensiveMilitary = this.properties.getProperty(CivilizationProperty.DEFENSIVE_MILITARY);
            this.properties.addPropertyBonus(CivilizationProperty.MILITARY, defensiveMilitary);
        }
    }

    /**
     * 刷新玩家的属性值
     *
     * @return
     */
    public void refreshProperties() {
        properties.clear();

        for (TTACard card : this.getAllPlayedCard()) {
            // 1.清空奖励
            card.property.clearAllBonus();

            // 2.计算叠加牌能力(荷马,孙子等)
            if (card.getAttachedCards() != null) {
                for (TTACard ac : card.getAttachedCards()) {
                    for (CivilCardAbility ca : ac.abilities) {
                        if (ca.getAbilityType() == CivilAbilityType.ATTACH_PROPERTY) {
                            card.property.addBonusProperties(ca.property, ca.getAvailableNumber(this));
                        }
                    }
                }
            }

            // 3.计算调整已有牌单张属性的能力(老版大汗,大教堂)
            for (CivilCardAbility ca : abilityManager.getAbilitiesByType(CivilAbilityType.ADJUST_UNIT_PROPERTY)) {
                if (ca.test(card)) {
                    card.property.addBonusProperties(ca.property, ca.getAvailableNumber(BgUtils.toList(card)));
                }
            }

            // 4.累加所有属性到玩家属性
            properties.addProperties(card.property, card.getAvailableCount());
        }

        // 计算所有因自己其他卡牌而调整玩家属性的能力(爱因斯坦,莎士比亚,拿破仑,帕特农神庙,等等)
        for (CivilCardAbility ca : abilityManager.getAbilitiesByType(CivilAbilityType.ADJUST_PROPERTY)) {
            properties.addProperties(ca.property, ca.getAvailableNumber(this));
        }

        // 计算所有按照其他玩家的属性调整玩家属性的能力
        Map<CivilCardAbility, PactCard> abilities = this.abilityManager
                .getPactAbilitiesWithRelation(CivilAbilityType.ADJUST_PROPERTY_BY_ALIAN);
        for (CivilCardAbility ca : abilities.keySet()) {
            properties.addProperties(ca.property, ca.getAvailableNumber(abilities.get(ca).alian));
        }

        // 计算幸福度
        int happiness = this.getProperty(CivilizationProperty.HAPPY_FACE);
        happiness -= this.getProperty(CivilizationProperty.UNHAPPY_FACE);
        properties.setProperty(CivilizationProperty.HAPPINESS, happiness);

        // 调整玩家的不满意工人数
        this.checkUnhappyWorkers();

        // 结算额外的战术牌效果和军事力奖励值
        this.refreshMilitaryBonus(true);
    }

    /**
     * 玩家移除牌
     *
     * @param card
     * @return
     */

    public TTAProperty removeCard(TTACard card) {
        TTAProperty res = new TTAProperty();
        int usedca = properties.getProperty(CivilizationProperty.CIVIL_ACTION) - this.getAvailableCivilAction();
        int usedma = properties.getProperty(CivilizationProperty.MILITARY_ACTION) - this.getAvailableMilitaryAction();
        switch (card.cardType) {
            case PRODUCTION:
            case BUILDING:
            case UNIT:
            case WONDER:
            case SPECIAL:
            case EVENT: // EVENT中只可能出现领土牌
                // 将该卡牌从玩家的已打出建筑牌堆中移除
                buildings.removeCard(card);
                break;
            case GOVERMENT:
                // 如果卡牌是政府牌,则只有是当前政府时,才会移除该政府
                if (this.goverment == card) {
                    // 将当前政府添加到过去政府列表中
                    this.goverment = null;
                } else {
                    return null;
                }
                break;
            case LEADER:
                // 如果卡牌是领袖牌,则只有是当前领袖时,才会移除该领袖
                if (this.leader == card) {
                    this.leader = null;
                } else {
                    return null;
                }
                break;
            case TACTICS:
                // 如果卡牌是战术牌,则只有是当前战术时,才会移除该战术
                if (this.tactics == card) {
                    this.tactics = null;
                } else {
                    return null;
                }
                break;
            case WAR:
                // 如果是战争,则检查是否当前玩家打出的战争,如果是则将其置空
                if (this.war == card) {
                    this.war = null;
                }
                buildings.removeCard(card);
                break;
            case PACT:
                // 如果是条约,则检查是否当前玩家打出的条约,如果是则将其置空
                if (this.pact == card) {
                    this.pact = null;
                }
                buildings.removeCard(card);
                break;
            default:
                break;
        }
        // 处理添加卡牌时的事件
        res = this.onCardChange(null, card, usedca, usedma);
        // 刷新属性

        return res;
    }

    /**
     * 玩家失去手牌
     *
     * @param cards
     */
    public void removeHand(List<TTACard> cards) {
        for (TTACard card : cards) {
            if (card.actionType == ActionType.CIVIL) {
                this.civilHands.removeCard(card);
            } else {
                this.militaryHands.removeCard(card);
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.gameRank = null;
        this.tokenPool = new TokenPool();
        this.abilityManager.clear();
        this.tempResManager.clear();
        this.civilHands.clear();
        this.militaryHands.clear();
        this.properties.clear();
        this.roundTempParam.clear();
        this.points.clear();
        this.buildings.clear();
        this.goverment = null;
        this.leader = null;
        this.uncompleteWonder = null;
        this.tactics = null;
        this.war = null;
        this.pact = null;
        this.resigned = false;
        this.ageDummyCard = new TTACard();
    }

    /**
     * 重置玩家的行动点
     */
    public void resetActionPoint() {
        this.goverment.setWhites(this.getProperty(CivilizationProperty.CIVIL_ACTION));
        this.goverment.setReds(this.getProperty(CivilizationProperty.MILITARY_ACTION));
    }

    /**
     * @return
     */
    public void resetRoundFlags() {
        for (CivilCardAbility ca : abilityManager.getAllFlaggedAbilities()) {
            this.getParams().setRoundParameter(ca.abilityType, ca.getLimit());
        }
    }

    /**
     * 按照当前文明取得文明点数
     *
     * @return
     */
    public int scoreCulturePoint() {
        int res = this.properties.getProperty(CivilizationProperty.CULTURE);
        if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_HUBATIAN)) {
            res *= 4;
        }
        return res;
    }

    /**
     * 按照当前科技取得科技点数
     *
     * @return
     */
    public int scoreSciencePoint() {
        return this.properties.getProperty(CivilizationProperty.SCIENCE);
    }

    /**
     * 设置玩家已经拿过指定等级领袖的参数
     *
     * @param level
     */
    public void setHasLeader(int level) {
        // 设置该时代leader已经拿过的参数
        this.getParams().setGameParameter(PARAM_LEADER + level, true);
    }

    /**
     * @param limit
     * @return
     */
    public boolean testRank(CivilizationProperty property, boolean weekest, int limit) {
        int rank = this.getRank(property, weekest);
        if (limit == 2 && this.getRealPlayerNumber() == 2) {
            limit = 1;
        }
        return rank <= limit;
    }

    /**
     * 玩家升级建筑/部队
     *
     * @param fromCard
     * @param toCard
     * @return 返回所有状态变化过的卡牌列表
     */
    public void upgrade(CivilCard fromCard, CivilCard toCard) {
        fromCard.addWorkers(-1);
        toCard.addWorkers(1);
    }

    /**
     * 提取资源和粮食用的类
     *
     * @author F14eagle
     */
    class ResourceTaker {
        List<ResourceCounter> cs;
        private CivilizationProperty property;

        ResourceTaker(CardSubType cardSubType) {
            if (cardSubType == CardSubType.FARM) {
                this.property = CivilizationProperty.FOOD;
            } else if (cardSubType == CardSubType.MINE) {
                this.property = CivilizationProperty.RESOURCE;
            }
            cs = new ArrayList<>();
            for (TTACard c : TTAPlayer.this.getBuildings().getCards()) {
                if (c.property.getProperty(this.property) > 0) {
                    cs.add(new ResourceCounter((CivilCard) c));
                }
            }
            Collections.sort(cs);
        }

        /**
         * 实际操作配件数量和情况,并返回所有变化过的卡牌列表
         *
         * @return
         */
        ProductionInfo execute() {
            ProductionInfo res = new ProductionInfo(this.property);
            // 先处理支付资源的情况
            for (ResourceCounter c : this.cs) {
                if (c.payNum > 0) {
                    // 将卡牌上的蓝色指示物移除,并添加到资源池
                    c.card.addBlues(-c.payNum);
                    TTAPlayer.this.tokenPool.putAvailableBlues(c.payNum);
                }
            }
            // 再处理找零的情况,从大找到小
            for (int i = this.cs.size() - 1; i >= 0; i--) {
                ResourceCounter c = this.cs.get(i);
                if (c.retNum > 0) {
                    // 从资源池取得蓝色指示物,并添加到卡牌上
                    int num = TTAPlayer.this.tokenPool.takeAvailableBlues(c.retNum);
                    c.card.addBlues(num);
                    res.put(c.card, num - c.payNum);
                } else {
                    res.put(c.card, -c.payNum);
                }
            }
            return res;
        }

        /**
         * 取得拿取资源后,将返回资源库的蓝色指示物个数
         *
         * @return
         */
        int getReturnedBlues() {
            int res = 0;
            // 先处理支付资源的情况
            for (ResourceCounter c : this.cs) {
                if (c.payNum > 0) {
                    // 返回资源库的蓝色指示物
                    res += c.payNum;
                }
            }
            // 再处理找零的情况,从大找到小
            for (int i = this.cs.size() - 1; i >= 0; i--) {
                ResourceCounter c = this.cs.get(i);
                if (c.retNum > 0) {
                    // 由于找零而拿回的蓝色指示物
                    res -= c.retNum;
                }
            }
            return res;
        }

        /**
         * 按照算法得到资源
         *
         * @param num
         */
        void putResource(int num) {
            // 处理特斯拉死亡后不能在实验室上添加蓝点（可以减少）
            if (TTAPlayer.this.getParams().getBoolean(CivilAbilityType.PA_TESLA_ABILITY)
                    && !TTAPlayer.this.abilityManager.hasAbilitiy(CivilAbilityType.PA_TESLA_ABILITY)) {
                cs.removeIf(rc -> rc.card.cardSubType == CardSubType.LAB);
            }
            int i = this.cs.size() - 1;
            int offset = -1;
            int rest = num;
            while (rest != 0) {
                ResourceCounter c = this.cs.get(i);
                int take = rest / c.value;
                if (take > 0) {
                    c.retNum += take;
                    rest -= take * c.value;
                }
                i += offset;
                // 越界时跳出循环
                if (i < 0 || i >= cs.size()) {
                    break;
                }
            }
        }

        /**
         * 按照算法拿取资源
         *
         * @param num
         */
        void takeResource(int num) {
            int i = 0;
            int offset = 1;
            int rest = num;
            // 循环计算使用资源的数量
            while (rest != 0) {
                ResourceCounter c = this.cs.get(i);
                if (rest > 0) {
                    // 拿取资源的逻辑
                    int take = (int) Math.ceil((double) rest / (double) c.value);
                    take = Math.min(take, c.availableNum);
                    c.payNum += take;
                    rest -= take * c.value;
                } else {
                    // 找零的逻辑
                    int take = -rest / c.value;
                    if (take > 0) {
                        take = Math.min(take, c.payNum);
                        c.payNum -= take;
                        rest += take * c.value;
                    }
                }
                if (rest > 0) {
                    // 如果还是不够资源,则继续检查值大的资源
                    offset = 1;
                } else {
                    // 如果需要找零,则检查值小的资源
                    offset = -1;
                }
                i += offset;
                // 越界时跳出循环
                if (i < 0 || i >= cs.size()) {
                    break;
                }
            }
            if (rest < 0) {
                // 需要处理找零的情况
                this.putResource(-rest);
            }
        }


        @Override
        public String toString() {
            String str = "";
            for (ResourceCounter c : this.cs) {
                str += c.value + " 可用:" + c.availableNum + " 使用:" + c.payNum + " 找回:" + c.retNum + "\n";
            }
            return str;
        }

        class ResourceCounter implements Comparable<ResourceCounter> {
            CivilCard card;
            /**
             * 每个单位表示的值
             */
            int value;
            /**
             * 可用的数量
             */
            int availableNum;
            /**
             * 使用的数量
             */
            int payNum;
            /**
             * 找回的数量
             */
            int retNum;

            ResourceCounter(CivilCard card) {
                this.card = card;
                this.value = this.card.property.getProperty(ResourceTaker.this.property);
                this.availableNum = this.card.getBlues();
            }

            @Override
            public int compareTo(ResourceCounter o) {
                if (value > o.value) {
                    return 1;
                } else if (value < o.value) {
                    return -1;
                } else if (card.level < o.card.level) {
                    return 1;
                } else if (card.level > o.card.level) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

}
