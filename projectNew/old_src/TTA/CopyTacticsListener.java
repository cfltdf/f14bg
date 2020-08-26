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
import com.f14.bg.utils.CheckUtils;

import java.util.List;

/**
 * 拷贝阵型的监听器
 *
 * @author F14eagle
 */
public class CopyTacticsListener extends TTAInterruptListener {

    protected TTACard card = null;

    public CopyTacticsListener(TTAGameMode gameMode, TTAPlayer trigPlayer) {
        super(gameMode, trigPlayer);
        this.addListeningPlayer(trigPlayer);
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        param.set("card", card);
        return param;
    }

    @Override
    protected BgResponse createStartListenCommand(TTAPlayer player) {
        BgResponse res = super.createStartListenCommand(player);
        // 传递当前公共阵型库
        res.setPrivateParameter("multiSelection", false);
        res.setPrivateParameter("cardIds",
                BgUtils.card2String(gameMode.getCardBoard().getPublicTacticsDeck().getCards()));
        return res;
    }

    @Override
    protected void doAction(BgAction action) throws BoardGameException {
        boolean confirm = action.getAsBoolean("confirm");
        TTAPlayer player = action.getPlayer();
        if (confirm) {
            String cardIds = action.getAsString("cardIds");
            List<TTACard> cards = gameMode.getCardBoard().getPublicTacticsDeck().getCards(cardIds);
            CheckUtils.check(cards.size() != 1, "没有找到指定阵型!");
            TTACard card = cards.get(0);
            CheckUtils.check(card == player.getTactics(), "你正在使用这个阵型!");
            this.card = card;
        }
        this.setPlayerResponsed(gameMode, player);
    }


    @Override
    protected String getMsg(TTAPlayer player) {
        return "请选择需要学习的阵型";
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_COPY_TACTICS;
    }
}
