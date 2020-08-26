package com.f14.framework.common.service

import com.f14.framework.common.model.BaseModel
import com.f14.framework.common.model.PageModel
import com.f14.framework.exception.BusinessException

import java.io.Serializable

/**
 * @author F14eagle
 */
interface BaseManager<T : BaseModel, PK : Serializable> {

    /**
     * 获取该表所有记录
     *
     * @return List of objects
     */
    val all: List<T>

    /**
     * 根据主键查询出一个实体
     *
     * @param id
     * @return a object *
     */
    operator fun get(id: PK): T?

    /**
     * 判断实体是否存在
     *
     * @param id
     * @return - true if it exists, false if it doesn't
     */
    fun exists(id: PK): Boolean

    /**
     * 保存一个实体，包括update和insert.会设置对象的createTime和updateTime时间
     *
     * @throws BusinessException
     */
    fun save(model: T): T

    /**
     * 保存一个实体，包括update和insert.会设置对象的updateTime时间
     *
     * @throws BusinessException
     */
    fun update(model: T): T

    /**
     * 根据主键删除一个实体
     *
     * @param id
     */
    fun delete(id: PK)

    /**
     * 删除实体
     *
     * @param model
     */
    fun delete(model: T)

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
    fun query(condition: T, order: String): List<T>

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
    fun queryByCriteria(condition: T, order: String): List<T>

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
    fun query(pm: PageModel<T>, order: String): PageModel<T>

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
    fun queryByCriteria(pm: PageModel<T>, order: String): PageModel<T>
}
