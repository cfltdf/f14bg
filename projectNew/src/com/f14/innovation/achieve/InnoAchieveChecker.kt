package com.f14.innovation.achieve

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer

/**
 * 成就检查器

 * @author F14eagle
 */
abstract class InnoAchieveChecker(protected var gameMode: InnoGameMode) {

    /**
     * 执行检查的逻辑,返回结果

     * @param player

     * @return
     */
    abstract fun check(player: InnoPlayer): Boolean
}
