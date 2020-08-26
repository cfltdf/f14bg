package com.f14.TTA.listener;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.CardBoard;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.consts.RoundStep;
import com.f14.TTA.consts.TTACmdString;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.utils.CheckUtils;
import org.jetbrains.annotations.NotNull;


/**
 * 第一回合的监听器
 *
 * @author F14eagle
 */
public class FirstRoundListener extends TTAOrderListener {

    public FirstRoundListener(@NotNull TTAGameMode gameMode) {
        super(gameMode);
    }

    @Override
    protected BgResponse createStartListenCommand(TTAGameMode gameMode, TTAPlayer player) {
        BgResponse res = super.createStartListenCommand(gameMode, player);
        // 首轮直接进行普通阶段
        res.setPublicParameter("currentStep", RoundStep.NORMAL);
        return res;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        super.doAction(gameMode, action);
        TTAPlayer player = action.getPlayer();
        String subact = action.getAsString("subact");
        if (TTACmdString.ACTION_TAKE_CARD.equals(subact)) {
            this.takeCard(gameMode, action);
        } else if (TTACmdString.ACTION_PASS.equals(subact)) {
            this.setPlayerResponsed(gameMode, player.getPosition());
        } else {
            throw new BoardGameException("第一回合不能进行其他的操作!");
        }
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_FIRST_ROUND;
    }

    @Override
    protected void onStartListen(TTAGameMode gameMode) throws BoardGameException {
        super.onStartListen(gameMode);
        // 为所有玩家创建使用多少行动点的参数
        int point = gameMode.getGame().getConfig().balanced22 ? 0 : 1;
        for (TTAPlayer p : gameMode.getGame().getPlayersByOrder(gameMode.getGame().getStartPlayer())) {
            PointParam param = new PointParam();
            // 玩家可使用行动点的数量等于其顺位
            param.available = point++;
            this.setParam(p.getPosition(), param);
        }
    }

    /**
     * 玩家从卡牌序列中拿牌
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void takeCard(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        String cardId = action.getAsString("cardId");
        CardBoard cb = gameMode.getCardBoard();
        int cost = cb.getCost(cardId, player);
        int index = cb.getCardIndex(cardId);
        // 第一回合时,每个玩家只能使用自己顺位数值的行动点
        PointParam param = this.getParam(player.getPosition());
        CheckUtils.check(cost > param.available, "内政行动点不够,你还能使用 " + param.available + " 个内政行动点!");
        TTACard card = cb.getCard(cardId);
        // 检查玩家是否可以拿牌
        player.checkTakeCard(card);
        gameMode.getGame().playerTakeCard(player, card, index);
        gameMode.getReport().playerTakeCard(player, cost, index, card);
        param.available -= cost;
        if (param.available <= 0) {
            this.setPlayerResponsed(gameMode, player.getPosition());
        }
    }

    /**
     * 使用行动点的参数
     *
     * @author F14eagle
     */
    class PointParam {
        int available = 0;
    }

}
