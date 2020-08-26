package com.f14.TTA.listener.war;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.card.AttackCard;
import com.f14.TTA.component.card.TTACard;
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
 * 新版刺杀破坏合并的侵略事件
 *
 * @author F14eagle
 */
public class InfiltrateListener extends TTAAttackResolutionListener {
    String cardId;

    public InfiltrateListener(AttackCard warCard, TTAPlayer trigPlayer, TTAPlayer winner, TTAPlayer loser,
                              ParamSet warParam) {
        super(warCard, trigPlayer, winner, loser, warParam);
    }

    @Override
    protected boolean beforeListeningCheck(TTAGameMode gameMode, TTAPlayer player) {
        return this.cannotPass();
    }

    @Override
    protected void beforeStartListen(TTAGameMode gameMode) throws BoardGameException {
        super.beforeStartListen(gameMode);

        this.cardId = null;
    }

    /**
     * 检查玩家是否可以跳过选择
     *
     * @return
     */
    protected boolean cannotPass() {
        // 如果目标玩家有领袖或在建奇迹,则需要监听结算
        TTACard leader = loser.getLeader();
        TTACard wonder = loser.getUncompleteWonder();
        if (leader == null) {
            if (wonder != null) {
                this.cardId = wonder.id;
            }
            return false;
        } else if (wonder == null) {
            this.cardId = leader.id;
            return false;
        }
        return true;
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        param.set("cardId", cardId);
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
            // 只能选择未完成奇迹或领袖
            TTACard wonder = target.getUncompleteWonder();
            TTACard leader = target.getLeader();
            if (wonder != null && wonder.id.equals(cardId)) {
                this.cardId = cardId;
                // mode.getBoardGame().playerRemoveUncompleteWonder(loser);
                // this.level = wonder.level;
            } else if (leader.id.equals(cardId)) {
                this.cardId = cardId;
                // mode.getBoardGame().playerRemoveCard(loser, leader);
                // this.level = leader.level;
            } else {
                throw new BoardGameException("不能选择这张牌!");
            }
        } else {
            CheckUtils.check(this.cannotPass(), this.getMsg(player));
        }
        this.setPlayerResponsed(gameMode, player.getPosition());
    }


    @Override
    protected String getActionString() {
        return TTACmdString.ACTION_CHOOSE_INFILTRATE;
    }

    @Override
    protected String getMsg(TTAPlayer player) {
        String msg = "你可以移除玩家{0}的领袖或正在建造的奇迹,请选择!";
        msg = CommonUtil.getMsg(msg, this.loser.getReportString());
        return msg;
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_EVENT_DESTORY_OTHERS;
    }
}
