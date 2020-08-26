package com.f14.TTA.listener;

import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.ability.EventAbility;

/**
 * TTA的事件监听器
 *
 * @author F14eagle
 */
public abstract class TTAEventListener extends TTAInterruptListener {
    /**
     * 事件卡
     */
    protected EventAbility ability;

    /**
     * 构造函数
     *
     * @param trigPlayer 触发玩家
     */
    public TTAEventListener(EventAbility ability, TTAPlayer trigPlayer) {
        super(trigPlayer);
        this.ability = ability;
    }

    /**
     * 取得监听事件的能力
     *
     * @return
     */
    public EventAbility getEventAbility() {
        return this.ability;
    }

}
