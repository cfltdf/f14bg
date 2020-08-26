package com.f14.TTA.component.card

import com.f14.TTA.TTAPlayer

/**
 * 持续生效的卡牌接口

 * @author F14eagle
 */
interface IOvertimeCard {
    var a: TTAPlayer?
    var b: TTAPlayer?
    var owner: TTAPlayer?
    var target: TTAPlayer?
}
