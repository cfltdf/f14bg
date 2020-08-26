package com.f14.framework.common.dao

import com.f14.framework.common.model.BaseModel
import com.f14.framework.common.model.PageModel
import org.hibernate.SessionFactory

import java.io.Serializable

/**
 * Data Access Object (Dao) interface. This is an interface used to tag our Dao
 * classes and to provide common methods to all Daos.
 *
 * @author F14eagle
 */
interface BaseDao<T : BaseModel, PK : Serializable> {

    /**
     * Generic method used to get all objects of a particular type. This is the
     * same as lookup up all rows in a table.
     * @return List of populated objects
     */
    val all: List<T>

    /**
     * Generic method used to inject the sessionfactory to dao instance.
     *
     * @param sessionFactory the type of hibernate SessionFactory
     */
    fun setSessionFactory(sessionFactory: SessionFactory)

    /**
     * Write the current data into the database
     */
    fun flush()

    /**
     * Generic method to get an object based on class and identifier. An
     * ObjectRetrievalFailureException Runtime Exception is thrown if nothing is
     * found.
     *
     * @param id the identifier (primary key) of the object to get
     * @return a populated object
     * @see org.springframework.orm.ObjectRetrievalFailureException
     */
    operator fun get(id: PK): T?

    /**
     * Generic method to load an object based on class and identifier. An
     * ObjectRetrievalFailureException Runtime Exception is thrown if nothing is
     * found.
     *
     * @param id the identifier (primary key) of the object to load
     * @return a populated object
     * @see org.springframework.orm.ObjectRetrievalFailureException
     */
    fun load(id: PK): T?

    /**
     * Checks for existence of an object of type T using the id arg.
     *
     * @param id
     * @return - true if it exists, false if it doesn't
     */
    fun exists(id: PK): Boolean

    /**
     * Generic method to save an object - handles both update and insert.
     * 该方法会更新object的CREATE_TIME和UPDATE_TIME字段
     *
     * @param obj the object to save
     */
    fun save(obj: T): T

    /**
     * Generic method to save an object - handles both update and insert.
     * 该方法会更新object的UPDATE_TIME字段
     *
     * @param obj the object to save
     */
    fun update(obj: T): T

    /**
     * Generic method to remove an object based on class and id
     *
     * @param id the identifier (primary key) of the object to remove
     */
    fun delete(id: PK)

    /**
     * delete an object
     *
     * @param obj
     */
    fun delete(obj: T)

    /**
     * 根据condition的字段统计记录数
     *
     * @param condition
     * @return
     */
    fun count(condition: T): Int

    /**
     * 根据condition的字段查询
     *
     * @param condition
     * @return
     */
    fun query(condition: T): List<T>

    /**
     * 根据condition的字段查询,并按照order排序
     *
     * @param condition
     * @param order
     * @return
     */
    fun query(condition: T, order: String?): List<T>

    /**
     * 根据condition的属性生成Criteria并查询分页记录
     *
     * @param condition
     * @return
     */
    fun queryByCriteria(condition: T): List<T>

    /**
     * 根据condition的属性生成Criteria并查询分页记录,并按照order排序
     *
     * @param condition
     * @param order
     * @return
     */
    fun queryByCriteria(condition: T, order: String?): List<T>

    /**
     * 根据pm设置的属性查询分页记录
     *
     * @param pm
     * @return
     */
    fun query(pm: PageModel<T>): PageModel<T>

    /**
     * 根据pm设置的属性查询分页记录,并按照order排序
     *
     * @param pm
     * @param order
     * @return
     */
    fun query(pm: PageModel<T>, order: String?): PageModel<T>

    /**
     * 根据pm设置的属性生成Criteria并查询分页记录
     *
     * @param pm
     * @return
     */
    fun queryByCriteria(pm: PageModel<T>): PageModel<T>

    /**
     * 根据pm设置的属性生成Criteria并查询分页记录,并按照order排序
     *
     * @param pm
     * @param order
     * @return
     */
    fun queryByCriteria(pm: PageModel<T>, order: String?): PageModel<T>

}