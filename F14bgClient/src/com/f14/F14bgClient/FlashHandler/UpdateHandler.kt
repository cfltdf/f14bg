package com.f14.F14bgClient.FlashHandler

import cn.smartinvoke.IServerObject

import com.f14.F14bgClient.manager.ManagerContainer

class UpdateHandler : IServerObject {

    /**
     * 更新文件
     */
    fun updateFiles() {
        ManagerContainer.updateManager.updateFiles()
    }

    override fun dispose() {

    }

}
