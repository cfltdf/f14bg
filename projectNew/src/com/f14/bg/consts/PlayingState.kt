package com.f14.bg.consts

/**
 * 玩家状态
 * @author f14eagle
 */
enum class PlayingState {
    /**
     * 等待游戏开始
     */
    WAITING,

    /**
     * 游戏进行中
     */
    PLAYING,

    /**
     * 旁观
     */
    AUDIENCE,

    /**
     * 断线
     */
    LOST_CONNECTION
}
