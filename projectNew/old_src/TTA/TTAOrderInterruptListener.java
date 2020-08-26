package com.f14.TTA.listener;

import com.f14.F14bg.consts.CmdConst;
import com.f14.F14bg.network.CmdFactory;
import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.bg.action.BgResponse;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.ListenerType;
import com.f14.bg.listener.OrderActionListener;

import java.util.List;

/**
 * TTA的中断监听器(玩家按顺序执行)
 *
 * @author F14eagle
 */
public abstract class TTAOrderInterruptListener extends OrderActionListener<TTAPlayer, TTAGameMode> {
    protected TTAPlayer trigPlayer;

    /**
     * 构造函数
     *
     * @param trigPlayer 触发该监听器的玩家
     */
    public TTAOrderInterruptListener(TTAPlayer trigPlayer) {
        super(ListenerType.INTERRUPT);
        this.trigPlayer = trigPlayer;
    }

    @Override
    protected BgResponse createStartListenCommand(TTAGameMode gameMode, TTAPlayer player) {
        BgResponse res = super.createStartListenCommand(gameMode, player);
        res.setPublicParameter("msg", this.getMsg(player));
        // 设置行动字符串
        res.setPublicParameter("actionString", this.getActionString());
        return res;
    }

    /**
     * 取得选择行动字符串
     *
     * @return
     */

    protected String getActionString() {
        return "";
    }

    /**
     * 取得提示文本
     *
     * @param player
     * @return
     */

    protected String getMsg(TTAPlayer player) {
        return "";
    }


    @Override
    protected List<TTAPlayer> getPlayersByOrder(TTAGameMode gameMode) {
        // 从触发玩家开始,按顺序执行
        return gameMode.getGame().getPlayersByOrder(this.trigPlayer);
    }

    /**
     * 刷新玩家的当前提示信息
     *
     * @param player
     * @throws BoardGameException
     */
    public void refreshMsg(TTAGameMode gameMode, TTAPlayer player) throws BoardGameException {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_REFRESH_MSG, player.getPosition());
        res.setPublicParameter("msg", this.getMsg(player));
        gameMode.getGame().sendResponse(player, res);
    }

    @Override
    protected void setListenerInfo(BgResponse res) {
        super.setListenerInfo(res);
        // 设置触发玩家的位置参数
        res.setPublicParameter("trigPlayerPosition", this.trigPlayer.getPosition());
    }

}