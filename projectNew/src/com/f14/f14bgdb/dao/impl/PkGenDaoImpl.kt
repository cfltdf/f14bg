package com.f14.f14bgdb.dao.impl

import com.f14.f14bgdb.dao.PkGenDao
import com.f14.f14bgdb.model.PkGenModel
import com.f14.framework.common.dao.BaseDaoHibernate

class PkGenDaoImpl : BaseDaoHibernate<PkGenModel, String>(PkGenModel::class.java), PkGenDao {

    /**
     * 取得指定变量的下一个值,如果没有指定的变量则返回1
     *
     * @param name
     * @return
     */
    @Synchronized
    override fun getNextValue(name: String): Long {
        var res: Long
        var pk: PkGenModel? = get(name)
        if (pk == null) {
            pk = PkGenModel()
            pk.name = name
            res = 1L
        } else {
            res = pk.value!!
            res += 1
        }
        pk.value = res
        save(pk)
        return res
    }
}
