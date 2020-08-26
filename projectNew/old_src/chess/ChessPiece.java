package com.f14.chess;

import com.f14.bg.component.Card;

public class ChessPiece extends Card {
    public ChessColor color;
    public ChessPower power;
    public ChessPosition[] startPositions;
    public ChessPosition startPosition;

    public ChessColor getColor() {
        return color;
    }

    public void setColor(ChessColor color) {
        this.color = color;
    }

    public ChessPower getPower() {
        return power;
    }

    public void setPower(ChessPower power) {
        this.power = power;
    }

    public ChessPosition[] getStartPositions() {
        return startPositions;
    }

    public void setStartPositions(ChessPosition[] startPositions) {
        this.startPositions = startPositions;
    }
}
