 package com.f14.net.smartinvoke

import cn.smartinvoke.RemoteObject
import cn.smartinvoke.gui.FlashContainer

class ClientCmdHandler(container: FlashContainer) : RemoteObject(container) {
    init {
        this.createRemoteObject()
    }

    fun onError(msg: String) {
        this.call("onError", arrayOf<Any>(msg))
        // this.asyncCall("onError", new Object[] { msg });

    }

    fun onCommand(cmdstr: String) {
        this.call("onCommand", arrayOf<Any>(cmdstr))
        // this.asyncCall("onCommand", new Object[] { cmdstr });
    }

    fun setRoomInfo(param: Map<*, *>) {
        this.call("setRoomInfo", arrayOf<Any>(param))
    }

    fun onConnection() {
        this.call("onConnection", arrayOf<Any>())
    }

    fun loadModule(path: String) {
        this.call("loadModule", arrayOf<Any>(path))
    }

    /**
     * 显示读取进度条
     */
    fun showTooltips(message: String, timeout: Double) {
        this.call("showTooltips", arrayOf(message, timeout))
    }

    /**
     * 隐藏读取进度条
     */
    fun hideTooltips() {
        this.call("hideTooltips", arrayOf<Any>())
    }
}
