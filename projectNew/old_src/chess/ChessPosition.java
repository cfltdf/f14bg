package com.f14.chess;


public class ChessPosition {
    public int x;
    public int y;

    public ChessPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }


    public ChessPosition move(ChessPosition dir) {
        return new ChessPosition(x + dir.x, y + dir.y);
    }


    public Boolean equals(ChessPosition o) {
        return this.x == o.x && this.y == o.y;
    }
}
