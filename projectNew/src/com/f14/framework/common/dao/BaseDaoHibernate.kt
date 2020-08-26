package com.f14.framework.common.dao

import com.f14.bg.utils.BgUtils
import com.f14.framework.common.model.BaseModel
import com.f14.framework.common.model.PageModel
import org.apache.log4j.Logger
import org.hibernate.Criteria
import org.hibernate.HibernateException
import org.hibernate.Query
import org.hibernate.Session
import org.hibernate.criterion.Example
import org.hibernate.criterion.Order
import org.hibernate.criterion.Projections
import org.springframework.orm.hibernate3.support.HibernateDaoSupport
import java.io.Serializable
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*

abstract class BaseDaoHibernate<T : BaseModel, PK : Serializable>(protected var persistentClass: Class<T>) : HibernateDaoSupport(), BaseDao<T, PK> {
    protected val log: Logger = Logger.getLogger(this.javaClass)

    val currentSession: Session
        get() = session

    @Suppress("UNCHECKED_CAST")
    override val all: List<T>
        get() = hibernateTemplate.loadAll(persistentClass).mapNotNull { it as? T }


    @Suppress("UNCHECKED_CAST")
    override fun get(id: PK): T? {
        return hibernateTemplate.get(persistentClass, id) as? T
    }

    override fun exists(id: PK): Boolean {
        return get(id) != null
    }


    override fun save(obj: T): T {
        val now = Date()
        obj.createTime = now
        obj.updateTime = now
        hibernateTemplate.saveOrUpdate(obj)
        return obj
    }


    override fun update(obj: T): T {
        val now = Date()
        obj.updateTime = now
        hibernateTemplate.saveOrUpdate(obj)
        return obj
    }

    override fun delete(id: PK) {
        hibernateTemplate.delete(get(id))
    }

    override fun delete(obj: T) {
        hibernateTemplate.delete(obj)
    }

    override fun flush() {
        session.flush()
    }


    @Suppress("UNCHECKED_CAST")
    override fun load(id: PK): T? {
        return this.hibernateTemplate.load(persistentClass, id) as? T
    }

    override fun count(condition: T): Int {
        val c = getCriteria(condition)
        return (c.setProjection(Projections.rowCount()).uniqueResult() as Number).toInt()
    }

    override fun query(condition: T): List<T> {
        return query(condition, null)
    }

    @Suppress("UNCHECKED_CAST")
    override fun query(condition: T, order: String?): List<T> {
        val c = getCriteria(condition)
        if (order != null) addOrder(c, order)
        return c.list().mapNotNull { it as? T }
    }

    override fun queryByCriteria(condition: T): List<T> {
        return queryByCriteria(condition, null)
    }

    @Suppress("UNCHECKED_CAST")
    override fun queryByCriteria(condition: T, order: String?): List<T> {
        val c = getQueryCriteria(condition)
        addQueryOrder(c)
        if (order != null) addOrder(c, order)
        return c.list().mapNotNull { it as? T }
    }


    override fun query(pm: PageModel<T>): PageModel<T> {
        return this.query(pm, null)
    }


    @Suppress("UNCHECKED_CAST")
    override fun query(pm: PageModel<T>, order: String?): PageModel<T> {
        val c = getCriteria(pm.condition)
        addPageInfo(c, pm)
        if (order != null) addOrder(c, order)
        val record = c.list().mapNotNull { it as? T }
        pm.records = record
        if (pm.pageIndex > 0 && pm.pageSize > 0) {
            val count = getCriteria(pm.condition)
            val res = getCount(count)
            pm.count = res
        } else {
            pm.count = record.count()
        }
        return pm
    }


    override fun queryByCriteria(pm: PageModel<T>): PageModel<T> {
        return queryByCriteria(pm, null)
    }


    @Suppress("UNCHECKED_CAST")
    override fun queryByCriteria(pm: PageModel<T>, order: String?): PageModel<T> {
        val c = getQueryCriteria(pm.condition)
        addQueryOrder(c)
        if (order != null) addOrder(c, order)
        addPageInfo(c, pm)
        pm.records = c.list().mapNotNull { it as? T }
        val cc = getQueryCriteria(pm.condition)
        pm.count = getCount(cc)
        return pm
    }

    /**
     * 生成queryByCriteria中使用的criteria的方法
     *
     * @return
     */
    protected fun getQueryCriteria(condition: T?): Criteria {
        return getCriteria(condition)
    }

    /**
     * 添加排序
     *
     * @param c
     * @return
     */
    protected fun addQueryOrder(c: Criteria): Criteria {
        return c
    }

    /**
     * 创建默认的criteria
     *
     * @param o
     * @return
     */
    protected fun getCriteria(o: T?): Criteria {
        val c = currentSession.createCriteria(this.persistentClass)
        if (o != null) c.add(Example.create(o))
        return c
    }

    /**
     * 取得查询记录数
     *
     * @param c
     * @return
     */
    protected fun getCount(c: Criteria): Int {
        return c.setProjection(Projections.rowCount()).uniqueResult() as Int
    }

    /**
     * 设置分页信息
     *
     * @param c
     * @param pm
     * @return
     */

    protected fun addPageInfo(c: Criteria, pm: PageModel<*>): Criteria {
        if (pm.pageIndex > 0 && pm.pageSize > 0) {
            c.setFirstResult((pm.pageIndex - 1) * pm.pageSize)
            c.setMaxResults(pm.pageSize)
        }
        return c
    }

    /**
     * 设置分页信息
     *
     * @param q
     * @param pm
     * @return
     */

    protected fun addPageInfo(q: Query, pm: PageModel<*>): Query {
        if (pm.pageIndex > 0 && pm.pageSize > 0) {
            q.setFirstResult((pm.pageIndex - 1) * pm.pageSize)
            q.setMaxResults(pm.pageSize)
        }
        return q
    }

    /**
     * 添加排序
     *
     * @param c
     * @param orderStr example: property1,property2 desc,property3
     * @return
     */

    protected fun addOrder(c: Criteria, orderStr: String): Criteria {
        val orders = BgUtils.string2Array(orderStr)
        for (e in orders) {
            val property = e.trim { it <= ' ' }.split(" ".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            if (property.size == 2 && "desc" == property[1].toLowerCase()) {
                c.addOrder(Order.desc(property[0]))
            } else {
                c.addOrder(Order.asc(property[0]))
            }
        }
        return c
    }

    /**
     * 生成PreparedStatement
     *
     * @param sql
     * @return
     * @throws HibernateException
     * @throws SQLException
     */
    @Throws(HibernateException::class, SQLException::class)
    protected fun createPreparedStatement(sql: String): PreparedStatement {
        return currentSession.connection().prepareStatement(sql)
    }

    /**
     * 生成CallableStatement
     *
     * @param sql
     * @return
     * @throws HibernateException
     * @throws SQLException
     */
    @Throws(HibernateException::class, SQLException::class)
    protected fun createCallableStatement(sql: String): CallableStatement {
        return currentSession.connection().prepareCall(sql)
    }
}
