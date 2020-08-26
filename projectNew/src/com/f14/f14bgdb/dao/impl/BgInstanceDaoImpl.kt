package com.f14.f14bgdb.dao.impl

import com.f14.f14bgdb.dao.BgInstanceDao
import com.f14.f14bgdb.model.BgInstanceModel
import com.f14.framework.common.dao.BaseDaoHibernate

class BgInstanceDaoImpl : BaseDaoHibernate<BgInstanceModel, Long>(BgInstanceModel::class.java), BgInstanceDao
