package com.f14.f14bgdb.dao.impl

import com.f14.f14bgdb.dao.UserDao
import com.f14.f14bgdb.model.UserModel
import com.f14.framework.common.dao.BaseDaoHibernate

class UserDaoImpl : BaseDaoHibernate<UserModel, Long>(UserModel::class.java), UserDao
