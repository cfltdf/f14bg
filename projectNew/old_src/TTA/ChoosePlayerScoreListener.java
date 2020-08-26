package com.f14.TTA.listener;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.card.TTACard;
import com.f14.bg.exception.BoardGameException;

/**
 * 肖邦选择玩家的中断监听器
 *
 * @author 吹风奈奈
 */
public class ChoosePlayerScoreListener extends ChoosePlayerListener {

    public ChoosePlayerScoreListener(TTAPlayer trigPlayer, TTACard card) {
        super(trigPlayer, card);
    }

    @Override
    protected void choosePlayer(TTAGameMode gameMode, TTAPlayer player, TTAPlayer target) throws BoardGameException {
    }

}
