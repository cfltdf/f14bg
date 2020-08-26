package com.f14.net.smartinvoke

import cn.smartinvoke.RemoteObject
import cn.smartinvoke.gui.FlashContainer

class QueryCommandHandler(container: FlashContainer) : RemoteObject(container) {
    init {
        this.createRemoteObject()
    }

    fun loadUserParam(paramString: String) {
        this.call("loadUserParam", arrayOf<Any>(paramString))
    }

}
