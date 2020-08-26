package com.f14.TS.manager

import com.f14.TS.TSGameMode
import com.f14.TS.TSResourceManager
import com.f14.TS.component.TSCountry
import com.f14.TS.consts.Country
import com.f14.TS.consts.SuperPower
import com.f14.bg.component.ICondition
import com.f14.bg.exception.BoardGameException
import java.util.*

/**
 * TS的国家管理器

 * @author F14eagle
 */
class CountryManager(private var gameMode: TSGameMode) {

    private var idcountries: MutableMap<String, TSCountry> = LinkedHashMap()

    private var countries: MutableMap<Country, TSCountry> = LinkedHashMap()

    init {
        this.init()
    }

    /**
     * 添加国家

     * @param o
     */
    private fun addCountry(o: TSCountry) {
        this.idcountries[o.id] = o
        this.countries[o.country] = o
    }

    /**
     * 取得指定超级大国的所有邻国 @param power
     * @return
     */

    fun getAdjacentCountries(power: SuperPower): Set<Country> {
        return countries.values.filter { it.adjacentPowers.contains(power) } // 超级大国的邻国
                .map(TSCountry::country).toSet()
    }

    /**
     * 取得指定国家周围被power控制的国家数量

     * @param country
     * @param power
     * @return
     */
    fun getAdjacentCountriesNumber(country: TSCountry, power: SuperPower) = country.adjacentCountries.map(this::getCountry).filter { it.controlledPower == power }.count()

    /**
     * 取得所有的国家

     * @return
     */
    val allCountries: Collection<TSCountry>
        get() = this.countries.values

    /**
     * 取得所有可以放置影响力的国家 包括,超级大国的邻国,已有影响力的国家及其邻国

     * @param power
     * @return
     */
    fun getAvailableCountries(power: SuperPower): Set<Country> {
        return this.countries.values.filter { it.customGetInfluence(power) > 0 }.map { setOf(it.country) + it.adjacentCountries.toSet() }.fold(this.getAdjacentCountries(power)) { a, b -> a + b }
    }

    /**
     * 取得符合条件的国家数量

     * @param condition
     * @return
     */
    fun getAvailableCountryNum(condition: ICondition<TSCountry>): Int {
        return this.getCountriesByCondition(condition).size
    }

    /**
     * 按照条件取得国家

     * @param condition
     * @return
     */
    fun getCountriesByCondition(condition: ICondition<TSCountry>): List<TSCountry> {
        return allCountries.filter(condition::test)
    }

    /**
     * 按照国家代码取得国家

     * @param country
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCountry(country: Country): TSCountry {
        return countries[country] ?: throw BoardGameException("没有找到指定的对象!")
    }

    /**
     * 按照国家代码取得国家

     * @param country
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCountry(country: String): TSCountry {
        return this.getCountry(Country.valueOf(country))
    }

    /**
     * 按照id取得国家

     * @param countryId
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCountryById(countryId: String): TSCountry {
        return idcountries[countryId] ?: throw BoardGameException("没有找到指定的对象!")
    }

    /**
     * 取得指定国家在调整阵营时,双方的修正值

     * @param country
     * @return
     */
    fun getRealignmentBonus(country: TSCountry): Map<SuperPower, Int> {
        val usaInfluence = country.customGetInfluence(SuperPower.USA)
        val ussrInfluence = country.customGetInfluence(SuperPower.USSR)
        // 每个控制的邻国+1,该国影响力高于对方+1,临近超级大国+1
        var usa = this.getAdjacentCountriesNumber(country, SuperPower.USA)
        if (usaInfluence > ussrInfluence) usa += 1
        if (country.adjacentPowers.contains(SuperPower.USA)) usa += 1
        var ussr = this.getAdjacentCountriesNumber(country, SuperPower.USSR)
        if (ussrInfluence > usaInfluence) ussr += 1
        if (country.adjacentPowers.contains(SuperPower.USSR)) ussr += 1
        return mapOf(SuperPower.USA to usa, SuperPower.USSR to ussr)
    }

    /**
     * 初始化
     */
    private fun init() {
        val res = this.gameMode.game.getResourceManager<TSResourceManager>()
        val countries = res.countriesInstance
        countries.forEach(this::addCountry)
    }

}
