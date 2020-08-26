package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.bg.exception.BoardGameException

abstract class TSExecutor(protected var trigPlayer: TSPlayer, protected var gameMode: TSGameMode) {

    /**
     * 执行该行动执行器

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    abstract fun execute()
}
