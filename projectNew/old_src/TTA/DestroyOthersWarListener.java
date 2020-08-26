package com.f14.TTA.listener.war;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.card.AttackCard;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.card.TechCard;
import com.f14.TTA.component.param.PopParam;
import com.f14.TTA.consts.TTACmdString;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.TTA.listener.TTAAttackResolutionListener;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.common.ParamSet;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;
import com.f14.utils.CommonUtil;
import com.f14.utils.StringUtils;


/**
 * 摧毁其他玩家一种科技卡上多个工人的攻击结算
 *
 * @author F14eagle
 */
public class DestroyOthersWarListener extends TTAAttackResolutionListener {
    protected PopParam popParam;

    public DestroyOthersWarListener(AttackCard warCard, TTAPlayer trigPlayer, TTAPlayer winner, TTAPlayer loser,
                                    ParamSet warParam) {
        super(warCard, trigPlayer, winner, loser, warParam);
        this.popParam = new PopParam();
    }

    @Override
    protected boolean beforeListeningCheck(TTAGameMode gameMode, TTAPlayer player) {
        return this.cannotPass();
    }

    @Override
    protected void beforeStartListen(TTAGameMode gameMode) throws BoardGameException {
        super.beforeStartListen(gameMode);
    }

    protected boolean cannotPass() {
        // 如果战败方有可选建筑,则需要继续监听
        for (TTACard card : loser.getBuildings().getCards()) {
            if (this.attackCard.loserEffect.test(card) && card.getAvailableCount() > 0) {
                return true;
            }
        }
        return false;
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        param.set(loser.getPosition(), popParam);
        return param;
    }

    @Override
    protected BgResponse createStartListenCommand(TTAGameMode gameMode, TTAPlayer player) {
        BgResponse res = super.createStartListenCommand(gameMode, player);
        // GAME_CODE_EVENT_DESTORY_OTHERS 需要的参数
        int[] availablePositions = new int[]{loser.getPosition()};
        res.setPublicParameter("availablePositions", StringUtils.array2String(availablePositions));
        return res;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        boolean confirm = action.getAsBoolean("confirm");
        if (confirm) {
            int targetPosition = action.getAsInt("targetPosition");
            CheckUtils.check(targetPosition != this.loser.getPosition(), "不能选择指定的玩家!");
            TTAPlayer target = gameMode.getGame().getPlayer(targetPosition);
            String cardId = action.getAsString("cardId");
            TTACard card = target.getPlayedCard(cardId);
            CheckUtils.check(!this.attackCard.loserEffect.test(card), "不能选择这张牌!");
            CheckUtils.check(!card.needWorker() || card.getAvailableCount() <= 0, "这张牌上没有工人!");

            // 结算战败方效果
            int num = Math.min(this.attackCard.loserEffect.amount, card.getAvailableCount());
            popParam.destory((TechCard) card, num);
        } else {
            CheckUtils.check(this.cannotPass(), this.getMsg(player));
        }
        this.setPlayerResponsed(gameMode, player.getPosition());
    }


    @Override
    protected String getActionString() {
        return TTACmdString.ACTION_DESTORY;
    }

    @Override
    protected String getMsg(TTAPlayer player) {
        String msg = "你可以摧毁玩家{0}最多{1}个同类型同等级的城市建筑,请选择!";
        msg = CommonUtil.getMsg(msg, this.loser.getReportString(), this.attackCard.loserEffect.amount);
        return msg;
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_EVENT_DESTORY_OTHERS;
    }

    @Override
    public void onAllPlayerResponsed(TTAGameMode gameMode) throws BoardGameException {

        super.onAllPlayerResponsed(gameMode);
    }
}
