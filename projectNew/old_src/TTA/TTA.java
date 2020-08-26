package com.f14.TTA;

import com.f14.F14bg.network.CmdFactory;
import com.f14.TTA.component.CardBoard;
import com.f14.TTA.component.CardBoard.EventResult;
import com.f14.TTA.component.ProductionInfo;
import com.f14.TTA.component.TTAProperty;
import com.f14.TTA.component.ability.CardAbility;
import com.f14.TTA.component.ability.CivilCardAbility;
import com.f14.TTA.component.card.*;
import com.f14.TTA.component.param.CostParam;
import com.f14.TTA.consts.*;
import com.f14.TTA.factory.TTACmdFactory;
import com.f14.TTA.listener.TTAConfirmListener;
import com.f14.TTA.manager.TTAConstManager;
import com.f14.bg.FixedOrderBoardGame;
import com.f14.bg.action.BgResponse;
import com.f14.bg.component.Card;
import com.f14.bg.consts.BgVersion;
import com.f14.bg.consts.TeamMode;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.player.Player;
import com.f14.bg.utils.BgUtils;
import com.f14.bg.utils.CheckUtils;
import com.f14.utils.CollectionUtils;
import net.sf.json.JSONObject;

import java.util.*;

/**
 * TTA的游戏
 *
 * @author F14eagle
 */
public class TTA extends FixedOrderBoardGame<TTAPlayer, TTAGameMode> {

    /**
     * 玩家可否离开（不影响游戏进行）
     *
     * @param player
     * @return
     */
    @Override
    public boolean canLeave(Player player) {
        TTAPlayer p = (TTAPlayer) player;
        return this.isPlayingGame(p) && p.resigned;
    }

