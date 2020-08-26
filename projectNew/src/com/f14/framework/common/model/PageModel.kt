package com.f14.framework.common.model

import com.f14.framework.common.Const

/**
 * 分页辅助模型类
 *
 *
 * setCondition 设置查询条件 setPageIndex 设置分页起始页,从1开始 setPageSize
 * 设置页面大小,默认使用Const.PAGE_SIZE的值
 *
 * @param <T>
 * @author F14eagle
</T> */
class PageModel<T> @JvmOverloads constructor(var condition: T? = null, var pageIndex: Int = -1, var pageSize: Int = Const.PAGE_SIZE) {
    var records: List<T>? = null
    var count: Int = 0

    constructor(pageIndex: Int, pageSize: Int) : this(null, pageIndex, pageSize)

}
