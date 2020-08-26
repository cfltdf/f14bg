package com.f14.F14bg.utils

import com.f14.bg.hall.User

/**
 * 权限管理类

 * @author F14eagle
 */
object PrivUtil {
    /**
     * 权限类型 - 管理员
     */
    const val PRIV_ADMIN = "PRIV_ADMIN"
    /**
     * 管理员名称
     */
    private const val ADMIN = "F14eagle"

    /**
     * 判断用户是否拥有管理员权限
     * @param user
     * @return
     */
    fun hasAdminPriv(user: User): Boolean {
        return user.isAdmin
    }

    /**
     * 判断用户是否拥有指定的权限
     * @param user
     * @param priv
     * @return
     */
    fun hasPriv(user: User, priv: String) = when (priv) {
        PRIV_ADMIN -> when {
            ADMIN.equals(user.loginName, ignoreCase = true) -> true
            else -> user.loginName.toUpperCase().startsWith("F14")
        }
        else -> false
    }
}
