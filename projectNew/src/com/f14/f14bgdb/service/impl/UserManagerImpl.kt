package com.f14.f14bgdb.service.impl

import com.f14.bg.exception.BoardGameException
import com.f14.f14bgdb.dao.PkGenDao
import com.f14.f14bgdb.dao.UserDao
import com.f14.f14bgdb.model.UserModel
import com.f14.f14bgdb.service.UserManager
import com.f14.framework.common.dao.BaseDao
import com.f14.framework.common.service.BaseManagerImpl
import com.f14.utils.MD5Utils
import com.f14.utils.StringUtils
import java.util.*

open class UserManagerImpl : BaseManagerImpl<UserModel, Long>(), UserManager {
    open lateinit var userDao: UserDao
    open lateinit var pkGenDao: PkGenDao

    override val dao: BaseDao<UserModel, Long>
        get() = userDao

    /**
     * 创建用户
     *
     * @param user
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun createUser(user: UserModel) {
        if (StringUtils.isEmpty(user.loginName)) {
            throw BoardGameException("请填写帐号名称!")
        }
        if (StringUtils.isEmpty(user.password)) {
            throw BoardGameException("请输入密码!")
        }
        if (StringUtils.isEmpty(user.userName)) {
            throw BoardGameException("请填写用户名!")
        }
        // 帐号名称只允许小写字母
        var condition = UserModel()
        condition.loginName = user.loginName!!.toLowerCase()
        if (userDao.count(condition) > 0) {
            throw BoardGameException("已经存在同名帐号,创建帐号失败!")
        }
        condition = UserModel()
        condition.userName = user.userName
        if (userDao.count(condition) > 0) {
            throw BoardGameException("已经存在相同的用户名,创建帐号失败!")
        }
        val u = UserModel()
        u.loginName = user.loginName!!.toLowerCase()
        // 将密码进行MD5加密
        u.password = MD5Utils.getMD5(user.password!!)
        u.userName = user.userName
        // 自动获取UID
        u.uid = this.pkGenDao.getNextValue(UID_FLAG)
        this.userDao.save(u)
    }

    /**
     * 按照登录名取得用户
     *
     * @param loginName
     * @return
     */

    override fun getUser(loginName: String): UserModel? {
        val u = UserModel()
        u.loginName = loginName.toLowerCase()
        val list = this.userDao.query(u)
        return list.firstOrNull()
    }

    /**
     * 检查用户登录,登录失败则抛出异常
     *
     * @param loginName
     * @param password
     * @return
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    override fun checkLogin(loginName: String, password: String): UserModel {
        val u = getUser(loginName) ?: throw BoardGameException("帐号不存在!")
        if (u.password != MD5Utils.getMD5(password)) {
            throw BoardGameException("密码错误!")
        }
        return u
    }

    /**
     * 执行用户登录,并更新用户最后登录的时间,登录失败则抛出异常
     *
     * @param loginName
     * @param password
     * @return
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    override fun doLogin(loginName: String, password: String): UserModel {
        val u = checkLogin(loginName, password)
        u.loginTime = Date()
        update(u)
        return u
    }

    /**
     * 更新密码为加密格式
     */
    override fun updatePassword() {
        val users = query(UserModel())
        for (u in users) {
            u.password = MD5Utils.getMD5(u.password!!)
            update(u)
        }
    }

    companion object {
        private const val UID_FLAG = "UID"
    }

}
