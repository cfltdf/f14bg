package com.f14.bg.action

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */
interface ISendableWith<in T> {
    fun sendResponse(arg: T, res: BgResponse)

    fun sendResponse(arg: T, res: Collection<BgResponse>) = res.forEach { this.sendResponse(arg, it) }
}