package com.f14.net.smartinvoke

import cn.smartinvoke.RemoteObject
import cn.smartinvoke.gui.FlashContainer

class UpdateCommandHandler(container: FlashContainer) : RemoteObject(container) {
    init {
        this.createRemoteObject()
    }

    fun loadParam(param: Map<String, String>) {
        this.asyncCall("loadParam", arrayOf<Any>(param))
    }

    fun refreshSize(currentSize: Int) {
        this.asyncCall("refreshSize", arrayOf<Any>(currentSize))
    }

}
