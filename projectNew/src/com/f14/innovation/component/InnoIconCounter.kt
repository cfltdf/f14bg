package com.f14.innovation.component

import com.f14.bg.component.Convertable
import com.f14.bg.component.PropertyCounter
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.consts.InnoSplayDirection

class InnoIconCounter : PropertyCounter<InnoIcon>(), Convertable {
    init {
        this.init()
    }

    /**
     * 添加牌上按指定方向展开的符号
     * @param card
     */
    fun addSplayIcons(card: InnoCard, splayDirection: InnoSplayDirection) {
        card.getSplayIcons(splayDirection)?.forEach(this::addPropertyBonus)
    }

    /**
     * 添加牌上的所有符号
     * @param card
     */
    fun addTopIcons(card: InnoCard) {
        card.topIcons().forEach(this::addPropertyBonus)
    }

    private fun init() {
        // 初始化,所有数量最小为0,无上限
        InnoIcon.values().forEach { this.setMinValue(it, 0) }
    }


    override fun toMap() = InnoIcon.values().map { it.toString() to this.getProperty(it) }.toMap()

}
