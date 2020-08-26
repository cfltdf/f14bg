package com.f14.TTA.listener;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.param.AuctionParam;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;


/**
 * 拍卖殖民地的监听器
 *
 * @author F14eagle
 */
public class ChooseArmyTichuListener extends ChooseArmyListener {
    protected TTACard tichuCard;
    protected TTAPlayer topPlayer;

    public ChooseArmyTichuListener(TTAPlayer trigPlayer, TTACard tichuCard) {
        super(trigPlayer);
        this.tichuCard = tichuCard;
    }

    @Override
    protected boolean beforeListeningCheck(TTAGameMode gameMode, TTAPlayer player) {
        // 到玩家回合时,如果当前玩家是拍卖最高价的玩家时,则无需进行拍卖
        return (this.topPlayer != player && !player.resigned);
    }

    /**
     * 玩家确认拍卖
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Override
    protected void confirm(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        CheckUtils.check(player == this.topPlayer, "不能对自己出价!");
        AuctionParam param = this.getParam(player.getPosition());
        if (this.topPlayer != null) {
            // 如果存在最高出价的玩家,则需要检查出价是否高于他
            CheckUtils.check(param.totalValue < this.getCurrentAuctionValue(), "让分必须大于当前出价者!");
        }
        gameMode.getReport().playerBid(player, param.totalValue);
        // 出价成功,暂时完成回应,等待下一玩家出价
        this.topPlayer = player;
        param.inputing = false;
        this.setPlayerResponsedTemp(gameMode, player);
        // 向所有玩家刷新当前出价的信息
        this.sendPlayerAuctionInfo(gameMode, player, null);
    }

    @Override
    protected BgResponse createAuctionInfoResponse(TTAGameMode gameMode, TTAPlayer receiver) {
        BgResponse res = super.createAuctionInfoResponse(gameMode, receiver);
        // 设置拍卖的殖民地
        res.setPublicParameter("showCardId", this.tichuCard.id);
        return res;
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam result = new InterruptParam();
        if (this.topPlayer != null) {
            AuctionParam param = this.getParam(this.topPlayer.getPosition());
            result.set("topPlayer", this.topPlayer);
            result.set(this.topPlayer.getPosition(), param);
        }
        return result;
    }

    /**
     * 取得当前拍卖值
     *
     * @return
     */
    private int getCurrentAuctionValue() {
        if (this.topPlayer == null) {
            return 0;
        } else {
            return this.getPlayerAuctionValue(this.topPlayer);
        }
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_AUCTION_TERRITORY;
    }

    @Override
    protected boolean isTichu() {
        return true;
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
        TTAPlayer player = action.getPlayer();
        AuctionParam param = this.getParam(player.getPosition());
        param.pass = true;
        param.inputing = false;
        this.setPlayerResponsed(gameMode, player.getPosition());
        // 向所有玩家刷新当前出价的信息
        this.sendPlayerAuctionInfo(gameMode, player, null);
    }
}
