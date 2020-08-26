package com.f14.TTA.executor.event

import com.f14.TTA.component.Chooser
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.MilitaryCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ChooserType
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.EventType

class TTAConfuciousEventExecuter(param: RoundParam, card: MilitaryCard) : TTAEventCardExecutor(param, card) {
    override fun execute() {
        val ability = EventAbility()
        ability.chooser = Chooser().also { it.type = ChooserType.ALL }
        ability.eventType = EventType.SCORE
        ability.property = TTAProperty().also { it.addProperty(CivilizationProperty.SCIENCE, 1) }
        TTAInstantAbilityExecutor(param, ability).execute()
    }

}
