package com.f14.TTA.listener;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.param.ConfirmParam;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.bg.action.BgAction;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;


/**
 * 确认是否的监听器
 *
 * @author 吹风奈奈
 */
public class TTAConfirmListener extends TTAInterruptListener {
    protected String msg;

    public TTAConfirmListener(TTAPlayer trigPlayer, String msg) {
        super(trigPlayer);
        this.msg = msg;
    }

    @Override
    protected void beforeStartListen(TTAGameMode gameMode) throws BoardGameException {
        super.beforeStartListen(gameMode);
        // 为所有玩家创建选择参数
        for (TTAPlayer player : gameMode.getRealPlayers()) {
            ConfirmParam param = new ConfirmParam();
            this.setParam(player.getPosition(), param);
        }
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        for (TTAPlayer player : this.getListeningPlayers()) {
            ConfirmParam cp = this.getParam(player);
            param.set(player.getPosition(), cp.confirm);
        }
        return param;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        boolean confirm = action.getAsBoolean("confirm");
        TTAPlayer player = action.getPlayer();
        ConfirmParam param = this.getParam(player);
        param.confirm = confirm;

        this.setPlayerResponsed(gameMode, player.getPosition());
    }

    @Override
    protected String getMsg(TTAPlayer player) {
        return msg;
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_EVENT_BUILD;
    }
}
