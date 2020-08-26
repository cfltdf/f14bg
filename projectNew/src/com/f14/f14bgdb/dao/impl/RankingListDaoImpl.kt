package com.f14.f14bgdb.dao.impl

import com.f14.f14bgdb.dao.RankingListDao
import com.f14.f14bgdb.model.RankingListModel
import com.f14.framework.common.dao.BaseDaoHibernate
import org.hibernate.criterion.Restrictions

class RankingListDaoImpl : BaseDaoHibernate<RankingListModel, Long>(RankingListModel::class.java), RankingListDao {

    /**
     * 按照用户id和游戏id取得排行榜对象
     *
     * @param userId
     * @param boardGameId
     * @return
     */

    override fun getRankingList(userId: Long, boardGameId: String): RankingListModel? {
        val o = RankingListModel()
        o.userId = userId
        o.boardGameId = boardGameId
        val list = query(o)
        return list.firstOrNull()
    }

    /**
     * 按照条件查询排行榜, 游戏数低于20的不做排行
     *
     * @param condition
     * @param order
     * @return
     */
    override fun queryRankingList(condition: RankingListModel, order: String): List<RankingListModel> {
        val c = getCriteria(condition)
        c.add(Restrictions.ge("numTotal", 20L))
        addOrder(c, order)
        return c.list().map { it as RankingListModel }
    }

}
