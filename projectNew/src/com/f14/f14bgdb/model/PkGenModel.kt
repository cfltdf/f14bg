package com.f14.f14bgdb.model

import com.f14.framework.common.model.BaseModel
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "PK_GEN")
class PkGenModel : BaseModel(), Serializable {

    @get:Id
    @get:Column(name = "NAME")
    var name: String? = null

    @get:Column(name = "VALUE")
    var value: Long? = null

    companion object {
        private const val serialVersionUID = 1L
    }

}
