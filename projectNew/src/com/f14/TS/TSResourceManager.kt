package com.f14.TS

import com.f14.F14bg.consts.GameType
import com.f14.F14bg.manager.ResourceManager
import com.f14.TS.component.TSCard
import com.f14.TS.component.TSCountry
import com.f14.TS.component.TSReplaceCard
import com.f14.TS.component.TSZeroCard
import com.f14.bg.action.BgResponse
import com.f14.bg.utils.BgUtils
import com.f14.utils.ExcelUtils
import com.f14.utils.SequenceUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.util.*

class TSResourceManager : ResourceManager() {
    /**
     * 国家对象的缓存
     */
    private var countries: MutableMap<String, TSCountry> = LinkedHashMap()
    /**
     * 卡牌对象的缓存
     */
    private var cards: MutableMap<String, MutableMap<String, TSCard>> = HashMap()
    /**
     * 第零回合包卡牌
     */
    private var zeroCards: MutableMap<String, TSZeroCard> = HashMap(0)
    /**
     * 替换卡牌
     */
    private var replaceCards: MutableMap<String, TSReplaceCard> = HashMap(0)

    /**
     * 添加卡牌
     * @param c
     */
    private fun addCard(c: TSCard) {
        val cards = this.getCardGroup(c.gameVersion)
        cards[c.id] = c
    }

    /**
     * 添加国家
     * @param c
     */
    private fun addCountry(c: TSCountry) {
        c.init()
        this.countries[c.id] = c
    }

    override fun createResourceResponse(): BgResponse {
        val res = super.createResourceResponse()
        res.public("cards", this.allCards)
        res.public("countries", this.countries.values)
        return res
    }

    /**
     * 取得所有卡牌
     * @return
     */

    private val allCards: Collection<TSCard>
        get() {
            val res = LinkedHashSet<TSCard>()
            this.cards.values.forEach { res.addAll(it.values) }
            res.addAll(zeroCards.values)
            res.addAll(replaceCards.values)
            return res
        }

    /**
     * 按照游戏版本取得卡牌组
     * @param version
     * @return
     */
    private fun getCardGroup(version: String): MutableMap<String, TSCard> {
        return this.cards.computeIfAbsent(version) { LinkedHashMap() }
    }

    /**
     * 按照设置取得所有卡牌的实例
     * @param config
     * @return
     */

    fun getCardsInstanceByConfig(config: TSConfig): Collection<TSCard> {
        return config.versions.mapNotNull { this.cards[it] }.flatMap { BgUtils.cloneList(it.values) }
    }

    /**
     * 取得所有国家的实例
     * @return
     */
    val countriesInstance: Collection<TSCountry>
        get() {
            val res = ArrayList<TSCountry>()
            res.addAll(BgUtils.cloneList(this.countries.values))
            return res
        }


    override val gameType: GameType
        get() = GameType.TS


    fun getReplaceCards(): Collection<TSReplaceCard> {
        return BgUtils.cloneList(replaceCards.values)
    }


    fun allZeroCards(): Map<Int, Collection<TSZeroCard>> {
        val res = HashMap<Int, MutableCollection<TSZeroCard>>()
        for (card in this.zeroCards.values) {
            val group = card.zeroGroup
            if (!res.containsKey(group)) {
                res[group] = ArrayList()
            }
            res[group]!!.add(card.clone())
        }
        return res
    }

    @Throws(Exception::class)
    override fun init() {
        val wb = HSSFWorkbook(BgUtils.getFileInputStream(this, "./game/TS.xls"))
        // 第一个sheet是国家的信息
        ExcelUtils.sheetToList(wb.getSheetAt(0), TSCountry::class.java) {
            it.id = SequenceUtils.generateId(TSCountry::class.java)
            this.addCountry(it)
        }
        // 第二个sheet是卡牌的信息
        ExcelUtils.sheetToList(wb.getSheetAt(1), TSCard::class.java) {
            it.id = SequenceUtils.generateId(TS::class.java)
            this.addCard(it)
        }
        // 第三个sheet是第零回合包卡牌的信息
        ExcelUtils.sheetToList(wb.getSheetAt(2), TSZeroCard::class.java) {
            it.id = SequenceUtils.generateId(TS::class.java)
            zeroCards[it.id] = it
        }
        // 第三个sheet是第用于替换的卡牌的信息
        ExcelUtils.sheetToList(wb.getSheetAt(3), TSReplaceCard::class.java) {
            it.id = SequenceUtils.generateId(TS::class.java)
            replaceCards[it.id] = it
        }
        super.init()
    }

}
