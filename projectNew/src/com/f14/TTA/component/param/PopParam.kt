package com.f14.TTA.component.param

import com.f14.TTA.component.card.TechCard
import java.util.*

/**
 * 人口参数

 * @author F14eagle
 */
class PopParam {
    var selectedPopulation = 0
    var shouldLosePopulation = 0
    var loseFirst: Int = 0

    var detail: MutableMap<TechCard, Int> = HashMap()

    /**
     * 拆除建筑

     * @param card
     */
    fun destory(card: TechCard) {
        this.destory(card, 1)
    }

    fun destory(card: TechCard, count: Int) {
        // 设置拆除建筑的数量
        val num = this.detail[card]
        val i = if (num == null) count else num + count
        this.detail[card] = i
        // 已选数量+1
        this.selectedPopulation += count
    }
}
