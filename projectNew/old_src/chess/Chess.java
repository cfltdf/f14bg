package com.f14.chess;

import com.f14.bg.FixedOrderBoardGame;
import com.f14.bg.common.ParamSet;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.utils.CheckUtils;
import net.sf.json.JSONObject;


public class Chess extends FixedOrderBoardGame<ChessPlayer, ChessGameMode> {
    protected ChessPiece[][] gameBoard;


    @SuppressWarnings("unchecked")
    @Override
    protected ChessConfig createConfig(JSONObject object) throws BoardGameException {
        return new ChessConfig();
    }

    @Override
    public void initConfig() {
        this.config = new ChessConfig();
    }

    @Override
    public void initConst() {
    }

    @Override
    public void initReport() {
        this.report = new ChessReport(this);
    }


    @Override
    public ChessReport getReport() {
        return (ChessReport) this.report;
    }

    @Override
    protected void sendGameInfo(ChessPlayer receiver) throws BoardGameException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void sendInitInfo(ChessPlayer receiver) throws BoardGameException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void sendPlayerPlayingInfo(ChessPlayer receiver) throws BoardGameException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void setupGame() throws BoardGameException {
        this.config.playerNumber = this.getCurrentPlayerNumber();
        this.gameMode = new ChessGameMode(this);
        this.gameBoard = new ChessPiece[9][10];
    }

    public void setPiece(ChessPosition pos, ChessPiece piece) {
        this.gameBoard[pos.x][pos.y] = piece;
    }

    public ChessPiece getPiece(ChessPosition pos) {
        return this.gameBoard[pos.x][pos.y];
    }

    public ChessPiece takePiece(ChessPosition pos) {
        ChessPiece piece = this.gameBoard[pos.x][pos.y];
        this.gameBoard[pos.x][pos.y] = null;
        return piece;
    }

    public void movePiece(ChessPosition src, ChessPosition des) {
        ChessPiece piece = this.takePiece(src);
        this.setPiece(des, piece);
        this.getReport().movePiece(this.getPlayer(piece.color), piece, des);
    }

    public void requestMove(ChessPlayer player, int x, int y) throws BoardGameException {
        ChessPiece piece = this.gameBoard[x][y];
        CheckUtils.checkNull(piece, "没有棋子!");
        CheckUtils.check(piece.color != player.color, "不是你的棋子!");
        ParamSet result = gameMode.insertListener(new CheseMoveListener(player));
        int dx = result.getInteger("x");
        int dy = result.getInteger("y");
        ChessPosition src = new ChessPosition(x, y);
        ChessPosition des = new ChessPosition(dx, dy);
        CheckUtils.check(!this.canMove(src, des), "不能这样移动!");
        this.movePiece(src, des);
        this.checkWin(player);
    }

    private void checkWin(ChessPlayer player) {
        ChessPosition redKing = null;
        ChessPosition blackKing = null;
        for (int x = 0; x < 9; ++x) {
            for (int y = 0; y < 10; ++y) {
                ChessPiece piece = this.gameBoard[x][y];
                if (piece.power == ChessPower.KING) {
                    if (piece.color == ChessColor.RED) {
                        redKing = new ChessPosition(x, y);
                    } else {
                        blackKing = new ChessPosition(x, y);
                    }
                }
            }
        }
        if (redKing == null) {
            this.getBlackPlayer().win = true;
            this.winGame();
        } else if (blackKing == null) {
            this.getRedPlayer().win = true;
            this.winGame();
        } else if (redKing.x == blackKing.x) {
            for (int y = redKing.y + 1; y < blackKing.y; ++y) {
                if (this.gameBoard[redKing.x][y] != null) {
                    return;
                }
            }
            this.getOpposite(player).win = true;
            this.winGame();
        }
    }

    private ChessPlayer getOpposite(ChessPlayer player) {
        for (ChessPlayer p : this.getPlayers()) {
            if (p.color != player.color) {
                return p;
            }
        }
        return null;
    }

    private ChessPlayer getPlayer(ChessColor color) {
        for (ChessPlayer p : this.getPlayers()) {
            if (p.color == color) {
                return p;
            }
        }
        return null;
    }


    private ChessPlayer getRedPlayer() {
        return getPlayer(ChessColor.RED);
    }


    private ChessPlayer getBlackPlayer() {
        return getPlayer(ChessColor.BLACK);
    }

