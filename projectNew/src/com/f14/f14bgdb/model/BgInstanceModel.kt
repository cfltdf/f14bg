package com.f14.f14bgdb.model

import com.f14.framework.common.model.BaseModel
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "BG_INSTANCE")
class BgInstanceModel : BaseModel(), Serializable {

    @get:Id
    @get:TableGenerator(name = "PK_GEN", allocationSize = 100, table = "PK_GEN", valueColumnName = "VALUE", pkColumnName = "NAME", pkColumnValue = "SEQ_ID")
    @get:GeneratedValue(strategy = GenerationType.TABLE, generator = "PK_GEN")
    @get:Column(name = "ID", unique = true, nullable = false)
    var id: Long? = null

    @get:Column(name = "CONFIG")
    var config: String? = null

    @get:Column(name = "GAME_TIME")
    var gameTime: Long? = null

    @get:Column(name = "PLAYER_NUM")
    var playerNum: Int? = null

    @get:Column(name = "BOARDGAME_ID", insertable = false, updatable = false)
    var boardgameId: String? = null

    @get:ManyToOne(fetch = FetchType.LAZY)
    @get:JoinColumn(name = "BOARDGAME_ID")
    var boardGame: BoardGameModel? = null

    @get:OneToMany(fetch = FetchType.LAZY, mappedBy = "bgInstance")
    @get:Cascade(CascadeType.ALL, CascadeType.DELETE_ORPHAN)
    @get:JoinColumn(name = "BG_INSTANCE_ID")
    var bgInstanceRecords: MutableList<BgInstanceRecordModel> = ArrayList(0)

    @get:OneToMany(fetch = FetchType.LAZY, mappedBy = "bgInstance")
    @get:Cascade(CascadeType.ALL, CascadeType.DELETE_ORPHAN)
    @get:JoinColumn(name = "BG_INSTANCE_ID")
    var bgReports: MutableList<BgReportModel> = ArrayList(0)

    @get:Temporal(TemporalType.TIMESTAMP)
    @get:Column(name = "CREATETIME")
    override var createTime: Date? = null

    @get:Temporal(TemporalType.TIMESTAMP)
    @get:Column(name = "UPDATETIME")
    override var updateTime: Date? = null

    fun addBgInstanceRecord(o: BgInstanceRecordModel) {
        o.bgInstance = this
        bgInstanceRecords.add(o)
    }

    fun addBgReport(o: BgReportModel) {
        o.bgInstance = this
        bgReports.add(o)
    }

    companion object {
        private const val serialVersionUID = 1L
    }

}
