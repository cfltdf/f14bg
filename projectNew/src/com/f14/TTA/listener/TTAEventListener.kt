package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

abstract class TTAEventListener(gameMode: TTAGameMode, eventAbility: EventAbility, trigPlayer: TTAPlayer) : TTAInterruptListener(gameMode, trigPlayer) {
    /**
     * 事件卡
     */
    var eventAbility: EventAbility = eventAbility
        protected set
}
