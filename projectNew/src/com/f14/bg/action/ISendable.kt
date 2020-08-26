package com.f14.bg.action

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */
interface ISendable {
    fun sendResponse(res: BgResponse)
    fun sendResponse(res: Collection<BgResponse>) = res.forEach(this::sendResponse)
}