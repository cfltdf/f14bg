package com.f14.chess;

public class ChessConstCmd {
    /**
     * 代码类型 - 游戏指令 - 刷新游戏基本信息
     */
    public static final int GAME_CODE_BASE_INFO = 0x4000;
    /**
     * 代码类型 - 游戏指令 - 刷新玩家的基本信息
     */
    public static final int GAME_CODE_PLAYER_INFO = 0x4001;
    /**
     * 代码类型 - 游戏指令 - 刷新玩家的按键信息
     */
    public static final int GAME_CODE_PLAYER_BUTTON = 0x4002;
    /**
     * 代码类型 - 游戏指令 - 正常回合阶段
     */
    public static final int GAME_CODE_ROUND_PHASE = 0x4403;
    /**
     * 代码类型 - 游戏指令 - 移动棋子
     */
    public static final int GAME_CODE_REQUEST_MOVE = 0x4404;
    /**
     * 代码类型 - 游戏指令 - 提议和棋
     */
    public static final int GAME_CODE_REQUEST_DRAW = 0x4405;
}
