package com.f14.framework.common.service

import com.f14.framework.common.dao.BaseDao
import com.f14.framework.common.model.BaseModel
import com.f14.framework.common.model.PageModel
import org.apache.log4j.Logger

import java.io.Serializable

/**
 * @param <T>
 * @param <PK>
 * @author F14eagle
</PK></T> */
abstract class BaseManagerImpl<T : BaseModel, PK : Serializable> : BaseManager<T, PK> {
    protected val log: Logger = Logger.getLogger(this.javaClass)

    abstract val dao: BaseDao<T, PK>

    override val all: List<T>
        get() = dao.all

    override fun delete(id: PK) {
        dao.delete(id)
    }

    override fun delete(model: T) {
        dao.delete(model)
    }

    override fun exists(id: PK): Boolean {
        return dao.exists(id)
    }

    override fun get(id: PK): T? {
        return dao[id]
    }

    override fun query(condition: T): List<T> {
        return dao.query(condition)
    }

    override fun query(pm: PageModel<T>): PageModel<T> {
        return dao.query(pm)
    }

    override fun queryByCriteria(pm: PageModel<T>): PageModel<T> {
        return dao.queryByCriteria(pm)
    }

    override fun save(model: T): T {
        return dao.save(model)
    }

    override fun update(model: T): T {
        return dao.update(model)
    }

    override fun count(condition: T): Int {
        return dao.count(condition)
    }

    override fun query(condition: T, order: String): List<T> {
        return dao.query(condition, order)
    }

    override fun query(pm: PageModel<T>, order: String): PageModel<T> {
        return dao.query(pm, order)
    }

    override fun queryByCriteria(pm: PageModel<T>, order: String): PageModel<T> {
        return dao.queryByCriteria(pm, order)
    }

    override fun queryByCriteria(condition: T): List<T> {
        return dao.queryByCriteria(condition)
    }

    override fun queryByCriteria(condition: T, order: String): List<T> {
        return dao.queryByCriteria(condition, order)
    }

}
