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

import java.util.List;

public class ChooseMilitaryCardListener extends TTAInterruptListener {
    protected List<TTACard> cards;
    protected int index;

    protected String cardId;

    public ChooseMilitaryCardListener(TTAPlayer trigPlayer, List<TTACard> cards, int index) {
        super(trigPlayer);
        this.addListeningPlayer(trigPlayer);
        this.cards = cards;
        this.index = index;
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        param.set("cardId", cardId);
        return param;
    }

    @Override
    protected BgResponse createStartListenCommand(TTAGameMode gameMode, TTAPlayer player) {
        BgResponse res = super.createStartListenCommand(gameMode, player);
        // 将军事牌作为参数传递到客户端
        res.setPrivateParameter("multiSelection", false);
        res.setPrivateParameter("cardIds", BgUtils.card2String(cards));
        return res;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        cardId = action.getAsString("cardIds");
        if (cardId == null || cardId.isEmpty()) {
            throw new BoardGameException("请选择1张牌!");
        }
        this.setPlayerResponsed(gameMode, player.getPosition());
    }


    @Override
    protected String getMsg(TTAPlayer player) {
        return "请选择第" + index + "张军事牌!";
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_DISCARD_MILITARY;
    }
}
