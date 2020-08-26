package com.f14.innovation.listener.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoReturnScoreListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import kotlin.math.floor

/**
 * #101-数据库 监听器

 * @author F14eagle
 */
class InnoCustom101Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoReturnScoreListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    init {
        this.setNum()
    }

    private fun setNum() {
        val player = this.targetPlayer
        this.initParam!!.num = floor((player.scores.size() / 2).toDouble()).toInt()
    }

}
