package com.f14.TTA.listener;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.card.PactCard;
import com.f14.bg.exception.BoardGameException;


/**
 * 条约时选择玩家的中断监听器
 *
 * @author F14eagle
 */
public class ChoosePlayerPactListener extends ChoosePlayerListener {

    public ChoosePlayerPactListener(TTAPlayer trigPlayer, PactCard card) {
        super(trigPlayer, card);
    }

    @Override
    protected void choosePlayer(TTAGameMode gameMode, TTAPlayer player, TTAPlayer target) throws BoardGameException {
    }


    @Override
    public PactCard getUsedCard() {
        return (PactCard) super.getUsedCard();
    }

}