    private boolean canMove(ChessPosition src, ChessPosition des) {
        ChessPiece srcPiece = this.getPiece(src);
        ChessPiece desPiece = this.getPiece(des);
        if (des.x < 0 || des.x > 8 || des.y < 0 || des.y > 9) {
            return false;
        }
        if (srcPiece.color == desPiece.color) {
            return false;
        }
        switch (srcPiece.power) {
            case CHARRIOT: {
                ChessPosition dir;
                if (src.x == des.x) {
                    if (src.y < des.y) {
                        dir = new ChessPosition(0, 1);
                    } else {
                        dir = new ChessPosition(0, -1);
                    }
                } else if (src.y == des.y) {
                    if (src.x < des.x) {
                        dir = new ChessPosition(1, 0);
                    } else {
                        dir = new ChessPosition(-1, 0);
                    }
                } else {
                    return false;
                }
                for (ChessPosition cur = src.move(dir); !cur.equals(des); cur = cur.move(dir)) {
                    if (this.getPiece(cur) != null) {
                        return false;
                    }
                }
                return true;
            }
            case CANNON: {
                ChessPosition dir;
                int count = 0;
                if (src.x == des.x) {
                    if (src.y < des.y) {
                        dir = new ChessPosition(0, 1);
                    } else {
                        dir = new ChessPosition(0, -1);
                    }
                } else if (src.y == des.y) {
                    if (src.x < des.x) {
                        dir = new ChessPosition(1, 0);
                    } else {
                        dir = new ChessPosition(-1, 0);
                    }
                } else {
                    return false;
                }
                for (ChessPosition cur = src.move(dir); !cur.equals(des); cur = cur.move(dir)) {
                    if (this.getPiece(cur) != null) {
                        count++;
                    }
                }
                return desPiece == null ? count == 0 : count == 1;
            }
            case ELEPHANT: {
                if (getColor(des) != srcPiece.color) {
                    return false;
                }
                switch (src.x - des.x) {
                    case 2:
                    case -2: {
                        switch (src.y - des.y) {
                            case 2:
                            case -2:
                                return this.getPiece(new ChessPosition((src.x + des.x) / 2, (src.y + des.y) / 2)) == null;
                            default:
                                return false;
                        }
                    }
                    default:
                        return false;
                }
            }
            case KING:
                if (getColor(des) != srcPiece.color) {
                    return false;
                }
                if (Math.abs(src.x - des.x) + Math.abs(src.y - des.y) == 1) {
                    if (des.x >= 3 && des.x <= 5 && (des.y <= 2 || des.y >= 7)) {
                        return true;
                    }
                }
                return false;
            case KNIGHT:
                switch (src.x - des.x) {
                    case 2:
                    case -2: {
                        switch (src.y - des.y) {
                            case 1:
                            case -1:
                                return this.getPiece(new ChessPosition((src.x + des.x) / 2, src.y)) == null;
                            default:
                                return false;
                        }
                    }
                    case 1:
                    case -1: {
                        switch (src.y - des.y) {
                            case 2:
                            case -2:
                                return this.getPiece(new ChessPosition(src.x, (src.y + des.y) / 2)) == null;
                            default:
                                return false;
                        }
                    }
                    default:
                        return false;
                }
            case MANDARIN:
                if (getColor(des) != srcPiece.color) {
                    return false;
                }
                switch (src.x - des.x) {
                    case 1:
                    case -1: {
                        switch (src.y - des.y) {
                            case 1:
                            case -1:
                                return des.x >= 3 && des.x <= 5 && (des.y <= 2 || des.y >= 7);
                            default:
                                return false;
                        }
                    }
                    default:
                        return false;
                }
            case PAWN:
                switch (srcPiece.color) {
                    case BLACK:
                        if (getColor(src) == ChessColor.BLACK) {
                            return des.x == src.x && des.y == src.y - 1;
                        } else {
                            return des.x == src.x && des.y == src.y - 1 || Math.abs(des.x - src.x) == 1 && des.y == src.y;
                        }
                    case RED:
                        if (getColor(src) == ChessColor.RED) {
                            return des.x == src.x && des.y == src.y + 1;
                        } else {
                            return des.x == src.x && des.y == src.y + 1 || Math.abs(des.x - src.x) == 1 && des.y == src.y;
                        }
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return false;
    }


    private ChessColor getColor(ChessPosition pos) {
        return pos.y < 5 ? ChessColor.RED : ChessColor.BLACK;
    }

    public void requestDraw(ChessPlayer player) throws BoardGameException {
        ParamSet param = gameMode.insertListener(new ChessRequestDrawListener(player));
        if (param.getBoolean("confirm")) {
            this.winGame();
        }
    }

    public void playerResign(ChessPlayer player) {
        player.resign = true;
        this.winGame();
    }

}
