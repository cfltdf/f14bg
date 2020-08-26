package com.f14.TTA.listener;

import com.f14.F14bg.network.CmdFactory;
import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.ability.CivilCardAbility;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.card.TechCard;
import com.f14.TTA.component.card.WonderCard;
import com.f14.TTA.component.param.AuctionParam;
import com.f14.TTA.consts.ActiveAbilityType;
import com.f14.TTA.consts.CardType;
import com.f14.TTA.consts.CivilAbilityType;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.utils.BgUtils;
import com.f14.bg.utils.CheckUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择部队的监听器
 *
 * @author F14eagle
 */
public abstract class ChooseArmyListener extends TTAOrderInterruptListener {

    public ChooseArmyListener(TTAPlayer trigPlayer) {
        super(trigPlayer);
    }

    /**
     * 调整拍卖所用的加值卡
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void adjustBonusCard(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        String cardId = action.getAsString("cardId");
        TTACard c = player.getCard(cardId);
        boolean selected = action.getAsBoolean("selected");
        AuctionParam param = this.getParam(player.getPosition());
        if (c.activeAbility != null && c.activeAbility.abilityType == ActiveAbilityType.PA_SHIHUANG_TOMB) {
        } else if (c.cardType != CardType.DEFENSE_BONUS && !(gameMode.getGame().isVersion2() && !this.isColony())
                && !super.getAbilityManager().hasAbilitiy(CivilAbilityType.PA_MEIRIN)
                && !super.getAbilityManager().hasAbilitiy(CivilAbilityType.PA_CARD_AS_BONUSCARD)) {
            throw new BoardGameException("只能选择防御/殖民地加值卡!");
        }
        param.setBonusCard(c, selected);
        // 向操作的玩家刷新当前出价的总值
        this.sendPlayerAuctionValue(gameMode, player, player);
    }

    /**
     * 调整拍卖所用的部队
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void adjustUnit(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        String cardId = action.getAsString("cardId");
        int num = action.getAsInt("num");
        AuctionParam param = this.getParam(player.getPosition());
        if (cardId == null || cardId.isEmpty()) {
            // 调整出价
            CheckUtils.check(isWar(), "不能在战争中选择出价!");
            CheckUtils.check(isColony() && num > param.checkTotalValue(), "不能选择超出你要牺牲的部队的出价!");
            CheckUtils.check(!isTichu() && num <= 0, "出价必须大于0!");
            param.totalValue = num;
        } else {
            // 调整部队
            CheckUtils.check(isTichu(), "地主竞价中不能牺牲部队!");
            TTACard card = player.getBuildings().getCard(cardId);
            if (card.cardType == CardType.WONDER) {
                WonderCard wonder = (WonderCard) card;
                CivilCardAbility a = super.getAbilityManager().getAbility(CivilAbilityType.PA_SHIHUANG_TOMB);
                // 检查现有部队数量是否超出出价的部队数量
                CheckUtils.check(num < 0 || num > wonder.getBlues() || num > a.getLimit(), "部队数量错误,不能进行调整!");
                param.setBlueNum(a, num);
            } else {
                CheckUtils.check(card.cardType != CardType.UNIT, "只能选择部队牌!");
                // 检查现有部队数量是否超出出价的部队数量
                CheckUtils.check(num < 0 || num > card.getAvailableCount(), "部队数量错误,不能进行调整!");
                param.setUnitNum((TechCard) card, num);
            }
            // 向操作的玩家刷新当前出价的总值
        }
        this.sendPlayerAuctionValue(gameMode, player, player);
    }

    @Override
    protected void beforeStartListen(TTAGameMode gameMode) throws BoardGameException {
        super.beforeStartListen(gameMode);
        // 监听开始前,为所有玩家创建拍卖参数
        for (TTAPlayer player : gameMode.getGame().getPlayers()) {
            AuctionParam param = new AuctionParam(player, !isWar());
            this.setParam(player.getPosition(), param);
        }
    }

    /**
     * 玩家确认
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected abstract void confirm(TTAGameMode gameMode, BgAction action) throws BoardGameException;

    /**
     * 创建玩家选择部队信息的指令
     *
     * @param gameMode
     * @param receiver
     * @return
     */
    protected BgResponse createAuctionInfoResponse(TTAGameMode gameMode, TTAPlayer receiver) {
        // 发送玩家的部队及拍卖信息
        BgResponse res = CmdFactory.createGameResponse(this.getValidCode(), -1);
        res.setPublicParameter("subact", "loadParam");
        List<Map<String, Object>> list = new ArrayList<>();
        for (TTAPlayer player : gameMode.getGame().getPlayers()) {
            Map<String, Object> map = this.createPlayerAuctionParam(gameMode, player, receiver);
            list.add(map);
        }
        res.setPublicParameter("playersInfo", list);
        // 设置触发器信息
        this.setListenerInfo(res);
        return res;
    }

    /**
     * 创建玩家的拍卖参数
     *
     * @param player
     * @param receiver
     * @return
     */

