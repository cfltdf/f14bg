package com.f14.bg;

import com.f14.F14bg.consts.CmdConst;
import com.f14.F14bg.network.CmdFactory;
import com.f14.bg.action.BgResponse;
import com.f14.bg.exception.BoardGameException;
import com.f14.f14bgdb.F14bgdb;
import com.f14.f14bgdb.model.BgInstance;
import com.f14.f14bgdb.service.BgInstanceManager;
import org.apache.log4j.Logger;


/**
 * 游戏结束阶段,用来计算游戏得分等
 *
 * @author F14eagle
 */
public abstract class GameEndPhase {
    protected Logger log = Logger.getLogger(this.getClass());

    /**
     * 创建游戏结果对象
     *
     * @return
     */
    protected abstract VPResult createVPResult(GameMode gameMode);

    @SuppressWarnings("unused")
    public void execute(GameMode gameMode) throws BoardGameException {
        gameMode.getReport().end();
        VPResult result = this.createVPResult(gameMode);
        // 将结果进行排名
        result.sort();
        // 保存游戏结果
        BgInstanceManager bm = F14bgdb.getBean("bgInstanceManager");
        BgInstance o = bm.saveGameResult(result);
        // 记录游戏得分情况
        gameMode.getReport().result(result);
        // 保存游戏战报(实在太大还是不保存了...)
        // bm.saveGameReport(o, mode.getReport().toJSONString());
        // mode.getReport().print();
        // 发送游戏结果到客户端
        this.sendGameResult(gameMode, result);
        this.sendGameReport(gameMode);
    }

    /**
     * 发送完整战报
     *
     * @param gameMode
     */
    protected void sendGameReport(GameMode gameMode) {
        gameMode.getGame().getRoom().setReport();
        gameMode.getGame().getRoom().sendReport();
    }

    /**
     * 将游戏结果发送到客户端
     *
     * @param result
     */
    protected void sendGameResult(GameMode gameMode, VPResult result) {
        BgResponse res = CmdFactory.createGameResultResponse(CmdConst.GAME_CODE_VP_BOARD, -1);
        res.setPublicParameter("vps", result.toMap());
        gameMode.getGame().sendResponse(res);
    }

}
