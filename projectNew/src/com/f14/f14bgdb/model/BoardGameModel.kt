package com.f14.f14bgdb.model

import com.f14.framework.common.model.BaseModel
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "BOARDGAME")
class BoardGameModel : BaseModel(), Serializable {

    @get:Id
    @get:Column(name = "ID")
    var id: String? = null

    @get:Column(name = "CNNAME")
    var cnname: String? = null

    @get:Column(name = "ENNAME")
    var enname: String? = null

    @get:Column(name = "MIN_PLAYER_NUMBER")
    var minPlayerNumber: Int? = null

    @get:Column(name = "MAX_PLAYER_NUMBER")
    var maxPlayerNumber: Int? = null

    @get:Column(name = "GAME_CLASS")
    var gameClass: String? = null

    @get:Column(name = "PLAYER_CLASS")
    var playerClass: String? = null

    @get:Column(name = "RESOURCE_CLASS")
    var resourceClass: String? = null

    companion object {
        private const val serialVersionUID = 1L
    }

}
