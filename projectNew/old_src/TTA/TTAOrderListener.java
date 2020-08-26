package com.f14.TTA.listener;

import com.f14.TTA.TTAConfig;
import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.TTAReport;
import com.f14.bg.listener.OrderActionListener;

import java.util.List;

/**
 * TTA的玩家顺序监听器
 *
 * @author F14eagle
 */
public abstract class TTAOrderListener extends OrderActionListener<TTAPlayer, TTAConfig, TTAReport> {


    @Override
    protected List<TTAPlayer> getPlayersByOrder(TTAGameMode gameMode) {
        return gameMode.getGame().getPlayersByOrder(gameMode.getGame().getStartPlayer());
    }
}
