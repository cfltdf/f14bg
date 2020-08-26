package com.f14.TTA.listener.event;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.CardBoard;
import com.f14.TTA.component.ability.EventAbility;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.consts.TTACmdString;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.TTA.listener.TTAEventListener;
import com.f14.bg.action.BgAction;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;
import com.f14.utils.CommonUtil;


/**
 * 玩家拿牌的事件
 *
 * @author F14eagle
 */
public class TakeCardListener extends TTAEventListener {
    protected int amount;

    protected TTACard card;
    protected int index;
    protected int actionCost;

    public TakeCardListener(EventAbility ability, TTAPlayer trigPlayer, int amount) {
        super(ability, trigPlayer);
        this.amount = amount;
        this.card = null;
    }

    @Override
    protected void beforeStartListen(TTAGameMode gameMode) throws BoardGameException {
        super.beforeStartListen(gameMode);
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam res = new InterruptParam();
        res.set("card", card);
        res.set("index", index);
        res.set("actionCost", actionCost);
        return res;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        boolean confirm = action.getAsBoolean("confirm");
        TTAPlayer player = action.getPlayer();
        if (confirm) {
            CardBoard cb = gameMode.getCardBoard();
            // 玩家从卡牌序列中拿牌
            String cardId = action.getAsString("cardId");
            int actionCost = cb.getCost(cardId, player);
            // 检查玩家是否有足够的内政行动点
            CheckUtils.check(actionCost > amount, "内政行动点不够,你还能使用 " + amount + " 个内政行动点!");
            TTACard card = cb.getCard(cardId);
            // 检查玩家是否可以拿牌
            player.checkTakeCard(card);
            this.index = cb.getCardIndex(cardId);
            this.card = card;
            this.actionCost = actionCost;
        }
        this.setPlayerResponsed(gameMode, player.getPosition());
    }


    @Override
    protected String getActionString() {
        return TTACmdString.ACTION_TAKE_CARD;
    }

    @Override
    protected String getMsg(TTAPlayer player) {
        String msg = "你还有{0}个内政行动点用来拿牌,请选择!";
        msg = CommonUtil.getMsg(msg, amount);
        return msg;
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_EVENT_TAKE_CARD;
    }

}
