package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ActiveAbilityType
import com.f14.TTA.listener.TTAConfirmListener
import com.f14.bg.exception.BoardGameException

/**
 * 新版丘吉尔的处理器
 * @author 吹风奈奈
 */
class TTAActiveBloodAndThreatExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val l = TTAConfirmListener(gameMode, player, "你是否决定获得3科技和3资源用于部队?")
        val res = gameMode.insertListener(l)
        val confirm = res.getBoolean(player.position)
        if (confirm) {
            param.addTemplateResource(ability)
            player.params.setRoundParameter(ActiveAbilityType.PA_BLOOD_AND_THREAT, true)
        }
        this.actived = confirm
    }

}
