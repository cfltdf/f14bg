package com.f14.TS.component

import com.f14.TS.consts.Country
import com.f14.TS.consts.Region
import com.f14.TS.consts.SubRegion
import com.f14.TS.consts.SuperPower
import com.f14.bg.component.Card
import com.f14.bg.component.Convertable
import com.google.gson.annotations.SerializedName

class TSCountry : Card(), Convertable {
    /**
     * 国家
     */
    lateinit var country: Country
    /**
     * 是否战场国
     */
    @SerializedName("battleField")
    var isBattleField: Boolean = false
    /**
     * 安定度
     */
    var stabilization: Int = 0
    /**
     * 属于的大国
     */
    var controlledPower: SuperPower = SuperPower.NONE
    /**
     * 所属区域
     */
    lateinit var region: Region
    /**
     * 所属子区域
     */
    var subRegions: Array<SubRegion> = emptyArray()
    /**
     * 邻国
     */
    var adjacentCountries: Array<Country> = emptyArray()
    /**
     * 临近的大国
     */
    var adjacentPowers: Array<SuperPower> = emptyArray()
    /**
     * 额外的安定度
     */
    var stabilizationBonus = 0
    /**
     * 影响力计数
     */
    @Transient
    private var influenceCounter = InfluenceCounter()

    /**
     * 为指定的势力添加影响力
     * @param power
     * @param num
     */
    fun addInfluence(power: SuperPower, num: Int) {
        this.influenceCounter.addProperty(power, num)
        this.checkControlledPower()
    }

    fun addStabilizationBonus(bonus: Int) {
        this.stabilizationBonus += bonus
        this.checkControlledPower()
    }

    /**
     * 将影响力设置成和country的相同
     * @param country
     */
    fun arrangeInfluence(country: TSCountry) {
        this.customSetInfluence(SuperPower.USSR, country.customGetInfluence(SuperPower.USSR))
        this.customSetInfluence(SuperPower.USA, country.customGetInfluence(SuperPower.USA))
    }

    /**
     * 检查并设置控制的超级大国
     */
    fun checkControlledPower() {
        val usa = this.customGetInfluence(SuperPower.USA)
        val ussr = this.customGetInfluence(SuperPower.USSR)
        when {
            usa - ussr >= this.stabilization -> this.controlledPower = SuperPower.USA
            ussr - usa >= this.stabilization -> this.controlledPower = SuperPower.USSR
            else -> this.controlledPower = SuperPower.NONE
        }
    }


    override fun clone(): TSCountry {
        val res = super.clone() as TSCountry
        res.influenceCounter = this.influenceCounter.clone()
        return res
    }

    /**
     * 取得指定势力的影响力
     * @param power
     */
    fun customGetInfluence(power: SuperPower): Int {
        return this.influenceCounter.getProperty(power)
    }

    /**
     * 为指定的势力设置影响力
     * @param power
     * @param num
     */
    fun customSetInfluence(power: SuperPower, num: Int) {
        this.influenceCounter.setProperty(power, num)
        this.checkControlledPower()
    }

    var defaultUsa: Int = 0
    var defaultUssr: Int = 0

    /**
     * 取得表示国家影响力的字符串
     * @return
     */
    val influenceString: String
        get() = "[${this.customGetInfluence(SuperPower.USA)}${if (this.controlledPower == SuperPower.USA) "(C)" else ""}:${this.customGetInfluence(SuperPower.USSR)}${if (this.controlledPower == SuperPower.USSR) "(C)" else ""}]"

    /**
     * 取得美国影响力
     * @return
     */
    val usaInfluence: Int
        get() = this.customGetInfluence(SuperPower.USA)

    /**
     * 取得苏联影响力
     * @return
     */
    val ussrInfluence: Int
        get() = this.customGetInfluence(SuperPower.USSR)

    /**
     * 判断超级大国在该国是否有影响力
     * @param power
     * @return
     */
    fun hasInfluence(power: SuperPower): Boolean {
        return this.customGetInfluence(power) > 0
    }

    /**
     * 判断该国家是否是指定国家的邻国
     * @param country
     * @return
     */
    fun isAdjacentTo(country: Country): Boolean {
        return this.adjacentCountries.contains(country)
    }

    /**
     * 判断是否被指定超级大国的对手控制
     * @param power
     * @return
     */
    fun isControlledByOpposite(power: SuperPower): Boolean {
        return this.controlledPower != SuperPower.NONE && this.controlledPower != power
    }

    override fun toMap(): Map<String, Any> = super.toMap() + mapOf("country" to this.country, "usa" to this.customGetInfluence(SuperPower.USA), "ussr" to this.customGetInfluence(SuperPower.USSR), "controlledPower" to this.controlledPower, "stabilization" to this.stabilization, "battleField" to this.isBattleField)

    fun init() {
        this.customSetInfluence(SuperPower.USA, this.defaultUsa)
        this.customSetInfluence(SuperPower.USSR, this.defaultUssr)
    }
}
