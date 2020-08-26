package com.f14.bg.chat

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */
interface IChatableWith<in T> {
    /**
     * 发送消息
     * @param message
     */
    fun sendMessage(arg: T, message: Message)
}
