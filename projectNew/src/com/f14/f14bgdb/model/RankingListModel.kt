package com.f14.f14bgdb.model

import com.f14.framework.common.model.BaseModel
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "RANKING_LIST")
class RankingListModel : BaseModel(), Serializable {

    @get:Id
    @get:TableGenerator(name = "PK_GEN", allocationSize = 100, table = "PK_GEN", valueColumnName = "VALUE", pkColumnName = "NAME", pkColumnValue = "SEQ_ID")
    @get:GeneratedValue(strategy = GenerationType.TABLE, generator = "PK_GEN")
    @get:Column(name = "ID", unique = true, nullable = false)
    var id: Long? = null

    @get:Column(name = "USER_ID")
    var userId: Long? = null

    @get:Column(name = "BOARDGAME_ID")
    var boardGameId: String? = null

    @get:Column(name = "LOGINNAME")
    var loginName: String? = null

    @get:Column(name = "USERNAME")
    var userName: String? = null

    @get:Column(name = "NUM_WINS")
    var numWins: Long? = null

    @get:Column(name = "NUM_LOSES")
    var numLoses: Long? = null

    @get:Column(name = "NUM_TOTAL")
    var numTotal: Long? = null

    @get:Column(name = "RATE")
    var rate: Double? = null

    @get:Column(name = "SCORE")
    var score: Long? = null

    @get:Column(name = "RANK_POINT")
    var rankPoint: Long? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}
