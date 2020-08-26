package com.f14.TTA.executor.action

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.CardAbility
import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException

/**
 * 获得临时资源的行动牌处理器

 * @author 吹风奈奈
 */
class TTAActionTempPropertyExecutor(param: RoundParam, card: ActionCard, private var property: TTAProperty) : TTAActionCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        // 如果存在临时的内政或军事行动点,则直接加上
        val a = CardAbility()
        a.bcs = card.actionAbility.bcs
        a.wcs = card.actionAbility.wcs
        a.property += property
        param.checkActionCardEnhance(a.property)
        // 将临时资源属性添加到玩家的回合参数中
        param.addTemplateResource(a)
        this.completed = true
    }
}
