package com.f14.machikoro.listener;

import com.f14.bg.listener.ListenerType;
import com.f14.bg.listener.OrderActionListener;
import com.f14.machikoro.MachiGameMode;
import com.f14.machikoro.MachiPlayer;

import java.util.List;

public abstract class MachiOrderListener extends OrderActionListener<MachiPlayer, MachiGameMode> {
    protected MachiPlayer startPlayer;

    public MachiOrderListener(MachiPlayer player, ListenerType listenerType) {
        super(listenerType);
        this.startPlayer = player;
    }


    @Override
    protected List<MachiPlayer> getPlayersByOrder(MachiGameMode gameMode) {
        // TODO Auto-generated method stub
        return gameMode.getGame().getPlayersByOrder(startPlayer);
    }

}
