package com.f14.f14bgdb.service

import com.f14.bg.VPResult
import com.f14.bg.exception.BoardGameException
import com.f14.f14bgdb.model.BgInstanceModel
import com.f14.framework.common.service.BaseManager


interface BgInstanceManager : BaseManager<BgInstanceModel, Long> {

    /**
     * 保存游戏结果
     *
     * @param result
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun saveGameResult(result: VPResult): BgInstanceModel

    /**
     * 保存游戏战报
     *
     * @param o
     * @param descr
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun saveGameReport(o: BgInstanceModel, descr: String)
}
