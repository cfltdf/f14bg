package com.f14.TS.component.condition

import com.f14.TS.component.TSCountry
import com.f14.TS.consts.Country
import com.f14.TS.consts.Region
import com.f14.TS.consts.SubRegion
import com.f14.TS.consts.SuperPower
import com.f14.bg.component.AbstractCondition


class TSCountryCondition : AbstractCondition<TSCountry>() {
    var battleField: Boolean? = null
    var controlledPower: SuperPower = SuperPower.NONE
    var region: Region? = null
    var subRegion: SubRegion? = null
    var country: Country? = null
    var hasUssrInfluence: Boolean? = null
    var hasUsaInfluence: Boolean? = null
    var minimunUssrInfluence: Int? = null
    var minimunUsaInfluence: Int? = null
    var stabilization: Int? = null
    var adjacentTo: Country? = null

    override fun clone(): TSCountryCondition {
        return super.clone() as TSCountryCondition
    }

    override fun test(obj: TSCountry): Boolean = when {
        this.country != null && obj.country != this.country -> false
        this.controlledPower != SuperPower.NONE && obj.controlledPower != this.controlledPower -> false
        this.battleField != null && obj.isBattleField != this.battleField -> false
        this.subRegion != null && !obj.subRegions.contains(this.subRegion!!) -> false
        this.region != null && obj.region != this.region -> false
        this.stabilization != null && obj.stabilization != this.stabilization -> false
        this.hasUssrInfluence != null && this.hasUssrInfluence != obj.hasInfluence(SuperPower.USSR) -> false
        this.hasUsaInfluence != null && this.hasUsaInfluence != obj.hasInfluence(SuperPower.USA) -> false
        this.minimunUssrInfluence != null && obj.ussrInfluence < this.minimunUssrInfluence!! -> false
        this.minimunUsaInfluence != null && obj.usaInfluence < this.minimunUsaInfluence!! -> false
        this.adjacentTo != null && !obj.isAdjacentTo(adjacentTo!!) -> false
        else -> true
    }

}
