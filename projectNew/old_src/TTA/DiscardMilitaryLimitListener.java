package com.f14.TTA.listener;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.BgUtils;
import com.f14.utils.CommonUtil;

import java.util.List;

/**
 * 弃军事牌的监听器
 *
 * @author F14eagle
 */
public class DiscardMilitaryLimitListener extends TTAInterruptListener {
    protected List<TTACard> discards;

    public DiscardMilitaryLimitListener(TTAPlayer trigPlayer) {
        super(trigPlayer);
        this.addListeningPlayer(trigPlayer);
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        param.set("discards", discards);
        return param;
    }

    @Override
    protected BgResponse createStartListenCommand(TTAGameMode gameMode, TTAPlayer player) {
        BgResponse res = super.createStartListenCommand(gameMode, player);
        // 将军事牌作为参数传递到客户端
        res.setPrivateParameter("multiSelection", true);
        res.setPrivateParameter("cardIds", BgUtils.card2String(player.militaryHands.getCards()));
        return res;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        String cardIds = action.getAsString("cardIds");
        discards = player.militaryHands.getCards(cardIds);
        this.setPlayerResponsed(gameMode, player.getPosition());
    }

    @Override
    protected String getMsg(TTAPlayer player) {
        int num = player.militaryHands.size() - player.getMilitaryHandLimit();
        String msg = "你需要弃掉{0}张军事牌!";
        msg = CommonUtil.getMsg(msg, num);
        return msg;
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_DISCARD_MILITARY;
    }

}
