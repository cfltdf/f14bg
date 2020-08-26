package com.f14.F14bgClient.FlashHandler

import cn.smartinvoke.IServerObject

import com.f14.F14bgClient.manager.ManagerContainer

class QueryHandler : IServerObject {

    /**
     * 查看用户信息
     */
    fun viewUser() {
        val userId = ManagerContainer.shellManager.userShell!!.currentUserId
        if (userId != null) {
            ManagerContainer.actionManager.viewUser(userId)
        }
    }

    override fun dispose() {

    }

}
