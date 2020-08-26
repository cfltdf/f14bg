package com.f14.f14bgdb.model

import com.f14.framework.common.model.BaseModel
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "USER")
class UserModel : BaseModel(), Serializable {

    @get:Id
    @get:TableGenerator(name = "PK_GEN", allocationSize = 100, table = "PK_GEN", valueColumnName = "VALUE", pkColumnName = "NAME", pkColumnValue = "SEQ_ID")
    @get:GeneratedValue(strategy = GenerationType.TABLE, generator = "PK_GEN")
    @get:Column(name = "ID", unique = true, nullable = false)
    var id: Long? = null

    @get:Column(name = "LOGINNAME")
    var loginName: String? = null

    @get:Column(name = "PASSWORD")
    var password: String? = null

    @get:Column(name = "USERNAME")
    var userName: String? = null

    @get:Column(name = "UID")
    var uid: Long? = null

    @get:Temporal(TemporalType.TIMESTAMP)
    @get:Column(name = "LOGINTIME")
    var loginTime: Date? = null

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
