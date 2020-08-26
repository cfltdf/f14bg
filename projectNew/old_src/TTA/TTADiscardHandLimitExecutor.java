package com.f14.TTA.executor;

import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.param.RoundParam;
import com.f14.TTA.listener.DiscardMilitaryLimitListener;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;

import java.util.List;

/**
 * 回合弃牌的处理器
 *
 * @author 吹风奈奈
 */
public class TTADiscardHandLimitExecutor extends TTAActionExecutor {

    public TTADiscardHandLimitExecutor(RoundParam param) {
        super(param);
    }

    @Override
    public void execute() throws BoardGameException {
        while (player.militaryHands.size() > player.getMilitaryHandLimit()) {
            // 如果军事牌超出手牌上限则需弃牌
            InterruptParam param = gameMode.insertListener(new DiscardMilitaryLimitListener(player));
            List<TTACard> cards = param.get("discards");
            gameMode.getGame().playerDiscardHand(player, cards);
        }
    }

}
