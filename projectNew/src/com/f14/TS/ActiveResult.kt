package com.f14.TS


/**
 * 卡牌事件的触发结果

 * @author F14eagle
 */
class ActiveResult(activePlayer: TSPlayer, eventActived: Boolean) {
    /**
     * 事件是否执行
     */
    var eventActived = false
    /**
     * 是否需要等待用户交互
     */
    // public boolean alternate = false;
    /**
     * 触发的玩家
     */

    var activePlayer: TSPlayer? = null

    init {
        this.activePlayer = activePlayer
        this.eventActived = eventActived
    }
}
