package com.f14.f14bgdb.model

import com.f14.framework.common.model.BaseModel
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "BG_INSTANCE_RECORD")
class BgInstanceRecordModel : BaseModel(), Serializable {

    @get:Id
    @get:TableGenerator(name = "PK_GEN", allocationSize = 100, table = "PK_GEN", valueColumnName = "VALUE", pkColumnName = "NAME", pkColumnValue = "SEQ_ID")
    @get:GeneratedValue(strategy = GenerationType.TABLE, generator = "PK_GEN")
    @get:Column(name = "ID", unique = true, nullable = false)
    var id: Long? = null

    @get:Column(name = "BG_INSTANCE_ID", insertable = false, updatable = false)
    var bgInstanceId: Long? = null

    @get:Column(name = "USER_ID", insertable = false, updatable = false)
    var userId: Long? = null

    @get:Column(name = "RANK")
    var rank: Int? = null

    @get:Column(name = "SCORE")
    var score: Int? = null

    @get:Column(name = "VP")
    var vp: Int? = null

    @get:Column(name = "DETAIL_STR")
    var detailStr: String? = null

    @get:Column(name = "IS_WINNER")
    var isWinner: Boolean? = null

    @get:Column(name = "RANK_POINT")
    var rankPoint: Int? = null

    @get:ManyToOne(fetch = FetchType.LAZY)
    @get:JoinColumn(name = "BG_INSTANCE_ID")
    var bgInstance: BgInstanceModel? = null

    @get:ManyToOne(fetch = FetchType.LAZY)
    @get:JoinColumn(name = "USER_ID")
    var user: UserModel? = null

    @get:Temporal(TemporalType.TIMESTAMP)
    @get:Column(name = "CREATETIME")
    override var createTime: Date? = null

    @get:Temporal(TemporalType.TIMESTAMP)
    @get:Column(name = "UPDATETIME")
    override var updateTime: Date? = null

    companion object {
        private const val serialVersionUID = 1L
    }

}
