package com.f14.f14bgdb.dao.impl

import com.f14.f14bgdb.dao.CodeDetailDao
import com.f14.f14bgdb.model.CodeDetailModel
import com.f14.framework.common.dao.BaseDaoHibernate

class CodeDetailDaoImpl : BaseDaoHibernate<CodeDetailModel, Long>(CodeDetailModel::class.java), CodeDetailDao
