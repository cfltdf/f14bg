package com.f14.f14bgdb.dao

import com.f14.f14bgdb.model.PkGenModel
import com.f14.framework.common.dao.BaseDao

interface PkGenDao : BaseDao<PkGenModel, String> {

    /**
     * 取得指定变量的下一个值,如果没有指定的变量则返回1
     *
     * @param name
     * @return
     */
    fun getNextValue(name: String): Long
}
