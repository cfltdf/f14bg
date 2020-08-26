package com.f14.TTA.listener;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.consts.TTAGameCmd;

import java.util.Map;

public abstract class TTAActionRequestListener extends TTAInterruptListener {
    protected TTACard showCard;
    protected String actionString;
    protected String msg;

    public TTAActionRequestListener(TTAPlayer trigPlayer, TTACard showCard, String actionString, String msg) {
        super(trigPlayer);
        this.showCard = showCard;
        this.actionString = actionString;
        this.msg = msg;
        this.addListeningPlayer(trigPlayer);
    }

    protected abstract Map<String, Object> createParam();

    @Override
    protected String getActionString() {
        return this.actionString;
    }

    @Override
    protected String getMsg(TTAPlayer player) {
        return this.msg;
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_ACTION_REQUEST;
    }

    @Override
    protected void onPlayerResponsed(TTAGameMode gameMode, TTAPlayer player) {
        gameMode.getGame().playerRequestEnd(player);
    }

    @Override
    protected void sendStartListenCommand(TTAGameMode gameMode, TTAPlayer player, TTAPlayer receiver) {
        super.sendStartListenCommand(gameMode, player, receiver);
        Map<String, Object> param = this.createParam();
        param.put("code", this.getValidCode());
        gameMode.getGame().sendPlayerActionRequestResponse(player, this.getActionString(),
                this.getMsg(player), showCard, param);
    }

}