    protected Map<String, Object> createPlayerAuctionParam(TTAGameMode gameMode, TTAPlayer player, TTAPlayer receiver) {
        Map<String, Object> map = new HashMap<>();
        AuctionParam param = this.getParam(player.getPosition());
        map.put("position", player.getPosition());

        List<Map<String, Object>> unitsInfo = player.getUnitsInfo();
        if (!this.isColony() && (player.getPosition() != this.trigPlayer.getPosition())
                && super.getAbilityManager().hasAbilitiy(CivilAbilityType.PA_SHIHUANG_TOMB)) {
            WonderCard c = (WonderCard) super.getAbilityManager().getAbilityCard(CivilAbilityType.PA_SHIHUANG_TOMB);
            if (c.getBlues() > 0) {
                Map<String, Object> o = new HashMap<>();
                o.put("cardId", c.id);
                o.put("num", c.getBlues());
                unitsInfo.add(o);
            }
        }
        map.put("unitsInfo", unitsInfo);
        List<TTACard> availableBonusCard = new ArrayList<>();
        // 防御和殖民地加值卡应该设置为私有参数
        if (!this.isColony() && ((player.getPosition() == this.trigPlayer.getPosition()) || this.isWar())) {
            // 非殖民时,侵略方或战争不能使用防御/奖励卡
        } else if (super.getAbilityManager().hasAbilitiy(CivilAbilityType.PA_MEIRIN)
                || (gameMode.getGame().isVersion2() && !this.isColony())
                || (super.getAbilityManager().hasAbilitiy(CivilAbilityType.PA_CARD_AS_BONUSCARD) && this.isColony())) {
            // 新版侵略或新版库克,可以使用全部手牌当D
            availableBonusCard.addAll(player.militaryHands.getCards());
        } else {
            availableBonusCard.addAll(player.getBonusCards());
        }
        map.put("bonusCardIds", BgUtils.card2String(availableBonusCard));

        // 如果是receiver玩家正在输入,或者玩家已经输入完成,则向发送该玩家的输入信息
        if (player == receiver || !param.inputing) {
            map.put("auctionParam", param.toMap());
        }
        return map;
    }

    @Override
    protected BgResponse createStartListenCommand(TTAGameMode gameMode, TTAPlayer player) {
        BgResponse res = super.createStartListenCommand(gameMode, player);
        // 设置玩家当前的拍卖信息
        AuctionParam param = this.getParam(player.getPosition());
        res.setPublicParameter("auctionParam", param.toMap());
        return res;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        String subact = action.getAsString("subact");
        if ("adjustUnit".equals(subact)) {
            // 调整部队
            this.adjustUnit(gameMode, action);
        } else if ("adjustBonusCard".equals(subact)) {
            // 调整加值卡
            this.adjustBonusCard(gameMode, action);
        } else if ("confirm".equals(subact)) {
            // 确认拍卖
            this.confirm(gameMode, action);
        } else if ("pass".equals(subact)) {
            // 放弃拍卖
            this.pass(gameMode, action);
        } else {
            throw new BoardGameException("无效的行动代码!");
        }
    }

    /**
     * 取得玩家的拍卖值
     *
     * @param player
     * @return
     */
    protected int getPlayerAuctionValue(TTAPlayer player) {
        AuctionParam param = this.getParam(player.getPosition());
        return param.totalValue;
    }

    /**
     * 是否看的是殖民点数
     *
     * @return
     */
    protected boolean isColony() {
        return false;
    }

    protected boolean isTichu() {
        return false;
    }

    protected boolean isWar() {
        return false;
    }

    @Override
    protected void onPlayerTurn(TTAGameMode gameMode, TTAPlayer player) throws BoardGameException {
        super.onPlayerTurn(gameMode, player);
        // 玩家回合开始时,将其输入状态设为true
        AuctionParam param = this.getParam(player.getPosition());
        param.inputing = true;
    }

    /**
     * 玩家结束
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected abstract void pass(TTAGameMode gameMode, BgAction action) throws BoardGameException;

    protected void sendAutionEnd(TTAGameMode gameMode, TTAPlayer receiver) {
        BgResponse res = CmdFactory.createGameResponse(this.getValidCode(), -1);
        res.setPublicParameter("subact", "end");
        gameMode.getGame().sendResponse(receiver, res);
    }

    /**
     * 向receiver发送player的拍卖信息,receiver为空则向所有玩家发送
     *
     * @param gameMode
     * @param player
     */
    protected void sendPlayerAuctionInfo(TTAGameMode gameMode, TTAPlayer player, TTAPlayer receiver) {
        AuctionParam param = this.getParam(player.getPosition());
        BgResponse res = CmdFactory.createGameResponse(this.getValidCode(), player.getPosition());
        Map<String, Object> auctionInfo = param.toMap();
        if (this.isColony() && player != receiver) {
            auctionInfo.remove("units");
            auctionInfo.put("units", new ArrayList<Map<String, Object>>());
            auctionInfo.remove("bonusCards");
            auctionInfo.put("bonusCards", new ArrayList<Map<String, Object>>());
        }
        res.setPublicParameter("subact", "auctionParam");
        res.setPublicParameter("auctionParam", auctionInfo);
        gameMode.getGame().sendResponse(receiver, res);
    }

    /**
     * 向receiver发送player的拍卖总值,receiver为空则向所有玩家发送
     *
     * @param gameMode
     * @param player
     */
    protected void sendPlayerAuctionValue(TTAGameMode gameMode, TTAPlayer player, TTAPlayer receiver) {
        AuctionParam param = this.getParam(player.getPosition());
        BgResponse res = CmdFactory.createGameResponse(this.getValidCode(), player.getPosition());
        res.setPublicParameter("subact", "auctionValue");
        res.setPublicParameter("auctionValue", param.totalValue);
        gameMode.getGame().sendResponse(receiver, res);
    }

    @Override
    protected void sendPlayerListeningInfo(TTAGameMode gameMode, TTAPlayer r) {
        super.sendPlayerListeningInfo(gameMode, r);
        // 发送玩家的部队及拍卖信息
        BgResponse res = this.createAuctionInfoResponse(gameMode, r);
        // 向receiver发送指令
        gameMode.getGame().sendResponse(r, res);
    }

}
