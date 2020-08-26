package com.f14.f14bgdb.dao

import com.f14.f14bgdb.model.BoardGameModel
import com.f14.framework.common.dao.BaseDao


interface BoardGameDao : BaseDao<BoardGameModel, String> {

    /**
     * 按照中文名称取得游戏对象
     *
     * @param cnname
     * @return
     */
    fun getBoardGameByCnname(cnname: String): BoardGameModel?
}