    /**
     *
     */
    protected boolean checkPastCard(TTACard card, int pastAge) {
        if (card == null) {
            return false;
        }
        if (card.level <= pastAge) {
            return true;
        }
        if (card.activeAbility != null) {
            switch (card.activeAbility.abilityType) {
                case PA_HUBATIAN:
                case PA_NARODNI:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    /**
     * @param player
     * @return
     */
    protected boolean checkPlayerUprising(TTAPlayer player) {
        return this.getConfig().isUprising() && player.isUprising();
    }

    /**
     * 检查临时费用的使用情况
     *
     * @param player
     * @param card
     * @param cp
     * @param property
     */
    public void checkTemplateResource(TTAPlayer player, TTACard card, CostParam cp, CivilizationProperty property) {
        if (cp.cost.getProperty(property) == 0) {
            return;
        }
        for (CardAbility a : player.tempResManager.getTempResAbility()) {
            if (a.test(card)) {
                // 如果该临时资源可以用在card上
                TTAProperty restp = player.tempResManager.getTempRes(a);
                TTAProperty used = new TTAProperty();
                // 取得该技能剩余的临时资源
                int restResource = restp.getProperty(property);
                used.addProperty(property, Math.min(cp.cost.getProperty(property), restResource));
                // 如果该技能的临时资源够付了,则支付对应的临时费用
                // 否则则支付所有的临时费用
                cp.useAbility(a, used);
                if (cp.cost.getProperty(property) == 0) {
                    // 如果已经不需支付任何费用了,则不再检查其他临时资源
                    return;
                }
            }
        }
        for (CivilCardAbility a : player.tempResManager.getAlternateTempResAbility()) {
            // 如果该临时资源可以用在card上
            if (a.test(card)) {
                TTAProperty restp = player.tempResManager.getAlternateTempRes(a);
                if (restp.getProperty(property) > 0) {
                    TTACard c = player.abilityManager.getAbilityCard(a.abilityType);
                    TTAConfirmListener listener = new TTAConfirmListener(player,
                            "你是否使用" + c.getReportString() + "的能力?");
                    listener.addListeningPlayer(player);
                    InterruptParam res;
                    try {
                        player.checkEnoughReource(restp, -1);
                        res = gameMode.insertListener(listener);
                        if (res.getBoolean(player.getPosition())) {
                            cp.useAbility(a, restp);
                        }
                    } catch (BoardGameException ignored) {
                    }
                    if (cp.cost.getProperty(property) == 0) {
                        // 如果已经不需支付任何费用了,则不再检查其他临时资源
                        return;
                    }
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    protected TTAConfig createConfig(JSONObject object) throws BoardGameException {
        TTAConfig config = new TTAConfig();
        config.versions = new LinkedHashSet<>(); // TTA版本要按顺序依次添加
        config.ageCount = TTAConsts.AGE_COUNT;// object.getInt("ageLimit");
        config.mode = TTAMode.valueOf(object.getString("mode"));
        config.corruption = true;
        config.uprising = true;
        config.darkAge = true;
        config.teamMatch = object.getBoolean("teamMatch");
        try {
            config.teamMode = TeamMode.valueOf(object.getString("teamMode"));
        } catch (Exception e) {
            config.teamMode = TeamMode.RANDOM;
        }
        // 总是需要打乱座位顺序
        try {
            config.randomSeat = object.getBoolean("randomSeat");
        } catch (Exception e) {
            config.randomSeat = true;
        }
        try {
            config.revoltDraw = object.getBoolean("revoltDraw");
        } catch (Exception e) {
            config.revoltDraw = false;
        }
        try {
            config.expansionUsed = object.getBoolean("expansionUsed");
        } catch (Exception e) {
            config.expansionUsed = false;
        }
        try {
            config.version2 = object.getBoolean("newAgeUsed");
        } catch (Exception e) {
            config.version2 = false;
        }
        try {
            config.touhouUsed = object.getBoolean("touhouUsed");
        } catch (Exception e) {
            config.touhouUsed = false;
        }
        try {
            config.expansionCN = object.getBoolean("expansionCN");
        } catch (Exception e) {
            config.expansionCN = false;
        }
        try {
            config.expansion14 = object.getBoolean("expansion14");
        } catch (Exception e) {
            config.expansion14 = false;
        }
        try {
            config.noLimit = object.getBoolean("noLimit");
        } catch (Exception e) {
            config.noLimit = false;
        }
        try {
            config.hideAvalable = object.getBoolean("hideAvalable");
        } catch (Exception e) {
            config.hideAvalable = false;
        }
        try {
            config.lazyMemory = object.getBoolean("lazyMemory");
        } catch (Exception e) {
            config.lazyMemory = false;
        }
        try {
            config.balanced22 = object.getBoolean("balanced22");
        } catch (Exception e) {
            config.balanced22 = false;
        }

        if (!config.version2) {
            // 原版
            config.versions.add("BASE");
            if (config.expansionUsed) {
                config.versions.add("BGO");
            }
            if (config.touhouUsed) {
                config.versions.add("TH");
            }
            if (config.expansionCN) {
                config.versions.add("CN");
            }
            if (config.expansion14) {
                config.versions.add("F14");
            }
        } else {
            // 新版
            config.versions.add("V2BASE");
            if (config.expansionCN) {
                config.versions.add("CN");
            }
            if (config.expansion14) {
                config.versions.add("F14");
            }
        }
        return config;
    }

    /**
     * 作弊代码
     *
     * @param msg
     * @return
     * @throws BoardGameException
     */
    @Override
    public boolean doCheat(String msg) throws BoardGameException {
        if (super.doCheat(msg)) {
            return true;
        }
        String[] s = msg.split(" ");
        switch (s[0]) {
            case "top":
                CheckUtils.check(!gameMode.cardBoard.topCard(s[1]), "Card not found!");
                return true;
            case "top%":
                CheckUtils.check(!gameMode.cardBoard.topCardByNo(s[1]), "Card not found!");
                return true;
            case "add_hand": {
                Integer pos = new Integer(s[1]);
                TTAPlayer p = this.getPlayer(pos);
                for (int i = 2; i < s.length; ++i) {
                    TTACard card = gameMode.cardBoard.getTempCardByNo(s[i]);
                    if (card.cardType == CardType.WONDER) {
                        this.playerGetWonder(p, (WonderCard) card);
                    } else {
                        this.playerAddHand(p, card);
                    }
                }
            }
            return true;
            case "add_board": {
                Integer pos = new Integer(s[1]);
                TTAPlayer p = this.getPlayer(pos);
                for (int i = 2; i < s.length; ++i) {
                    TTACard card = gameMode.cardBoard.getTempCardByNo(s[i]);
                    this.playerAddCardDirect(p, card);
                    this.getReport().playerAddCard(p, card);
                }
                return true;
            }
            case "next_event": {
                TTACard card = gameMode.cardBoard.getTempCardByNo(s[1]);
                gameMode.cardBoard.addNextEvent(card);
            }
            return true;
            case "score": {
                Integer pos = new Integer(s[1]);
                TTAPlayer p = this.getPlayer(pos);
                TTAProperty pro = new TTAProperty();
                int i = 2;
                while (i < s.length) {
                    String type = s[i++].toLowerCase();
                    int num;
                    try {
                        num = new Integer(s[i++]);
                    } catch (Exception e) {
                        continue;
                    }
                    switch (type) {
                        case "cp":
                        case "culture":
                            pro.setProperty(CivilizationProperty.CULTURE, num);
                            break;
                        case "sp":
                        case "science":
                            pro.setProperty(CivilizationProperty.SCIENCE, num);
                            break;
                        case "rp":
                        case "resource":
                            pro.setProperty(CivilizationProperty.RESOURCE, num);
                            break;
                        case "fp":
                        case "food":
                            pro.setProperty(CivilizationProperty.FOOD, num);
                            break;
                        case "yt":
                        case "yellow":
                            pro.setProperty(CivilizationProperty.YELLOW_TOKEN, num);
                            break;
                        case "bt":
                        case "blue":
                            pro.setProperty(CivilizationProperty.BLUE_TOKEN, num);
                            break;
                        case "ca":
                        case "civil":
                            pro.setProperty(CivilizationProperty.CIVIL_ACTION, num);
                            break;
                        case "ma":
                        case "military":
                            pro.setProperty(CivilizationProperty.MILITARY_ACTION, num);
                            break;
                        case "popinc":
                            this.playerIncreasePopulation(p, num);
                            break;
                        case "popdec":
                            this.playerDecreasePopulation(p, num);
                            break;
                        default:
                            break;
                        // throw new BoardGameException("Score type not supported!");
                    }
                }
                this.playerAddPoint(p, pro);
                this.playerAddToken(p, pro);
                this.playerAddAction(p, pro);
                this.getReport().printCache(p);
            }
            return true;
            default:
                return false;
        }
    }

    /**
     * 处理临时资源的使用情况
     *
     * @param cp
     */
    public void executeTemplateResource(TTAPlayer player, CostParam cp) {
        for (CardAbility a : cp.usedAbilities.keySet()) {
            TTAProperty p = cp.usedAbilities.get(a);
            if (player.tempResManager.getTempRes(a) != null) {
                player.tempResManager.useTemplateResource(a, p);
                this.getReport().playerExecuteTemporaryResource(player, p);
            } else {
                player.tempResManager.useAlternateTemplateResource(a, p);
                this.getReport().playerActiveCardCache(player, ((CivilCardAbility) a).abilityType);
            }
        }
    }

    /**
     * 玩家体面退出游戏
     *
     * @param player
     */
    public void forceResign(TTAPlayer player) {
        gameMode.resignedPlayerPosition.put(player, gameMode.getResignedPlayerNumber());
        player.resigned = true;
        gameMode.gameRank.removePlayer(player);
    }


    @Override
    public TTAConfig getConfig() {
        return (TTAConfig) super.config;
    }

    /**
     * 获取游戏模式
     */
    public TTAGameMode getGameMode() {
        return this.gameMode;
    }

    /**
     * 还在游戏的玩家数量（除去体退的）
     *
     * @return
     */
    public int getRealPlayerNumber() {
        return gameMode.gameRank.getPlayerNumber();
    }


    @Override
    public TTAReport getReport() {
        return (TTAReport) super.getReport();
    }

    public int getRequiredPlayerNumber() {
        if (this.isTeamMatch()) {
            return 2;
        } else {
            return 1;
        }
    }


    @Override
    public TTAPlayer getStartPlayer() {
        TTAPlayer p = super.getStartPlayer();
        while (p.resigned) {
            p = getNextPlayer(p);
        }
        return p;
    }

    @Override
    public void initConfig() {
        TTAConfig config = new TTAConfig();
        config.versions.add(BgVersion.BASE);
        config.ageCount = TTAConsts.AGE_COUNT;
        config.corruption = true;
        config.uprising = true;
        config.darkAge = true;
        config.mode = TTAMode.FULL;
        config.teamMatch = false;
        config.teamMode = TeamMode.RANDOM;
        config.randomSeat = true;
        config.revoltDraw = false;
        config.bonusCardFlag = false;
        config.expansionUsed = false;
        config.version2 = false;
        config.touhouUsed = false;
        config.expansionCN = false;
        config.expansion14 = false;
        config.noLimit = false;
        config.hideAvalable = false;
        config.lazyMemory = false;
        config.balanced22 = false;
        this.config = config;
    }

    @Override
    public void initConst() {
    }

    /**
     * 初始化玩家的座位信息
     */
    @Override
    protected void initPlayersSeat() {
        if (!this.getConfig().randomSeat) {
            // 如果不是随机座位
            // 设置座位号
            for (int i = 0; i < this.getPlayers().size(); i++) {
                this.getPlayers().get(i).setPosition(i);
            }
            // 设置起始玩家和当前玩家
            this.startPlayer = this.getPlayer(0);
            this.setCurrentPlayer(this.startPlayer);
        } else if (this.isTeamMatch() && this.getConfig().teamMode == TeamMode.FIXED) {
            // 如果是team match, 并且选择了13 vs 24, 则需要按照特殊的规定排列位置
            synchronized (this.getPlayers()) {
                // 先设置好玩家的team
                for (TTAPlayer p : this.getPlayers()) {
                    p.setTeam(p.getPosition() % 2);
                }
                // 打乱玩家的顺序
                List<TTAPlayer> players = new ArrayList<>(this.getPlayers());
                CollectionUtils.shuffle(players);
                this.getPlayers().clear();
                this.getPlayers().add(players.remove(0));
                int lastTeam = this.getPlayers().get(0).getTeam();
                while (!players.isEmpty()) {
                    for (TTAPlayer o : players) {
                        if (o.getTeam() != lastTeam) {
                            this.getPlayers().add(o);
                            players.remove(o);
                            lastTeam = o.getTeam();
                            break;
                        }
                    }
                }
                // 设置座位号
                for (int i = 0; i < this.getPlayers().size(); i++) {
                    this.getPlayers().get(i).setPosition(i);
                }
                // 设置起始玩家和当前玩家
                this.startPlayer = this.getPlayer(0);
                this.setCurrentPlayer(this.startPlayer);
            }
        } else {
            this.regroupPlayers();
        }
    }

    @Override
    protected void initPlayerTeams() {
        if (this.isTeamMatch()) {
            // 13 vs 24
            for (TTAPlayer p : this.getPlayers()) {
                p.setTeam(p.getPosition() % 2);
            }
        } else if (this.isTichuMode()) {
        } else {
            super.initPlayerTeams();
        }
    }

    @Override
    public void initReport() {
        super.report = new TTAReport(this);
    }

    @Override
    public boolean isTeamMatch() {
        // 必须要4人游戏才会是组队赛
        return this.getPlayers().size() == 4 && super.isTeamMatch();
    }

    public boolean isTichuMode() {
        // 必须要新版3人游戏才会是地主模式
        return this.getPlayers().size() == 3 && this.getConfig().isNewAgeUsed() && super.isTeamMatch();
    }

    /**
     * 新版检测
     */
    public boolean isVersion2() {
        return ((TTAConfig) this.config).version2;
    }

    /**
     * 玩家调整CA/MA
     *
     * @param player
     * @param property
     */
    public void playerAddAction(TTAPlayer player, TTAProperty property) {
        int num = property.getProperty(CivilizationProperty.CIVIL_ACTION);
        if (num != 0) {
            this.playerAddCivilAction(player, num);
            this.getReport().playerAddCivilAction(player, num);
        }
        num = property.getProperty(CivilizationProperty.MILITARY_ACTION);
        if (num != 0) {
            this.playerAddMilitaryAction(player, num);
            this.getReport().playerAddMilitaryAction(player, num);
        }
    }

    /**
     * 玩家打出手牌到面板
     *
     * @param player
     * @param card
     */
    public void playerAddCard(TTAPlayer player, TTACard card) {
        this.playerRemoveHand(player, card);
        this.playerAddCardDirect(player, card);
    }

    /**
     * 玩家直接打出牌,包括 领袖/政府/各种科技/殖民地/战术牌
     *
     * @param player
     * @param card
     */
    public void playerAddCardDirect(TTAPlayer player, TTACard card) {
        TTAProperty result = player.addCard(card);
        this.sendPlayerAddCardResponse(player, card, null);
        this.playerAddToken(player, result);
        this.playerAddAction(player, result);
        this.playerRefreshProperty(player);
        // 检查打出的牌是否拥有可以使用的能力,如果有则刷新玩家的可使用能力列表
        if (card.activeAbility != null) {
            // 暂时只可能在NORMAL阶段触发该方法
            this.sendPlayerActivableCards(RoundStep.NORMAL, player);
        }
        // 玩家卡牌变化时,刷新所有与玩家有关联属性能力的玩家属性
        this.refreshRelationPlayerProperty(player, card);
    }

    /**
     * 玩家调整内政行动点数
     *
     * @param player
     * @param num
     */
    public void playerAddCivilAction(TTAPlayer player, int num) {
        player.addCivilAction(num);
        this.sendPlayerCardToken(player, player.getGoverment(), null);
    }

    /**
     * 玩家文明点数调整
     *
     * @param player
     * @param num
     * @return 返回调整后与调整前的文明点数差值
     */
    public int playerAddCulturePoint(TTAPlayer player, int num) {
        int res = -player.getCulturePoint();
        player.addCulturePoint(num);
        res += player.getCulturePoint();
        if (res != 0) {
            this.sendPlayerCivilizationInfo(player, null);
        }
        return res;
    }

    /**
     * 玩家添加事件牌
     *
     * @param player
     * @param card   添加的事件牌
     * @return 返回触发的事件牌
     * @throws BoardGameException
     */
    public EventCard playerAddEvent(TTAPlayer player, EventCard card) throws BoardGameException {
        // 将事件和殖民地牌埋入未来事件牌堆,玩家得到事件牌对应的分数,
        // 并翻开当前事件牌堆的第一张牌,处理该事件
        EventResult res = gameMode.getCardBoard().addEvent(player, card);
        EventCard currCard = res.card;
        this.playerRemoveHand(player, card);
        int culturePoint = card.level;
        for (CivilCardAbility ca : player.abilityManager.getAbilitiesByType(CivilAbilityType.ADJUST_EVENT_POINT)) {
            if (ca.test(card)) {
                culturePoint += ca.getAmount();
            }
        }
        this.playerAddCulturePoint(player, culturePoint);
        this.getReport().playerAddCulturePoint(player, culturePoint);
        this.getReport().playerAddEvent(player, card, currCard);

        // 刷新事件牌堆
        this.sendBaseInfo(null);
        this.sendAddEventResponse(player, card, false, null);
        this.sendRemoveEventResponse(res.player, currCard, res.futureToCurrent, null);
        this.sendAddPastEventResponse(res.card, null);
        return currCard;
    }

    /**
     * 玩家食物调整
     *
     * @param player
     * @param num
     * @return 返回调整后与调整前的食物差值
     */
    public int playerAddFood(TTAPlayer player, int num) {
        if (num != 0) {
            ProductionInfo info = player.addFood(num);
            this.sendPlayerCardToken(player, info.keySet(), null);
            this.sendPlayerBoardTokens(player, null);
            return info.getTotalValue();
        } else {
            return 0;
        }
    }

    /**
     * 玩家得到手牌
     *
     * @param player
     * @param cards
     */
    public void playerAddHand(TTAPlayer player, List<TTACard> cards) {
        player.addHand(cards);
        this.sendPlayerAddHandResponse(player, cards, null);
    }

    /**
     * 玩家得到手牌
     *
     * @param player
     * @param card
     */
    public void playerAddHand(TTAPlayer player, TTACard card) {
        this.playerAddHand(player, BgUtils.toList(card));
    }

    public void playerAddjustCivilAction(TTAPlayer player, Integer num) {
        if (num < 0) {
            this.playerAddCivilAction(player, num);
            this.getReport().playerAddCivilAction(player, num);
            this.getReport().printCache(player);
            this.sendAlert(player, "你失去" + -num + "个内政行动点");
        }
    }

    /**
     * 玩家调整军事行动点数
     *
     * @param player
     * @param num
     */
    public void playerAddMilitaryAction(TTAPlayer player, int num) {
        player.addMilitaryAction(num);
        this.sendPlayerCardToken(player, player.getGoverment(), null);
    }

    /**
     * 玩家按照property中的资源/食物/文明点数/科技点数调整对应的数值
     *
     * @param player
     * @param property
     * @return 返回调整后与调整前的属性差额
     */

    public TTAProperty playerAddPoint(TTAPlayer player, TTAProperty property) {
        TTAProperty res = this.playerAddPoint(player, property, 1);
        this.getReport().playerAddPoint(player, res);
        return res;
    }

    /**
     * 玩家按照property中的资源/食物/文明点数/科技点数调整对应的数值
     *
     * @param player
     * @param property
     * @param multi    倍数
     * @return 返回调整后与调整前的属性差额
     */

    public TTAProperty playerAddPoint(TTAPlayer player, TTAProperty property, int multi) {
        TTAProperty res = new TTAProperty();
        int p = property.getProperty(CivilizationProperty.SCIENCE);
        if (p != 0) {
            int d = this.playerAddSciencePoint(player, p * multi);
            res.setProperty(CivilizationProperty.SCIENCE, d);
        }
        p = property.getProperty(CivilizationProperty.CULTURE);
        if (p != 0) {
            int d = this.playerAddCulturePoint(player, p * multi);
            res.setProperty(CivilizationProperty.CULTURE, d);
        }
        p = property.getProperty(CivilizationProperty.FOOD);
        if (p != 0) {
            int d = this.playerAddFood(player, p * multi);
            res.setProperty(CivilizationProperty.FOOD, d);
        }
        p = property.getProperty(CivilizationProperty.RESOURCE);
        if (p != 0) {
            int d = this.playerAddResource(player, p * multi);
            res.setProperty(CivilizationProperty.RESOURCE, d);
        }
        return res;
    }

    /**
     * 玩家资源调整
     *
     * @param player
     * @param num
     * @return 返回调整后与调整前的资源差值
     */
    public int playerAddResource(TTAPlayer player, int num) {
        if (num != 0) {
            ProductionInfo info = player.addResource(num);
            this.sendPlayerCardToken(player, info.keySet(), null);
            this.sendPlayerBoardTokens(player, null);
            return info.getTotalValue();
        } else {
            return 0;
        }
    }

    /**
     * 玩家科技点数调整
     *
     * @param player
     * @param num
     * @return 调整后与调整前的科技差值
     */
    public int playerAddSciencePoint(TTAPlayer player, int num) {
        int res = -player.getSciencePoint();
        player.addSciencePoint(num);
        res += player.getSciencePoint();
        if (res != 0) {
            this.sendPlayerCivilizationInfo(player, null);
        }
        return res;
    }

    /**
     * 玩家按照property中的CA/MA/黄色标志物/蓝色标志物调整对应的数值
     *
     * @param player
     * @param property
     * @return 返回实际调整的数量
     */

    public TTAProperty playerAddToken(TTAPlayer player, TTAProperty property) {
        return this.playerAddToken(player, property, 1);
    }

    /**
     * 玩家按照property中的黄色标志物/蓝色标志物调整对应的数值
     *
     * @param player
     * @param property
     * @param multi    倍数
     * @return 返回实际调整的数量
     */

    public TTAProperty playerAddToken(TTAPlayer player, TTAProperty property, int multi) {
        TTAProperty res = new TTAProperty();
        int p = property.getProperty(CivilizationProperty.YELLOW_TOKEN);
        if (p != 0) {
            int d = player.addAvailableWorker(p * multi);
            this.getReport().playerAddYellowToken(player, p * multi);
            res.setProperty(CivilizationProperty.YELLOW_TOKEN, d);
        }
        p = property.getProperty(CivilizationProperty.BLUE_TOKEN);
        if (p != 0) {
            int d = player.tokenPool.addAvailableBlues(p * multi);
            this.getReport().playerAddBlueToken(player, p * multi);
            res.setProperty(CivilizationProperty.BLUE_TOKEN, d);
        }
        // 向所有玩家发送玩家的标志物信息
        this.sendPlayerBoardTokens(player, null);
        return res;
    }

    /**
     * 玩家将一张牌埋在目标牌下
     *
     * @param player
     * @param card
     * @param destCard 目标牌
     */
    public void playerAttachCard(TTAPlayer player, TTACard card, TTACard destCard) {
        if (destCard.getAttachedCards() == null) {
            destCard.setAttachedCards(new ArrayList<>(0));
        }
        destCard.getAttachedCards().add(card);
        this.gameMode.cardBoard.getAttachedCards().put(card, destCard);
        // 检测所有目标卡的玩家,刷新属性
        for (TTAPlayer p : gameMode.getRealPlayers()) {
            if (p.getAllPlayedCard().contains(destCard)) {
                this.playerRefreshProperty(p);
            }
        }
        this.sendPlayerAttachCardResponse(player, card, destCard, null);
        this.getReport().playerAttachCard(player, card, destCard);
    }

    /**
     * 玩家建造建筑,部队
     *
     * @param player
     * @param card
     */
    public void playerBuild(TTAPlayer player, CivilCard card) {
        player.build(card);
        this.playerRefreshProperty(player);
        this.sendPlayerCardToken(player, card, null);
    }

    /**
     * 检查城市建筑限制
     *
     * @param player
     * @param card
     * @throws BoardGameException
     */
    public void playerBuildLimitCheck(TTAPlayer player, CivilCard card) throws BoardGameException {
        if (card.cardType == CardType.BUILDING) {
            int limit = player.getGoverment().getBuildingLimit();
            switch (card.cardSubType) {
                case LAB:
                case LIBRARY:
                    if (gameMode.inquisitionPosition != -1) {
                        limit -= 1;
                    }
                default:
                    CheckUtils.check(player.getBuildingNumber(card.cardSubType) >= limit, "你现有的政府不能再建造更多这样的建筑了!");
                    break;
            }
        }
    }

    /**
     * 玩家建造奇迹
     *
     * @param player
     * @param step
     * @throws BoardGameException
     */
    public void playerBuildWonder(TTAPlayer player, int step) throws BoardGameException {
        WonderCard card = player.getUncompleteWonder();
        player.buildWonder(step);
        this.sendPlayerBoardTokens(player, null);
        this.sendPlayerCardToken(player, card, null);
    }

    /**
     * 玩家完成奇迹
     *
     * @param player
     */
    public void playerCompleteWonder(TTAPlayer player) {
        WonderCard card = player.getUncompleteWonder();
        TTAProperty result = player.completeWonder();

        // 如果奇迹已经建造完成,则发送玩家得到奇迹的消息
        this.sendPlayerWonderCompleteResponse(player);
        this.playerAddToken(player, result);
        this.playerAddAction(player, result);
        // 检查完成的奇迹是否会立即带来文明点数,如果有则直接加给玩家
        int cp = player.getScoreCulturePoint(card.scoreAbilities);
        this.playerAddCulturePoint(player, cp);
        this.getReport().playerAddCulturePoint(player, cp);
        // 检查该奇迹是否拥有可以使用的能力,如果有则刷新玩家的可使用能力列表
        if (card.activeAbility != null) {
            switch (card.activeAbility.abilityType) {
                case PA_MANHATTAN:
                    // 曼哈顿移除所有被宣言的战争
                    for (TTACard c : player.getPlayedCard(card.activeAbility)) {
                        AttackCard ac = (AttackCard) c;
                        if (ac.getTarget() == player) {
                            this.removeOvertimeCard(ac);
                        }
                    }
                    break;
                case PA_NARODNI:
                    // 国家图书馆消耗所有内政行动点,每个获得5文化分
                    int actionPoints = player.getAvailableActionPoint(ActionType.CIVIL);
                    actionPoints = Math.max(actionPoints, 0);
                    this.playerAddCulturePoint(player, actionPoints * 5);
                    this.playerAddCivilAction(player, -actionPoints);
                    this.getReport().playerAddCivilAction(player, -actionPoints);
                    break;
                default:
                    // 暂时只可能在NORMAL阶段建造奇迹
                    this.sendPlayerActivableCards(RoundStep.NORMAL, player);
                    break;
            }
        }
        this.sendPlayerCardToken(player, card, null);
        this.playerRefreshProperty(player);
        // 建造完成时,刷新所有与玩家有关联属性能力的玩家属性
        this.refreshRelationPlayerProperty(player, card);
    }

    /**
     * 执行粮食消耗
     *
     * @param player
     */
    protected void playerConsume(TTAPlayer player) {
        // 检查玩家的粮食供应
        CostParam cp = new CostParam();
        int consumption = player.getConsumption();
        int food = player.getTotalFood();
        cp.cost.addProperty(CivilizationProperty.FOOD, consumption);
        this.checkTemplateResource(player, null, cp, CivilizationProperty.FOOD);
        if (food < cp.cost.getProperty(CivilizationProperty.FOOD)) {
            // 每缺少一个食物扣4点文明点
            int point = 4 * (cp.cost.getProperty(CivilizationProperty.FOOD) - food);
            cp.cost.addProperty(CivilizationProperty.CULTURE, point);
        }
        // 扣除粮食
        this.playerAddPoint(player, cp.cost, -1);
        this.getReport().playerCostPoint(player, cp.cost, "消费");
        this.executeTemplateResource(player, cp);
    }

    /**
     * 执行腐败
     *
     * @param player
     */
    protected void playerCorrupt(TTAPlayer player) {
        // 可以设置全局忽略腐败
        if (this.getConfig().corruption) {
            CostParam cp = new CostParam();
            int corruption = TTAConstManager.getResourceCorruption(player.tokenPool.getAvailableBlues(), isVersion2());
            int totalResource = player.getTotalResource();
            if (this.isVersion2() && corruption > totalResource) {
                // 新版资源不够支付时扣食物,扣完为止
                int foodCost = corruption - totalResource;
                corruption = totalResource;
                cp.cost.addProperty(CivilizationProperty.FOOD, foodCost);
            }
            cp.cost.addProperty(CivilizationProperty.RESOURCE, corruption);
            this.checkTemplateResource(player, null, cp, CivilizationProperty.RESOURCE);
            this.playerAddPoint(player, cp.cost, -1);
            this.getReport().playerCostPoint(player, cp.cost, "腐败");
            this.executeTemplateResource(player, cp);
        }
    }

    /**
     * 玩家减少人口(减少的是空闲人口)
     *
     * @param player
     * @param num
     */
    public void playerDecreasePopulation(TTAPlayer player, int num) {
        int res = player.decreasePopulation(num);
        if (res != 0) {
            this.sendPlayerBoardTokens(player, null);
        }
    }

    /**
     * 玩家减少人口(拆除card并减少人口)
     *
     * @param player
     * @param detail
     */
    public void playerDecreasePopulation(TTAPlayer player, Map<TechCard, Integer> detail) {
        int res = this.playerDestroy(player, detail);
        this.playerDecreasePopulation(player, res);
    }

    /**
     * 玩家减少人口(拆除card并减少人口)
     *
     * @param player
     * @param card
     * @param num
     */
    public void playerDecreasePopulation(TTAPlayer player, TechCard card, int num) {
        int res = this.playerDestroy(player, card, num);
        this.playerDecreasePopulation(player, res);
    }

    /**
     * 玩家摧毁建筑,部队
     *
     * @param player
     * @param detail
     */
    public int playerDestroy(TTAPlayer player, Map<TechCard, Integer> detail) {
        int res = 0;
        for (TechCard c : detail.keySet()) {
            res += player.destory(c, detail.get(c));
        }
        if (res != 0) {
            this.playerRefreshProperty(player);
            this.sendPlayerCardToken(player, detail.keySet(), null);
        }
        return res;
    }

    /**
     * 玩家摧毁建筑,部队
     *
     * @param player
     * @param card
     */
    public int playerDestroy(TTAPlayer player, TechCard card, int num) {
        int res = player.destory(card, num);
        if (res != 0) {
            this.playerRefreshProperty(player);
            this.sendPlayerCardToken(player, card, null);
        }
        return res;
    }

    /**
     * 玩家将牌放入弃牌堆
     *
     * @param player
     * @param cards
     */
    public void playerDiscardHand(TTAPlayer player, List<TTACard> cards) {
        // 只有军事牌会被放入弃牌堆
        if (!cards.isEmpty()) {
            this.playerRemoveHand(player, cards);
            this.gameMode.cardBoard.discardCards(cards);
            this.getReport().playerDiscardMilitaryHand(player, cards);
        }
    }

    /**
     * 玩家摸军事牌
     *
     * @param player
     */
    public void playerDrawMilitaryCard(TTAPlayer player) {
        if (this.getConfig().isRevoltDraw() || !this.checkPlayerUprising(player)) {
            // 玩家摸当前军事行动点数的军事牌
            int num = player.getAvailableActionPoint(ActionType.MILITARY);
            // 最多只能摸3张军事牌
            num = Math.min(num, player.getMilitaryDraw());
            if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_SUNTZU_ABILITY)) {
                num = 3;
            }
            Map<CivilCardAbility, TTAPlayer> abilities = gameMode
                    .getPlayerAbilities(CivilAbilityType.PA_MILITARY_DRAW_GLOBAL);
            for (CivilCardAbility ca : abilities.keySet()) {
                if (abilities.get(ca) != player || ca.effectSelf) {
                    num += ca.getAmount();
                }
            }
            this.playerDrawMilitaryCard(player, num);
            this.getReport().printCache(player);
        } else {
            this.getReport().playerCannotDrawWarning(player);
        }
    }

    /**
     * 玩家摸军事牌
     *
     * @param player
     * @param num
     */
    public void playerDrawMilitaryCard(TTAPlayer player, int num) {
        if (num > 0) {
            List<TTACard> cards = this.gameMode.cardBoard.drawMilitaryCard(num);
            if (!cards.isEmpty()) {
                this.playerAddHand(player, cards);
                this.getReport().playerDrawMilitary(player, cards);
            }
        }
    }

    /**
     * 检查是否有人口建造
     *
     * @param player
     * @throws BoardGameException
     */
    public void playerFreeWorkerCheck(TTAPlayer player) throws BoardGameException {
        CheckUtils.check(player.tokenPool.getUnusedWorkers() <= 0, "你没有空闲的人口!");
    }

    /**
     * 玩家得到奇迹
     *
     * @param player
     * @param card
     */
    public void playerGetWonder(TTAPlayer player, WonderCard card) {
        player.setUncompleteWonder(card);
        this.sendPlayerGetWonderResponse(player, card, null);
    }

    /**
     * 玩家扩张人口
     *
     * @param player
     * @param num
     */
    public void playerIncreasePopulation(TTAPlayer player, int num) {
        int res = player.increasePopulation(num);
        if (res != 0) {
            this.sendPlayerBoardTokens(player, null);
        }
    }

    public void playerPlayBonusCard(TTAPlayer player, List<TTACard> bonusCards) {
        if (!bonusCards.isEmpty()) {
            gameMode.getGame().playerRemoveHand(player, bonusCards);
            gameMode.getGame().tryDiscardCards(bonusCards);
            gameMode.getReport().playerBonusCardPlayed(player, bonusCards);
        }
    }

    /**
     * 玩家塞牌
     *
     * @param player
     * @param card
     * @return
     * @throws BoardGameException
     */
    public EventCard playerPlayEventCard(TTAPlayer player, TTACard card) throws BoardGameException {
        // 得到添加事件牌后,触发的事件牌,并处理该事件牌
        return this.playerAddEvent(player, (EventCard) card);
    }

    /**
     * 玩家生产粮矿,按顺序执行
     *
     * @param player
     * @param produceFood
     * @param doConsumption
     * @param produceResouce
     * @param doCorruption
     */
    public void playerProduce(TTAPlayer player, boolean produceFood, boolean doConsumption, boolean produceResouce,
                              boolean doCorruption, boolean doExtraProduce) {
        if (!this.isVersion2()) {
            // 原版，先生产再腐败
            if (produceFood)
                this.playerProduceFood(player, doExtraProduce);
            if (doConsumption)
                this.playerConsume(player);
            if (produceResouce)
                this.playerProduceResource(player, doExtraProduce);
            if (doCorruption)
                this.playerCorrupt(player);
        } else {
            // 新版，先腐败再生产
            if (doCorruption)
                this.playerCorrupt(player);
            if (produceFood)
                this.playerProduceFood(player, doExtraProduce);
            if (doConsumption)
                this.playerConsume(player);
            if (produceResouce)
                this.playerProduceResource(player, doExtraProduce);
        }

        // 刷新玩家所有卡牌上的指示物
        // this.sendPlayerCardToken(player, null);
        // 刷新玩家文明的信息
        this.sendPlayerCivilizationInfo(player, null);
        // 刷新玩家面板的指示物
        // this.sendPlayerBoardTokens(player, null);
    }

    /**
     * 玩家进行生产的详情
     *
     * @param player
     * @param map
     * @return
     */

    protected ProductionInfo playerProduceDetail(TTAPlayer player, Map<CivilCard, Integer> map,
                                                 CivilizationProperty property) {
        ProductionInfo res = new ProductionInfo(property);
        for (CivilCard c : map.keySet()) {
            int num = player.tokenPool.takeAvailableBlues(map.get(c));
            c.addBlues(num);
            res.put(c, num);
        }
        return res;
    }

    /**
     * 玩家生产粮食
     *
     * @param player
     */
    protected void playerProduceFood(TTAPlayer player) {
        this.playerProduceFood(player, false);
    }

    /**
     * 玩家生产粮食
     *
     * @param player
     * @param doExtraProduce
     */
    protected void playerProduceFood(TTAPlayer player, boolean doExtraProduce) {
        // 农田生产
        ProductionInfo res = this.playerProduceDetail(player, player.getProductionFromFarm(),
                CivilizationProperty.FOOD);

        // 检查玩家生产食物的特殊能力
        if (doExtraProduce) {
            int foodnum = player.getProperties().getProperty(CivilizationProperty.EXTRA_FOOD);
            if (foodnum != 0) {
                res.addAll(player.addFood(foodnum));
            }
        }

        this.sendPlayerCardToken(player, res.keySet(), null);
        this.sendPlayerBoardTokens(player, null);
        // 最终生产报告
        this.getReport().playerProduceFood(player, res.getTotalValue());
    }

    /**
     * 玩家生产资源
     *
     * @param player
     */
    protected void playerProduceResource(TTAPlayer player) {
        this.playerProduceResource(player, false);
    }

    /**
     * 玩家生产资源
     *
     * @param player
     * @param doExtraProduce
     */
    protected void playerProduceResource(TTAPlayer player, boolean doExtraProduce) {
        // 矿山生产
        ProductionInfo res = this.playerProduceDetail(player, player.getProductionFromMine(),
                CivilizationProperty.RESOURCE);

        // 特斯拉产矿
        if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_TESLA_ABILITY)) {
            res.addAll(this.playerProduceDetail(player, player.getProductionFromLab(), CivilizationProperty.RESOURCE));
        }

        // 检查玩家生产资源的特殊能力
        if (doExtraProduce) {
            int resnum = player.getProperties().getProperty(CivilizationProperty.EXTRA_RESOURCE);
            if (resnum != 0) {
                res.addAll(player.addResource(resnum));
            }
        }

        this.sendPlayerCardToken(player, res.keySet(), null);
        this.sendPlayerBoardTokens(player, null);
        this.getReport().playerProduceResource(player, res.getTotalValue());
    }

    public void playerReattachCard(TTAPlayer player, TTACard newCard, TTACard oldCard) {
        if (oldCard.getAttachedCards() != null) {
            for (TTACard c : oldCard.getAttachedCards()) {
                gameMode.getGame().playerAttachCard(player, c, newCard);
            }
            oldCard.setAttachedCards(null);
        }
    }

    /**
     * 刷新玩家信息
     *
     * @param player
     */
    public void playerRefreshProperty(TTAPlayer player) {
        player.refreshProperties();
        this.sendPlayerCivilizationInfo(player, null);
    }

    /**
     * 玩家移除卡牌列上的卡牌(法雅节)
     *
     * @param player
     * @param card
     * @param index
     */
    public void playerRemoveBoardCard(TTAPlayer player, TTACard card, int index) {
        this.getGameMode().getCardBoard().takeCard(index);
        this.getReport().playerTakeCardCache(player, card, index);
        this.sendCardRowRemoveCardResponse(card.id);
    }

    public void playerRemoveCard(TTAPlayer player, String cardId) {
        for (TTACard c : player.getAllPlayedCard()) {
            if (c.id.equals(cardId)) {
                this.playerRemoveCard(player, c);
            }
        }
    }

    public void playerRemoveCard(TTAPlayer player, TTACard card) {
        this.playerRemoveCardDirect(player, card);
        this.getReport().playerRemoveCard(player, card);
    }

    /**
     * 玩家失去已打出的牌
     *
     * @param player
     * @param card
     */
    public void playerRemoveCardDirect(TTAPlayer player, TTACard card) {
        TTAProperty result = player.removeCard(card);
        this.sendPlayerRemoveCardResponse(player, card, null);
        this.playerAddToken(player, result);
        this.playerAddAction(player, result);
        this.playerRefreshProperty(player);
        // 玩家卡牌变化时,刷新所有与玩家有关联属性能力的玩家属性
        this.refreshRelationPlayerProperty(player, card);
    }

    /**
     * 玩家失去手牌
     *
     * @param player
     * @param cards
     */
    public void playerRemoveHand(TTAPlayer player, List<TTACard> cards) {
        player.removeHand(cards);
        this.sendPlayerRemoveHandResponse(player, cards, null);
    }

    /**
     * 玩家失去手牌
     *
     * @param player
     * @param card
     */
    public void playerRemoveHand(TTAPlayer player, TTACard card) {
        this.playerRemoveHand(player, BgUtils.toList(card));
    }

    /**
     * 玩家失去未建成的奇迹
     *
     * @param player
     */
    public void playerRemoveUncompleteWonder(TTAPlayer player) {
        WonderCard card = player.getUncompleteWonder();
        if (card != null) {
            // 如果该奇迹上有蓝色标志物,则需要回到资源库
            int blues = card.getBlues();
            if (blues > 0) {
                player.tokenPool.addAvailableBlues(blues);
                this.sendPlayerBoardTokens(player, null);
            }
            player.setUncompleteWonder(null);
            this.sendPlayerRemoveCardResponse(player, card, null);
            this.getReport().playerRemoveCard(player, card);
        }
    }

    /**
     * 玩家结束请求,关闭窗口
     *
     * @param player
     */
    public void playerRequestEnd(TTAPlayer player) {
        this.sendPlayerRequestEndResponse(player);
    }

    /**
     * 玩家研发科技时支付科技点,会消耗临时科技,执行科技协作等能力
     *
     * @param player
     * @param card
     * @param cost
     */
    public void playerResearchCost(TTAPlayer player, TTACard card, TTAProperty cost) {
        // 自身消耗科技点
        this.playerAddPoint(player, cost, -1);
        this.getReport().playerCostPoint(player, cost, "花费");
        // 发动科技协作能力
        Map<CivilCardAbility, PactCard> pacts = player.abilityManager
                .getPactAbilitiesWithRelation(CivilAbilityType.PA_SCIENCE_ASSIST);
        for (CivilCardAbility ca : pacts.keySet()) {
            PactCard p = pacts.get(ca);
            int num = this.playerAddSciencePoint(p.alian, -ca.amount);
            this.getReport().playerAddSciencePoint(p.alian, num);
            this.getReport().printCache(p.alian);
        }
    }

    /**
     * 玩家重置行动点数
     *
     * @param player
     */
    public void playerResetActionPoint(TTAPlayer player) {
        player.resetActionPoint();
        this.sendPlayerCardToken(player, player.getGoverment(), null);
    }

    /**
     * 玩家体面退出游戏
     *
     * @param player
     * @throws BoardGameException
     */
    public void playerResign(TTAPlayer player) throws BoardGameException {
        CheckUtils.check(gameMode.finalRound && gameMode.getGame().isVersion2(), "新版最后一轮不能体退!");
        if (this.isTeamMatch() || this.isTichuMode()) {
            for (TTAPlayer p : this.getPlayers()) {
                if (this.isTeammates(p, player)) {
                    TTAConfirmListener listener = new TTAConfirmListener(player, "你的队友决定体面退出游戏,你同意吗?");
                    listener.addListeningPlayer(p);
                    InterruptParam res = gameMode.insertListener(listener);
                    boolean confirm = res.getBoolean(p.getPosition());
                    CheckUtils.check(!confirm, "你的队友不同意体面退出游戏!");
                    this.forceResign(p);
                }
            }
            this.forceResign(player);
            this.winGame();
        } else {
            this.forceResign(player);
            for (TTACard c : player.getAllPlayedCard()) {
                switch (c.cardType) {
                    case PACT:
                        this.removeOvertimeCard((IOvertimeCard) c);
                        break;
                    case WAR:
                        AttackCard ac = ((AttackCard) c);
                        TTAPlayer p = ac.getOwner();
                        // 新版体退会给战争的对手加7分
                        int point = TTAConstManager.getResignWarCulture(this.isVersion2());
                        this.playerAddCulturePoint(p, point);
                        this.getReport().playerAddCulturePoint(p, point);
                        this.getReport().printCache(p);
                        this.removeOvertimeCard(ac);
                        break;
                    default:
                        break;
                }
            }
            this.tryDiscardCards(player.getAllPlayedCard());
            player.clearAllPlayedCard();
            this.tryDiscardCards(player.getAllHands());
            player.clearAllHands();
            this.getReport().playerResign(player);
        }
        gameMode.doregroup = false;
        if (this.getRealPlayerNumber() <= this.getRequiredPlayerNumber()) {
            this.winGame();
        }
    }

    /**
     * 玩家回合结束时生产粮食,资源,文明和科技点数
     *
     * @param player
     * @throws BoardGameException
     */
    public void playerRoundScore(TTAPlayer player) {
        if (!this.checkPlayerUprising(player)) {
            // 生产粮矿、文化和科技
            this.playerProduce(player, true, true, true, true, true);
            this.playerScoreScienct(player);
            this.playerScoreCulture(player);
            this.getReport().playerRoundScore(player);
        } else {
            // 如果产生暴动,则什么都不会生产
            this.getReport().playerUprisingWarning(player);
        }
    }

    /**
     * 玩家牺牲部队
     *
     * @param player
     * @param units
     * @throws BoardGameException
     */
    public void playerSacrifidUnit(TTAPlayer player, Map<TechCard, Integer> units) {
        this.playerDecreasePopulation(player, units);
        this.getReport().playerSacrifidUnit(player, units);
    }

    /**
     * 玩家生产文明点数
     *
     * @param player
     */
    public void playerScoreCulture(TTAPlayer player) {
        int res = this.playerAddCulturePoint(player, player.scoreCulturePoint());
        this.getReport().playerScoreCulturePoint(player, res);
    }

    /**
     * 玩家生产科技点数
     *
     * @param player
     */
    public void playerScoreScienct(TTAPlayer player) {
        int num = this.playerAddSciencePoint(player, player.scoreSciencePoint());
        this.getReport().playerScoreSciencePoint(player, num);
    }

    /**
     * 从卡排列拿牌
     */
    public void playerTakeCard(TTAPlayer player, TTACard card, int index) {
        gameMode.cardBoard.takeCard(index);

        if (card.cardType == CardType.WONDER) {
            // 如果是奇迹牌则直接打出
            this.playerGetWonder(player, (WonderCard) card);
        } else {
            // 否则加入手牌
            this.playerAddHand(player, card);
        }

        if (card.cardType == CardType.LEADER) {
            // 如果拿的是领袖,则设置玩家的领袖参数
            player.setHasLeader(card.level);
        }
        this.sendCardRowRemoveCardResponse(card.id);
    }

    /**
     * 玩家升级建筑,部队
     *
     * @param player
     * @param fromCard
     * @param toCard
     */
    public void playerUpgrade(TTAPlayer player, CivilCard fromCard, CivilCard toCard) {
        player.upgrade(fromCard, toCard);
        this.playerRefreshProperty(player);
        this.sendPlayerCardToken(player, Arrays.asList(fromCard, toCard), null);
    }

    /**
     * 玩家对目标玩家使用卡牌
     *
     * @param player
     * @param target
     * @param card
     */
    public void playerUseCardOnPlayer(TTAPlayer player, TTAPlayer target, TTACard card) {
        if (card instanceof IOvertimeCard) {
            IOvertimeCard c = (IOvertimeCard) card;
            c.setOwner(player);
            c.setTarget(target);
        }
        // 将玩家间持续效果卡牌的信息发送到客户端
        this.sendOvertimeCardInfoResponse(card);
        // 玩家和目标玩家得到卡牌
        this.playerAddCardDirect(player, card);
        this.getReport().playerAddCard(player, card);
        this.playerAddCardDirect(target, card);
        this.getReport().playerAddCard(target, card);
    }

    /**
     * @param player
     * @param num
     * @return
     */
    public void refreshDecreasePopulation(TTAPlayer player, int num) {
        BgResponse res = TTACmdFactory.createPlayerBoardTokensResponse(player);
        res.setPublicParameter(Token.UNUSED_WORKER.toString(), player.tokenPool.getUnusedWorkers() - num);
        this.sendResponse(player, res);
    }

    /**
     * @param player
     * @param card
     * @param num
     * @return
     */
    public void refreshDecreasePopulation(TTAPlayer player, TechCard card, int num) {
        this.refreshDecreasePopulation(player, card, num, player);
    }

    /**
     * @param player
     * @param card
     * @param num
     * @param receiver
     * @return
     */
    public void refreshDecreasePopulation(TTAPlayer player, TechCard card, int num, TTAPlayer receiver) {
        TechCard newCard = card.clone();
        newCard.addWorkers(-num);
        this.sendPlayerCardToken(player, BgUtils.toList(newCard), receiver);

    }

    /**
     * 刷新与玩家有关联属性玩家的属性
     *
     * @param player
     * @param card
     */
    public void refreshRelationPlayerProperty(TTAPlayer player, TTACard card) {
        for (TTAPlayer p : gameMode.getRealPlayers()) {
            Map<CivilCardAbility, PactCard> pacts = p.abilityManager
                    .getPactAbilitiesWithRelation(CivilAbilityType.ADJUST_PROPERTY_BY_ALIAN);
            for (CivilCardAbility ca : pacts.keySet()) {
                if (ca.test(card) && pacts.get(ca).alian == player) {
                    this.playerRefreshProperty(p);
                    break;
                }
            }
        }
    }

    /**
     * 卡牌序列补牌
     */
    public boolean regroupCardRow(boolean doDiscard) {
        int oldAge = gameMode.currentAge;
        gameMode.getCardBoard().regroupCardRow(doDiscard);
        this.sendCardRowInfo(null);
        this.sendCardRowReport();
        int newAge = gameMode.currentAge;
        return newAge > oldAge;
    }

    /**
     * 移除持续效果的卡牌
     *
     * @param card
     */
    public void removeOvertimeCard(IOvertimeCard card) {
        String cardId = ((Card) card).id;
        this.playerRemoveCard(card.getOwner(), cardId);
        this.playerRemoveCard(card.getTarget(), cardId);
    }

    /**
     * 移除条约
     *
     * @param targetPlayer
     * @param player
     * @param cardId
     */
    public void removePactCard(TTAPlayer player, TTAPlayer targetPlayer, String cardId) {
        // 因为每个玩家的条约牌都是副本,所以需要找到该条约牌副本对象
        for (TTACard c : player.getAllPlayedCard()) {
            if (c.id.equals(cardId)) {
                this.playerRemoveCard(player, c);
            }
        }
        for (TTACard c : targetPlayer.getAllPlayedCard()) {
            if (c.id.equals(cardId)) {
                this.playerRemoveCard(player, c);
            }
        }
    }

    /**
     * 移除所有玩家过时的卡牌
     */
    public void removePastCards() {
        if (this.gameMode.getCurrentAge() > 0) {
            // 从I时代才开始移除...
            int pastAge = this.gameMode.getCurrentAge() - 1;
            // 所有玩家需要弃掉前一时代的领袖,未建成奇迹,条约,和手牌
            for (TTAPlayer player : gameMode.getRealPlayers()) {
                if (checkPastCard(player.getLeader(), pastAge)) {
                    this.getReport().playerRemoveCard(player, player.getLeader());
                    this.playerRemoveCardDirect(player, player.getLeader());
                }
                if (checkPastCard(player.getUncompleteWonder(), pastAge)) {
                    this.playerRemoveUncompleteWonder(player);
                }
                if (checkPastCard(player.getPact(), pastAge)) {
                    this.removeOvertimeCard(player.getPact());
                }
                // 时代变迁时需要移除2个黄色标记
                if (this.getConfig().darkAge) {
                    this.playerAddToken(player, TTAConstManager.getDarkAgeToken());
                    this.getReport().printCache(player);
                }

                // 移除手牌列表
                List<TTACard> discards = new ArrayList<>();
                for (TTACard c : player.getAllHands()) {
                    if (this.checkPastCard(c, pastAge)) {
                        discards.add(c);
                    }
                }
                this.playerRemoveHand(player, discards);
                this.getReport().playerRemoveHand(player, discards);

                // 刷新玩家文明的属性
                this.sendPlayerCivilizationInfo(player, null);
            }
        }
    }

    /**
     * 发送添加事件牌的信息
     *
     * @param player
     * @param receiver
     */
    public void sendAddEventResponse(TTAPlayer player, TTACard card, boolean futureToCurrent, TTAPlayer receiver) {
        boolean lazyMemory = getConfig().lazyMemory;
        if (card != null) {
            int position = lazyMemory && player != null ? player.getPosition() : -1;
            BgResponse res = CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_ADD_EVENT, position);
            res.setPrivateParameter("cardId", card.id);
            if (player != null) {
                if (lazyMemory) {
                    res.setPublicParameter("player", player.getPosition());
                } else {
                    res.setPrivateParameter("player", player.getPosition());
                }
            }
            res.setPublicParameter("cardLevel", card.level);
            res.setPublicParameter("futureToCurrent", futureToCurrent);
            this.sendResponse(receiver, res);
        }
    }

    /**
     * 添加过去事件
     *
     * @param card
     */
    public void sendAddPastEventResponse(TTACard card, TTAPlayer receiver) {
        this.sendAddPastEventsResponse(BgUtils.toList(card), receiver);
    }

    /**
     * 添加过去事件
     */
    public void sendAddPastEventsResponse(Collection<TTACard> cards, TTAPlayer receiver) {
        BgResponse res = CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_ADD_EVENT, -1);
        res.setPublicParameter("past", true);
        res.setPublicParameter("cardIds", BgUtils.card2String(cards));
        this.sendResponse(receiver, res);
    }

    /**
     * 向目标玩家发送所有持续效果卡牌的信息
     *
     * @param receiver
     */
    public void sendAllOvertimeCardsInfo(TTAPlayer receiver) {
        List<TTACard> cards = new ArrayList<>();
        for (TTAPlayer p : gameMode.getRealPlayers()) {
            if (p.getWar() != null) {
                cards.add(p.getWar());
            }
            if (p.getPact() != null) {
                cards.add(p.getPact());
            }
        }
        this.sendOvertimeCardsInfoResponse(cards, receiver);
    }

    /**
     * 发送游戏基本信息
     *
     * @param receiver
     */
    public void sendBaseInfo(TTAPlayer receiver) {
        BgResponse res = CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_BASE_INFO, -1);
        CardBoard cardBoard = this.gameMode.cardBoard;
        // 设置基本信息
        res.setPublicParameter("isVersion2", this.gameMode.getGame().isVersion2());
        res.setPublicParameter("infoType", "base");
        res.setPublicParameter(TTACmdString.CURRENT_AGE, this.gameMode.getCurrentAge());
        res.setPublicParameter(TTACmdString.CIVIL_REMAIN, cardBoard.getCivilRemain());
        res.setPublicParameter(TTACmdString.MILITARY_REMAIN, cardBoard.getMilitaryRemain());
        // 设置最近一个事件的信息
        EventCard card = cardBoard.getLastEvent();
        if (card != null) {
            res.setPublicParameter("lastEventCardId", card.id);
        }
        // 设置未来事件的信息
        card = cardBoard.getNextFutureEventCard();
        if (card != null) {
            res.setPublicParameter("nextFutureEventLevel", card.level);
            res.setPublicParameter("futureDeckNum", cardBoard.getFutureEventDeck().size());
        }
        // 设置当前事件的信息
        card = cardBoard.getNextCurrentEventCard();
        if (card != null) {
            res.setPublicParameter("nextCurrentEventLevel", card.level);
            res.setPublicParameter("currentDeckNum", cardBoard.getCurrentEventDeck().size());
        }
        // 设置新版可学阵型的信息
        List<TTACard> cardList = cardBoard.getPublicTacticsDeck().getCards();
        if (!cardList.isEmpty()) {
            res.setPublicParameter("tactics", BgUtils.card2String(cardList));
        }
        this.sendResponse(receiver, res);
    }

