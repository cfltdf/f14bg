package com.f14.chess;

import com.f14.bg.GameMode;
import com.f14.bg.exception.BoardGameException;


public class ChessGameMode extends GameMode<ChessPlayer> {

    public ChessGameMode(Chess chess) {
        this.game = chess;
    }


    @SuppressWarnings("unchecked")
    @Override
    public Chess getGame() {
        return (Chess) this.game;
    }

    @Override
    protected boolean isGameOver() {
        return false;
    }

    @Override
    protected void round() throws BoardGameException {
        this.addListener(new ChessRoundListener());
    }

    @Override
    protected void setupGame() throws BoardGameException {
        ChessResourceManager rm = this.getGame().getResourceManager();
        for (ChessPiece piece : rm.getPieces()) {
            this.getGame().setPiece(piece.startPosition, piece);
        }
    }

}
