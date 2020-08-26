package com.f14.f14bgdb.service.impl

import com.f14.f14bgdb.dao.CodeDetailDao
import com.f14.f14bgdb.model.CodeDetailModel
import com.f14.f14bgdb.service.CodeDetailManager
import com.f14.framework.common.service.BaseManagerImpl

open class CodeDetailManagerImpl : BaseManagerImpl<CodeDetailModel, Long>(), CodeDetailManager {
    open lateinit var codeDetailDao: CodeDetailDao

    override val dao
        get() = codeDetailDao

}
