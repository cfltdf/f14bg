package com.f14.TTA.listener.active;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.CardBoard;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.param.RoundParam;
import com.f14.TTA.consts.TTACmdString;
import com.f14.TTA.listener.TTAActiveCardListener;
import com.f14.bg.action.BgAction;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;


public class ActiveGetLeaderListener extends TTAActiveCardListener {

    protected String cardId;

    public ActiveGetLeaderListener(RoundParam param, TTACard card) {
        super(param, card);
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        param.set("cardId", cardId);
        return param;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        boolean confirm = action.getAsBoolean("confirm");
        TTAPlayer player = action.getPlayer();
        if (confirm) {
            // 检查选择的卡牌是否可以应用到该能力
            String cardId = action.getAsString("cardId");
            CardBoard cb = gameMode.getCardBoard();
            TTACard card = cb.getCard(cardId);
            CheckUtils.check(!activeCard.activeAbility.test(card), "该能力不能在这张牌上使用!");
            player.checkTakeCard(card);

            this.cardId = cardId;
        }
        this.setPlayerResponsed(gameMode, player.getPosition());
    }


    @Override
    protected String getActionString() {
        return TTACmdString.ACTION_TAKE_CARD;
    }


    @Override
    protected String getMsg(TTAPlayer player) {
        return "请选择要替换的领袖!";
    }

}
