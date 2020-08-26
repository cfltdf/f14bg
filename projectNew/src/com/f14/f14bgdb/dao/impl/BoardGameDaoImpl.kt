package com.f14.f14bgdb.dao.impl

import com.f14.f14bgdb.dao.BoardGameDao
import com.f14.f14bgdb.model.BoardGameModel
import com.f14.framework.common.dao.BaseDaoHibernate

class BoardGameDaoImpl : BaseDaoHibernate<BoardGameModel, String>(BoardGameModel::class.java), BoardGameDao {

    /**
     * 按照中文名称取得游戏对象
     *
     * @param cnname
     * @return
     */

    override fun getBoardGameByCnname(cnname: String): BoardGameModel? {
        val c = BoardGameModel()
        c.cnname = cnname
        val res = query(c)
        return res.firstOrNull()
    }
}
