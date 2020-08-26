package com.f14.innovation.component.condition

import com.f14.bg.component.AbstractCondition
import com.f14.innovation.component.InnoCard
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.consts.InnoSplayDirection


class InnoCardCondition : AbstractCondition<InnoCard>() {
    /**
     * 等级
     */
    var level: Int? = null
    /**
     * 允许的最低等级
     */
    var minLevel: Int? = null
    /**
     * 允许的最高等级
     */
    var maxLevel: Int? = null
    /**
     * 颜色
     */
    var color: InnoColor? = null
    /**
     * 符号
     */
    var icons: Array<InnoIcon> = emptyArray()
    /**
     * 展开方向
     */
    var splayDirection: InnoSplayDirection? = null
    /**
     * 卡牌序号
     */
    var cardIndex: Int? = null

    override fun test(obj: InnoCard): Boolean = when {
        this.level != null && this.level != obj.level -> false
        this.minLevel != null && obj.level < this.minLevel!! -> false
        this.maxLevel != null && obj.level > this.maxLevel!! -> false
        this.icons.isNotEmpty() && !obj.containsIcons(*icons) -> false
        this.color != null && this.color != obj.color -> false
        this.cardIndex != null && this.cardIndex != obj.cardIndex -> false
        else -> true
    }

}
