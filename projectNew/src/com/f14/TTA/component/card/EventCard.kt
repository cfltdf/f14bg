package com.f14.TTA.component.card

import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.ability.ScoreAbility
import java.util.*

/**
 * 事件牌

 * @author F14eagle
 */
class EventCard : MilitaryCard() {
    var scoreAbilities: MutableList<ScoreAbility> = ArrayList(0)
    var eventAbilities: MutableList<EventAbility> = ArrayList(0)


    override fun clone(): EventCard {
        return super.clone() as EventCard
    }

}
