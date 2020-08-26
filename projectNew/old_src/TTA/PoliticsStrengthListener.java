package com.f14.TTA.listener.event;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.ability.EventAbility;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.TTA.listener.TTAEventListener;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.BgUtils;
import com.f14.bg.utils.CheckUtils;
import com.f14.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 新版强权政治摸/弃军事牌的事件
 *
 * @author F14eagle
 */
public class PoliticsStrengthListener extends TTAEventListener {
    List<TTACard> discard = new ArrayList<>();
    int amount;

    public PoliticsStrengthListener(EventAbility ability, TTAPlayer trigPlayer) {
        super(ability, trigPlayer);
        amount = -this.getEventAbility().amount;
    }

    @Override
    protected boolean beforeListeningCheck(TTAGameMode gameMode, TTAPlayer player) {
        // 如果是最后一回合,则改为减分,不需回应
        if (gameMode.gameOver) {
            return false;
        }
        // 如果玩家手牌数小于等于需要弃牌数,则不需回应,弃掉所有牌
        if (player.militaryHands.size() <= amount) {
            discard.addAll(player.militaryHands.getCards());
            return false;
        }
        return true;
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        for (TTAPlayer p : this.listeningPlayers) {
            param.set(p.getPosition(), discard);
        }
        return param;
    }

    @Override
    protected BgResponse createStartListenCommand(TTAGameMode gameMode, TTAPlayer player) {
        BgResponse res = super.createStartListenCommand(gameMode, player);

        // 将军事牌作为参数传递到客户端
        res.setPrivateParameter("multiSelection", true);
        res.setPrivateParameter("cardIds", BgUtils.card2String(player.militaryHands.getCards()));
        return res;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        String cardIds = action.getAsString("cardIds");
        List<TTACard> cards = player.militaryHands.getCards(cardIds);
        CheckUtils.check(cards.size() != amount, "弃牌数量错误," + this.getMsg(player));
        discard.addAll(cards);

        this.setPlayerResponsed(gameMode, player.getPosition());
    }

    @Override
    protected String getMsg(TTAPlayer player) {
        String msg = "你需要弃掉{0}张军事牌!";
        msg = CommonUtil.getMsg(msg, amount);
        return msg;
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_DISCARD_MILITARY;
    }
}
