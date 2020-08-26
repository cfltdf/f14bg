package com.f14.net.socket

import java.net.Socket
import java.util.HashMap

object SocketContext {
    var socket = ThreadLocal<Socket>()
    var param = ThreadLocal<MutableMap<Any, Any>>()

    /**
     * 初始化当前线程的信息
     */
    fun init() {
        param.set(HashMap<Any, Any>())
    }

    /**
     * 取得当前线程的socket对象

     * @return
     */
    fun getSocket(): Socket {
        return socket.get()
    }

    /**
     * 设置当前线程的socket对象

     * @param socket
     */
    fun setSocket(socket: Socket) {
        SocketContext.socket.set(socket)
    }

    /**
     * 取得当前线程的所有参数

     * @return
     */
    val parameters: MutableMap<Any, Any>
        get() = param.get()

    /**
     * 设置当前线程的参数

     * @param key
     * *
     * @param value
     */
    fun setParameter(key: Any, value: Any) {
        parameters.put(key, value)
    }

    /**
     * 取得当前线程的参数

     * @param key
     * *
     * @return
     */
    fun getParameter(key: Any): Any? {
        return parameters[key]
    }
}
