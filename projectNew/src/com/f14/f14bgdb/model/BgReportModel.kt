package com.f14.f14bgdb.model

import com.f14.framework.common.model.BaseModel
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "BG_REPORT")
class BgReportModel : BaseModel(), Serializable {

    @get:Id
    @get:TableGenerator(name = "PK_GEN", allocationSize = 100, table = "PK_GEN", valueColumnName = "VALUE", pkColumnName = "NAME", pkColumnValue = "SEQ_ID")
    @get:GeneratedValue(strategy = GenerationType.TABLE, generator = "PK_GEN")
    @get:Column(name = "ID", unique = true, nullable = false)
    var id: Long? = null

    @get:Column(name = "BG_INSTANCE_ID", insertable = false, updatable = false)
    var bgInstanceId: Long? = null

    @get:Column(name = "DESCR")
    var descr: String? = null

    @get:ManyToOne(fetch = FetchType.LAZY)
    @get:JoinColumn(name = "BG_INSTANCE_ID")
    var bgInstance: BgInstanceModel? = null


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
