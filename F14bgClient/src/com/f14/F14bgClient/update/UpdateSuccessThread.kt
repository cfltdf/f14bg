package com.f14.F14bgClient.update

import com.f14.F14bgClient.manager.ManagerContainer

/**
 * 成功更新后调用的线程

 * @author F14eagle
 */
class UpdateSuccessThread : Runnable {
    override fun run() {
        ManagerContainer.shellManager.hideUpdateShell()
        ManagerContainer.updateManager.updateSuccess(true)
    }
}
