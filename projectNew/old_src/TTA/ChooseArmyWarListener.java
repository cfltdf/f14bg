package com.f14.TTA.listener;

import com.f14.F14bg.network.CmdFactory;
import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.card.AttackCard;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.param.AuctionParam;
import com.f14.TTA.consts.CardType;
import com.f14.TTA.consts.CivilAbilityType;
import com.f14.TTA.consts.CivilizationProperty;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 侵略/战争选择军队的监听器
 *
 * @author F14eagle
 */
public class ChooseArmyWarListener extends ChooseArmyListener {
    protected AttackCard warCard;
    protected TTAPlayer targetPlayer;

    protected TTAPlayer winner = null;

    protected TTAPlayer loser = null;
    protected int result = 0;
    protected int advantage;

    public ChooseArmyWarListener(AttackCard warCard) {
        this(warCard, warCard.owner, warCard.target);
    }

    public ChooseArmyWarListener(AttackCard warCard, TTAPlayer trigPlayer, TTAPlayer targetPlayer) {
        super(trigPlayer);
        this.warCard = warCard;
        this.targetPlayer = targetPlayer;
        this.addListeningPlayer(trigPlayer);
        this.addListeningPlayer(targetPlayer);
    }

    @Override
    protected void adjustBonusCard(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        // 战争时不允许使用加值卡
        CheckUtils.check(this.warCard.cardType == CardType.WAR, "战争时不允许使用加值卡!");
        // 侵略时,只有防守方可以出加值卡
        TTAPlayer player = action.getPlayer();
        CheckUtils.check(this.warCard.cardType == CardType.AGGRESSION && player != this.targetPlayer,
                "侵略时只有防守方可以使用加值卡!");
        super.adjustBonusCard(gameMode, action);
    }

    @Override
    protected void adjustUnit(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        String cardId = action.getAsString("cardId");
        if (cardId != null && !cardId.isEmpty()) {
            TTACard card = player.getPlayedCard(cardId);
            if (card.cardType != CardType.WONDER) {
                CheckUtils.check(gameMode.getGame().isVersion2(), "新版不允许牺牲部队!");
                // 如果是最后一个回合,则不允许牺牲部队
                CheckUtils.check(gameMode.finalRound, "最后一个回合中不允许牺牲部队!");
            }
        }
        super.adjustUnit(gameMode, action);
    }

    @Override
    protected boolean beforeListeningCheck(TTAGameMode gameMode, TTAPlayer player) {
        // 战争时,新版以及老版最后一回合直接结算
        return player.getPosition() != trigPlayer.getPosition() && super.getAbilityManager().hasAbilitiy(CivilAbilityType.PA_SHIHUANG_TOMB) || !((gameMode.getGame().isVersion2() || gameMode.finalRound && player.getPosition() == trigPlayer.getPosition()) && (player.getPosition() == trigPlayer.getPosition() || this.isWar()));
    }

    @Override
    protected void beforeStartListen(TTAGameMode gameMode) throws BoardGameException {
        super.beforeStartListen(gameMode);
        // 监听开始前,为所有玩家创建拍卖参数
        for (TTAPlayer p : this.getListeningPlayers()) {
            AuctionParam param = new AuctionParam(p, this.isColony());
            this.setParam(p, param);
        }
        // 检查进攻方玩家调整军事点数的能力
        AuctionParam param = this.getParam(this.trigPlayer);
        param.military = trigPlayer.getAttackerMilitary(warCard, targetPlayer);
        param = this.getParam(this.targetPlayer);
        param.military = targetPlayer.getDefenceMilitary();
    }

    /**
     * 玩家确认部队
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Override
    protected void confirm(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        AuctionParam param = this.getParam(player.getPosition());
        if (gameMode.getGame().isVersion2()) {
            CheckUtils.check(
                    param.getSelectedBonusCards().size() > player.getProperty(CivilizationProperty.MILITARY_ACTION),
                    "不能使用超出你的军事行动点数的卡牌!");
        }
        // 确认部队,完成回应,等待对方选择部队
        param.inputing = false;
        this.setPlayerResponsed(gameMode, player.getPosition());
        // 向所有玩家刷新当前部队的信息
        this.sendPlayerAuctionInfo(gameMode, player, null);
    }

    /**
     * 创建玩家选择部队信息的指令
     *
     * @param gameMode
     * @param receiver
     * @return
     */

    @Override
    protected BgResponse createAuctionInfoResponse(TTAGameMode gameMode, TTAPlayer receiver) {
        // 发送玩家的部队及拍卖信息
        BgResponse res = CmdFactory.createGameResponse(this.getValidCode(), -1);
        res.setPublicParameter("subact", "loadParam");
        // 只生成触发玩家和目标玩家的拍卖信息
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(this.createPlayerAuctionParam(gameMode, trigPlayer, receiver));
        list.add(this.createPlayerAuctionParam(gameMode, targetPlayer, receiver));
        res.setPublicParameter("playersInfo", list);
        // 设置触发器信息
        this.setListenerInfo(res);
        // 设置战争/侵略牌
        res.setPublicParameter("showCardId", this.warCard.id);
        return res;
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        // 所有玩家都结束后,结算结果
        param.set(trigPlayer.getPosition(), this.getParam(trigPlayer.getPosition()));
        param.set(targetPlayer.getPosition(), this.getParam(targetPlayer.getPosition()));
        return param;
    }

    /**
     * 取得战争中指定玩家的对手
     *
     * @param player
     * @return
     */

    protected TTAPlayer getOpponent(TTAPlayer player) {
        if (player == this.trigPlayer) {
            return this.targetPlayer;
        } else if (player == this.targetPlayer) {
            return this.trigPlayer;
        } else {
            return null;
        }
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_WAR;
    }

    @Override
    protected boolean isWar() {
        return warCard.cardType == CardType.WAR;
    }

    @Override
    public void onAllPlayerResponsed(TTAGameMode gameMode) throws BoardGameException {
        this.sendAutionEnd(gameMode, null);
        super.onAllPlayerResponsed(gameMode);

    }

    /**
     * 玩家结束拍卖
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Override
    protected void pass(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        // 放弃则清空玩家所有的选择
        TTAPlayer player = action.getPlayer();
        AuctionParam param = this.getParam(player.getPosition());
        param.clear();
        // 确认部队,完成回应,等待对方选择部队
        param.inputing = false;
        this.setPlayerResponsed(gameMode, player.getPosition());
        // 向所有玩家刷新当前部队的信息
        this.sendPlayerAuctionInfo(gameMode, player, null);
    }
}