    /**
     * 向玩家发送奖励牌堆的信息
     *
     * @param receiver
     */
    public void sendBonusCard(TTAPlayer receiver) {
        this.sendResponse(receiver, TTACmdFactory.createBonusCardResponse(receiver, this.gameMode.bonusCards));
    }

    /**
     * 发送文明牌序列的信息
     *
     * @param receiver
     */
    public void sendCardRowInfo(TTAPlayer receiver) {
        BgResponse res = CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_CARD_ROW, -1);
        res.setPublicParameter("cardIds", this.gameMode.cardBoard.getCardRowIds());
        this.sendResponse(receiver, res);
    }

    /**
     * 发送卡牌序列失去卡牌的消息
     */
    public void sendCardRowRemoveCardResponse(String cardId) {
        this.sendResponse(TTACmdFactory.createCardRowRemoveCardResponse(cardId));
    }

    /**
     * 发送卡牌列信息
     */
    public void sendCardRowReport() {
        this.getReport().refreshCardRow(gameMode.getCardBoard().getCardRow());
    }

    /**
     * 发送腐败信息
     *
     * @param player
     * @param property
     */
    protected void sendCorruptionAlert(TTAPlayer player, TTAProperty property) {
        StringBuilder sb = new StringBuilder("你因为腐败");
        boolean flag = false;
        int p = property.getProperty(CivilizationProperty.FOOD);
        if (p != 0) {
            flag = true;
            sb.append(",失去").append(-p).append("食物");
        }
        p = property.getProperty(CivilizationProperty.RESOURCE);
        if (p != 0) {
            flag = true;
            sb.append(",失去").append(-p).append("资源");
        }
        if (flag) {
            this.sendAlert(player, sb.toString());
        }
    }

    @Override
    protected void sendGameInfo(TTAPlayer receiver) throws BoardGameException {
        // 需要发送以下游戏信息
        // 当前世纪
        // 当前文明牌剩余数量
        // 未来事件牌堆数量
        this.sendBaseInfo(receiver);
        // 发送领袖和奇迹信息
        this.sendLeadersAndWondersInfo(receiver);
        // 当前文明牌序列
        this.sendCardRowInfo(receiver);
        // 如果是额外奖励牌模式,则发送奖励牌堆的信息
        if (this.getConfig().bonusCardFlag) {
            this.sendBonusCard(receiver);
        }
    }

    @Override
    protected void sendInitInfo(TTAPlayer receiver) throws BoardGameException {

    }

    /**
     * 发送领袖奇迹信息
     *
     * @param receiver
     */
    public void sendLeadersAndWondersInfo(TTAPlayer receiver) {
        BgResponse res = CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_BASE_INFO, -1);
        // 设置领袖奇迹显示板
        res.setPublicParameter("infoType", "leadersAndWonders");
        if (!this.getConfig().isHideAvalable()) {
            for (int i = 0; i < this.getConfig().getAgeCount(); ++i) {
                res.setPublicParameter("leaders_".concat(String.valueOf(i)),
                        BgUtils.card2String(gameMode.getCardBoard().leaders.get(i)));
                res.setPublicParameter("wonders_".concat(String.valueOf(i)),
                        BgUtils.card2String(gameMode.getCardBoard().wonders.get(i)));
            }
        }
        this.sendResponse(receiver, res);
    }

    /**
     * 发送玩家间持续效果卡牌的信息
     */
    public void sendOvertimeCardInfoResponse(TTACard card) {
        this.sendOvertimeCardsInfoResponse(BgUtils.toList(card), null);
    }

    /**
     * 发送玩家间持续效果卡牌的信息
     *
     * @param cards
     */
    public void sendOvertimeCardsInfoResponse(List<TTACard> cards, TTAPlayer receiver) {
        this.sendResponse(receiver, TTACmdFactory.createOvertimeCardsInfoResponse(cards, receiver));
    }

    /**
     * 发送玩家请求行动的信息,card为使用的卡牌
     *
     * @param player
     * @param cmdString 请求的命令字符串
     * @param msg       显示的信息
     * @param showCard  展示的card
     * @param param     其他参数
     */
    public void sendPlayerActionRequestResponse(TTAPlayer player, String cmdString, String msg, TTACard showCard,
                                                Map<String, Object> param) {
        // 该请求只需向自己发送
        this.sendResponse(player,
                TTACmdFactory.createPlayerActionRequestResponse(player, cmdString, msg, showCard, param));
    }

    /**
     * 向玩家发送他可激活卡牌的列表
     *
     * @param activeStep
     * @param player
     */
    public void sendPlayerActivableCards(RoundStep activeStep, TTAPlayer player) {
        this.sendResponse(player, TTACmdFactory.createPlayerActivableCardsResponse(activeStep, player));
    }

    /**
     * 发送玩家添加打出卡牌的信息
     *
     * @param player
     * @param receiver
     */
    public void sendPlayerAddCardResponse(TTAPlayer player, TTACard card, TTAPlayer receiver) {
        if (card != null) {
            this.sendResponse(receiver, TTACmdFactory.createPlayerAddCardResponse(player, card));
        }
    }

    /**
     * 发送玩家添加打出卡牌的信息
     *
     * @param player
     * @param cards
     * @param receiver
     */
    public void sendPlayerAddCardsResponse(TTAPlayer player, List<TTACard> cards, TTAPlayer receiver) {
        this.sendResponse(receiver, TTACmdFactory.createPlayerAddCardsResponse(player, cards));
    }

    /**
     * 发送玩家得到手牌的消息
     *
     * @param player
     * @param cards
     * @param receiver
     */
    public void sendPlayerAddHandResponse(TTAPlayer player, List<TTACard> cards, TTAPlayer receiver) {
        this.sendResponse(receiver,
                TTACmdFactory.createPlayerAddHandResponse(player, cards, this.getConfig().lazyMemory));
    }

    /**
     * 发送玩家叠放卡牌的信息
     *
     * @param player
     * @param receiver
     */
    public void sendPlayerAttachCardResponse(TTAPlayer player, TTACard card, TTACard destCard, TTAPlayer receiver) {
        if (card != null) {
            this.sendResponse(receiver, TTACmdFactory.createPlayerAttachCardResponse(player, card, destCard));
        }
    }

    /**
     * 发送玩家桌面标志物的数量(该方法在发送玩家文明信息时被调用)
     *
     * @param player
     * @param receiver
     */
    public void sendPlayerBoardTokens(TTAPlayer player, TTAPlayer receiver) {
        this.sendResponse(receiver, TTACmdFactory.createPlayerBoardTokensResponse(player));
    }

    /**
     * 发送玩家卡牌的标志物信息
     *
     * @param player
     * @param cards
     * @param receiver
     */
    public void sendPlayerCardToken(TTAPlayer player, Collection<? extends TTACard> cards, TTAPlayer receiver) {
        this.sendResponse(receiver, TTACmdFactory.createPlayerCardTokenResponse(player, cards));
    }

    /**
     * 发送玩家所有卡牌的标志物信息
     *
     * @param player
     * @param receiver
     */
    public void sendPlayerCardToken(TTAPlayer player, TTAPlayer receiver) {
        List<TTACard> cards = player.getAllPlayedCard();
        // 发送玩家未建成奇迹的token信息
        if (player.getUncompleteWonder() != null) {
            cards.add(player.getUncompleteWonder());
        }
        this.sendPlayerCardToken(player, cards, receiver);
    }

    /**
     * 发送玩家卡牌的标志物信息
     *
     * @param player
     * @param card
     * @param receiver
     */
    public void sendPlayerCardToken(TTAPlayer player, TTACard card, TTAPlayer receiver) {
        this.sendPlayerCardToken(player, BgUtils.toList(card), receiver);
    }

    /**
     * 发送玩家文明的基本属性信息
     *
     * @param player
     * @param receiver
     */
    public void sendPlayerCivilizationInfo(TTAPlayer player, TTAPlayer receiver) {
        this.sendResponse(receiver, TTACmdFactory.createPlayerCivilizationInfoResponse(player));
        // 同时更新一下玩家当前政府牌上token的信息
        if (player.getGoverment() != null) {
            this.sendPlayerCardToken(player, player.getGoverment(), receiver);
        }
        // 以及玩家面板的情况
        this.sendPlayerBoardTokens(player, receiver);
    }

    /**
     * 发送玩家得到奇迹的信息
     *
     * @param player
     * @param card
     * @param receiver
     */
    public void sendPlayerGetWonderResponse(TTAPlayer player, TTACard card, TTAPlayer receiver) {
        this.sendResponse(receiver, TTACmdFactory.createPlayerGetWonderResponse(player, card));
    }

    @Override
    protected void sendPlayerPlayingInfo(TTAPlayer receiver) throws BoardGameException {
        // 发送全局的游戏信息
        this.sendAllOvertimeCardsInfo(receiver);
        // 需要发送以下玩家游戏状态
        for (TTAPlayer p : this.getPlayers()) {
            // 玩家所有已打出牌的信息

            this.sendPlayerAddCardsResponse(p, p.getAllPlayedCard(), receiver);
            if (p.getTactics() != null) {
                this.sendPlayerAddCardResponse(p, p.getTactics(), receiver);
            }
            if (p.getUncompleteWonder() != null) {
                this.sendPlayerGetWonderResponse(p, p.getUncompleteWonder(), receiver);
            }
            // 玩家的手牌信息
            this.sendPlayerAddHandResponse(p, p.getAllHands(), receiver);
            // 玩家文明信息
            this.sendPlayerCivilizationInfo(p, receiver);
            // 玩家所有卡牌和台面上标志物的信息
            this.sendPlayerCardToken(p, receiver);
            // 玩家叠加卡牌的信息
            for (TTACard c : this.gameMode.cardBoard.getAttachedCards().keySet()) {
                TTACard d = this.gameMode.cardBoard.getAttachedCards().get(c);
                if (p.getAllPlayedCard().contains(d))
                    this.sendPlayerAttachCardResponse(p, c, d, receiver);
            }
        }
        if (!gameMode.cardBoard.getPastEventDeck().isEmpty()) {
            this.sendAddPastEventsResponse(gameMode.cardBoard.getPastEventDeck().getCards(), receiver);
        }
        for (TTACard c : gameMode.cardBoard.currentEventRelation.keySet()) {
            this.sendAddEventResponse(gameMode.cardBoard.currentEventRelation.get(c), c, true, receiver);
        }
        for (TTACard c : gameMode.cardBoard.futureEventRelation.keySet()) {
            this.sendAddEventResponse(gameMode.cardBoard.futureEventRelation.get(c), c, false, receiver);
        }
    }

    /**
     * 发送玩家结束政治阶段
     *
     * @param player
     * @param currentStep
     */
    public void sendPlayerPoliticalEndResponse(TTAPlayer player, RoundStep currentStep) {
        BgResponse res = CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_CHANGE_STEP, player.getPosition());
        res.setPublicParameter("currentStep", currentStep);
        this.sendResponse(res);
    }

    /**
     * 发送玩家失去打出卡牌的信息
     *
     * @param player
     * @param receiver
     */
    public void sendPlayerRemoveCardResponse(TTAPlayer player, TTACard card, TTAPlayer receiver) {
        if (card != null) {
            this.sendResponse(receiver, TTACmdFactory.createPlayerRemoveCardResponse(player, card));
        }
    }

    /**
     * 发送玩家失去打出卡牌的信息
     *
     * @param player
     * @param cards
     * @param receiver
     */
    public void sendPlayerRemoveCardsResponse(TTAPlayer player, List<TTACard> cards, TTAPlayer receiver) {
        this.sendResponse(receiver, TTACmdFactory.createPlayerRemoveCardsResponse(player, cards));
    }

    /**
     * 发送玩家失去手牌的消息
     *
     * @param player
     * @param cards
     * @param receiver
     */
    public void sendPlayerRemoveHandResponse(TTAPlayer player, List<TTACard> cards, TTAPlayer receiver) {
        this.sendResponse(receiver,
                TTACmdFactory.createPlayerRemoveHandResponse(player, cards, this.getConfig().lazyMemory));
    }

    /**
     * 发送玩家请求行动结束,关闭窗口的信息
     *
     * @param player
     */
    public void sendPlayerRequestEndResponse(TTAPlayer player) {
        this.sendResponse(player, CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_REQUEST_END, player.getPosition()));
    }

    /**
     * 发送玩家当前选中手牌的消息
     *
     * @param player
     * @param cards
     * @param receiver
     */
    public void sendPlayerSelectHandResponse(TTAPlayer player, List<TTACard> cards, TTAPlayer receiver) {
        this.sendResponse(receiver, TTACmdFactory.createPlayerSelectHandResponse(player, cards));
    }

    /**
     * 发送玩家奇迹建造完成的消息
     *
     * @param player
     */
    public void sendPlayerWonderCompleteResponse(TTAPlayer player) {
        this.sendResponse(CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_WONDER_COMPLETE, player.getPosition()));
    }

    /**
     * 发送移除事件牌的信息
     *
     * @param player
     * @param futureToCurrent
     * @param receiver
     */
    public void sendRemoveEventResponse(TTAPlayer player, TTACard card, boolean futureToCurrent, TTAPlayer receiver) {
        boolean lazyMemory = getConfig().lazyMemory;
        if (card != null) {
            int position = lazyMemory && player != null ? player.getPosition() : -1;
            BgResponse res = CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_REMOVE_EVENT, position);
            res.setPrivateParameter("cardId", card.id);
            if (player != null) {
                if (lazyMemory) {
                    res.setPublicParameter("player", player.getPosition());
                } else {
                    res.setPrivateParameter("player", player.getPosition());
                }
            }
            res.setPublicParameter("cardLevel", card.level);
            res.setPublicParameter("futureToCurrent", futureToCurrent);
            this.sendResponse(receiver, res);
        }
    }

    /**
     * 移除过去事件（殖民等）
     *
     * @param card
     */
    public void sendRemovePastEventResponse(TTACard card, TTAPlayer receiver) {
        BgResponse res = CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_REMOVE_EVENT, -1);
        res.setPublicParameter("past", true);
        res.setPublicParameter("cardIds", card.id);
        this.sendResponse(receiver, res);
    }

    /**
     * 发送第一张当前事件
     *
     * @param receiver
     */
    public void sendTopEvent(TTAPlayer receiver) {
        BgResponse res = CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_BASE_INFO, -1);
        // 设置领袖奇迹显示板
        res.setPublicParameter("infoType", "topEvent");
        // 设置当前事件的信息
        EventCard card = this.gameMode.cardBoard.getNextCurrentEventCard();
        if (card != null) {
            res.setPublicParameter("nextCurrentEventCardId", card.id);
        }
        this.sendResponse(receiver, res);
    }

    /**
     * 发送战争的提示信息
     *
     * @param player
     * @param trigPlayer
     * @param card
     */
    public void sendWarAlertInfo(TTAPlayer player, TTAPlayer trigPlayer, TTACard card) {
        Map<String, Object> param = new HashMap<>();
        param.put("cardId", card.id);
        this.sendAlert(player, trigPlayer.getReportString() + "对你宣战了!", param);
    }

    /**
     * 设置当前玩家
     */
    public void setCurrentPlayer(TTAPlayer p) {
        currentPlayer = p;
        if (gameMode != null) {
            gameMode.gameRank.currentPlayer = p;
        }
    }

    /**
     * @param player
     */
    public void setTichu(TTAPlayer player, TTACard card) {
        this.startPlayer = player;
        this.currentPlayer = player;
        this.playerAddCardDirect(player, card);
        player.setTeam(0);
        for (TTAPlayer p : this.getPlayers()) {
            if (p.getPosition() != player.getPosition()) {
                p.setTeam(1);
            }
        }
    }

    @Override
    protected void setupGame() throws BoardGameException {
        getLog().info("设置游戏...");
        int num = this.getCurrentPlayerNumber();
        getLog().info("游戏人数: " + num);
        // 设置游戏人数
        this.config.playerNumber = num;
        this.gameMode = new TTAGameMode(this);
    }

    /**
     * 将使用过的牌放入弃牌堆（新版规则）
     *
     * @param cards
     */
    public void tryDiscardCards(List<TTACard> cards) {
        if (this.isVersion2()) {
            gameMode.getCardBoard().discardCards(cards);
        }
    }

    /**
     * 将使用过的牌放入弃牌堆（新版规则）
     *
     * @param card
     */
    public void tryDiscardCards(TTACard card) {
        this.tryDiscardCards(BgUtils.toList(card));
    }

}
