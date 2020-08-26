package com.f14.chess;

import com.f14.bg.action.BgAction;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.ActionListener;


public class ChessRoundListener extends ActionListener<ChessPlayer, ChessGameMode> {

    @Override
    protected int getValidCode() {
        return ChessConstCmd.GAME_CODE_ROUND_PHASE;
    }

    @Override
    protected void doAction(ChessGameMode gameMode, BgAction action) throws BoardGameException {
        String subact = action.getAsString("subact");
        ChessPlayer player = action.getPlayer();
        if (ChessConsts.MOVE.equals(subact)) {
            gameMode.getGame().requestMove(player, action.getAsInt("x"), action.getAsInt("y"));
            this.setPlayerResponsed(gameMode, player);
        } else if (ChessConsts.REQUEST_DRAW.equals(subact)) {
            gameMode.getGame().requestDraw(player);
        } else if (ChessConsts.RESIGN.equals(subact)) {
            gameMode.getGame().playerResign(player);
        } else {
            throw new BoardGameException("非法操作!");
        }
    }

}
