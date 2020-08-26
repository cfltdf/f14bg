package com.f14.TTA.listener;

import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.param.RoundParam;
import com.f14.TTA.consts.TTAGameCmd;


/**
 * 激活卡牌能力的监听器
 *
 * @author F14eagle
 */
public abstract class TTAActiveCardListener extends TTAInterruptListener {
    protected RoundParam param;
    protected TTACard activeCard;

    public TTAActiveCardListener(RoundParam param, TTACard card) {
        super(param.player);
        this.param = param;
        this.activeCard = card;
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_ACTIVABLE_CARD;
    }
}
