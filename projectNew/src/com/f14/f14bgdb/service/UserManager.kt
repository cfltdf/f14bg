package com.f14.f14bgdb.service

import com.f14.bg.exception.BoardGameException
import com.f14.f14bgdb.model.UserModel
import com.f14.framework.common.service.BaseManager


interface UserManager : BaseManager<UserModel, Long> {

    /**
     * 创建用户
     *
     * @param user
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun createUser(user: UserModel)

    /**
     * 按照登录名取得用户
     *
     * @param loginName
     * @return
     */
    fun getUser(loginName: String): UserModel?

    /**
     * 检查用户登录,登录失败则抛出异常
     *
     * @param loginName
     * @param password
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkLogin(loginName: String, password: String): UserModel

    /**
     * 执行用户登录,并更新用户最后登录的时间,登录失败则抛出异常
     *
     * @param loginName
     * @param password
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun doLogin(loginName: String, password: String): UserModel

    /**
     * 更新密码为加密格式
     */
    fun updatePassword()
}
