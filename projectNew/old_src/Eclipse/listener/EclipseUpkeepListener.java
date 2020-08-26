package com.f14.Eclipse.listener;

import com.f14.Eclipse.EclipseGameMode;
import com.f14.Eclipse.EclipsePlayer;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.ListenerType;


/**
 * 维护阶段
 *
 * @author f14eagle
 */
public class EclipseUpkeepListener extends EclipseOrderListener {

    public EclipseUpkeepListener(EclipsePlayer startPlayer) {
        super(startPlayer, ListenerType.NORMAL);
    }

    @Override
    protected boolean beforeListeningCheck(EclipseGameMode gameMode, EclipsePlayer player) {
        return false;
    }

    @Override
    protected int getValidCode() {
        return 0;
    }

    @Override
    public void onAllPlayerResponsed(EclipseGameMode gameMode) throws BoardGameException {
        super.onAllPlayerResponsed(gameMode);
        for (EclipsePlayer p : gameMode.getGame().getPlayers()) {
            // 重置玩家配件
            p.initRoundPart();
            // 生产资源
            p.produceResource();
            gameMode.getGame().sendPlayerResourceInfo(p, null);
        }
    }

}
