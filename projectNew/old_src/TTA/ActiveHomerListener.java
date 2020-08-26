package com.f14.TTA.listener.active;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.param.RoundParam;
import com.f14.TTA.consts.TTACmdString;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.TTA.listener.TTAActiveCardListener;
import com.f14.bg.action.BgAction;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;


public class ActiveHomerListener extends TTAActiveCardListener {
    protected TTACard card;

    public ActiveHomerListener(RoundParam param, TTACard card) {
        super(param, card);
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        param.set("card", card);
        return param;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        boolean confirm = action.getAsBoolean("confirm");
        TTAPlayer player = action.getPlayer();
        if (confirm) {

            String cardId = action.getAsString("cardId");
            TTACard card = player.getPlayedCard(cardId);
            CheckUtils.check(!activeCard.activeAbility.test(card), "不能选择这张牌!");

            this.card = card;
        } else {
        }
        this.setPlayerResponsed(gameMode, player.getPosition());
    }


    @Override
    protected String getActionString() {
        return TTACmdString.ACTION_CHOOSE_WONDER;
    }


    @Override
    protected String getMsg(TTAPlayer player) {
        return "你可以选择一个建成的奇迹，把" + this.activeCard.name + "放在那张奇迹下方,请选择!";
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_EVENT_FLIP_WONDER;
    }
}
