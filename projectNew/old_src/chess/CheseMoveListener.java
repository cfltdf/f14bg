package com.f14.chess;

import com.f14.bg.action.BgAction;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.ActionListener;
import com.f14.bg.listener.ListenerType;

public class CheseMoveListener extends ActionListener<ChessPlayer, ChessGameMode> {

    public CheseMoveListener(ChessPlayer player) {
        super(ListenerType.INTERRUPT);
        this.addListeningPlayer(player);
    }

    @Override
    protected int getValidCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void doAction(ChessGameMode gameMode, BgAction action) throws BoardGameException {
        // TODO Auto-generated method stub

    }

}
