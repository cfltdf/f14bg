package com.f14.TTA

import com.f14.F14bg.consts.GameType
import com.f14.F14bg.manager.ResourceManager
import com.f14.TTA.component.card.*
import com.f14.TTA.consts.TTAConsts
import com.f14.bg.action.BgResponse
import com.f14.bg.component.ICondition
import com.f14.bg.utils.BgUtils
import com.f14.utils.ExcelUtils
import com.f14.utils.SequenceUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.util.*
import kotlin.collections.ArrayList

/**
 * TTA的资源管理器

 * @author F14eagle
 */
class TTAResourceManager : ResourceManager() {
    /**
     * 按游戏版本分组的牌堆
     */

    private val groups = HashMap<String, CardGroup>()
    /**
     * 废弃的奇迹牌
     */

    private val flipWonders = ArrayList<WonderCard>()
    /**
     * 卡牌索引表
     */

    private val allCards = HashMap<String, MutableList<TTACard>>()
    /**
     * 地主卡
     */

    private val tichuCard = ArrayList<TTACard>()

    /**
     * 将指定的行动牌按数量克隆并添加到对应的行动牌组
     * @param card
     */
    private fun addToActionDeck(card: ActionCard) {
        val group = this.getGroup(card.gameVersion)
        repeat(card.qty) {
            val c = generateCard(card)
            group.actionCards[card.level].add(c)
        }
    }

    /**
     * 将指定的事件牌按数量克隆并添加到对应的军事牌组
     * @param card
     */
    private fun addToEventDeck(card: EventCard) {
        val group = this.getGroup(card.gameVersion)
        val c = generateCard(card)
        group.militaryCards[card.level].add(c)
    }

    /**
     * 将指定的废弃奇迹牌克隆并添加到废弃奇迹牌堆中
     * @param card
     */
    private fun addToFlipWonder(card: WonderCard) {
        repeat(card.qty) {
            val c = generateCard(card)
            this.flipWonders.add(c)
        }
    }

    /**
     * 添加地主牌
     * @param card
     */
    private fun addToTichuCard(card: TTACard) {
        generateId(card)
        this.tichuCard.add(card)
    }


    /**
     * 将指定的领袖牌克隆并添加到对应的领袖牌组
     * @param card
     */
    private fun addToLeaderDeck(card: LeaderCard) {
        val group = this.getGroup(card.gameVersion)
        val c = generateCard(card)
        group.leaderCards[card.level].add(c)
    }

    /**
     * 将指定的军事牌按数量克隆并添加到对应的军事牌组
     * @param card
     */
    private fun addToMilitaryDeck(card: MilitaryCard) {
        val group = this.getGroup(card.gameVersion)
        repeat(card.qty) {
            val c = generateCard(card)
            group.militaryCards[card.level].add(c)
        }
    }

    /**
     * 将指定的牌克隆并添加到所有玩家起始牌组
     * @param card
     */
    private fun addToStartDeck(card: TTACard) {
        val group = this.getGroup(card.gameVersion)
        repeat(TTAConsts.PLAYER_NUM) { i ->
            val c = generateCard(card)
            val cards = group.startCards[i]
            cards.add(c)
        }
    }

    /**
     * 将指定的科技牌按数量克隆并添加到对应的内政科技牌组
     * @param card
     */
    private fun addToTechDeck(card: TechCard) {
        val group = this.getGroup(card.gameVersion)
        val list = group.techCards[card.level]
        repeat(card.qty) { i ->
            val c = generateCard(card)
            if (i < card.qty2p) list.getValue(2).add(c)
            if (i < card.qty3p) list.getValue(3).add(c)
            if (i < card.qty4p) list.getValue(4).add(c)
        }
    }

    /**
     * 将指定的奇迹牌克隆并添加到对应的奇迹牌组
     * @param card
     */
    private fun addToWonderDeck(card: WonderCard) {
        val group = this.getGroup(card.gameVersion)
        val c = generateCard(card)
        group.wonderCards[card.level].add(c)
    }

    override fun createResourceResponse(): BgResponse {
        val res = super.createResourceResponse()
        res.public("cards", this.getAllCards())
        return res
    }

    /**
     * 设置卡牌id,并加入索引表
     */
    private fun generateId(c: TTACard) {
        c.id = SequenceUtils.generateId(TTA::class.java)
        allCards.computeIfAbsent(c.cardNo) { ArrayList() }.add(c)
    }

    private fun <C: TTACard> generateCard(c: C): C = c.clone().also(this::generateId) as C

    /**
     * 取得指定世纪的行动牌堆副本
     * @param config
     * @param age
     * @return
     */
    fun getActionCards(config: TTAConfig, age: Int): List<TTACard> {
        return config.versions.map(this::getGroup).map { it.actionCards[age] }.map { BgUtils.cloneList(it) }.fold(emptyList(), this::replaceAddCards)
    }

    /**
     * 取得所有卡牌
     * @return
     */
    private fun getAllCards(): Collection<TTACard> {
        val cards = HashSet<TTACard>()
        for (group in this.groups.values) {
            group.startCards.forEach { cards.addAll(it) }
            group.techCards.flatMap { it.values }.forEach { cards.addAll(it) }
            group.wonderCards.forEach { cards.addAll(it) }
            group.leaderCards.forEach { cards.addAll(it) }
            group.actionCards.forEach { cards.addAll(it) }
            group.militaryCards.forEach { cards.addAll(it) }
        }
        cards.addAll(this.flipWonders)
        cards.addAll(this.tichuCard)
        return cards
    }

    /**
     * 取得指定cardNo牌堆的副本
     * @param cardNo
     * @return
     */
    fun getCardByNo(cardNo: String): List<TTACard>? {
        return allCards[cardNo]
    }

    /**
     * 按照config和condition取得对应的牌
     * @param config
     * @param condition
     * @return
     */
    fun getCardsByCondition(config: TTAConfig, condition: ICondition<TTACard>) =
            // 按照config取得所有的牌,并过滤不符合condition的牌
            config.versions.map(this::getGroup)
                    .flatMap { listOf(it.startCards, it.techCards.flatMap { it.values }, it.militaryCards) }
                    .flatten()
                    .flatten()
                    .filter(condition::test)

    /**
     * 取得废弃奇迹牌堆
     * @return
     */
    fun getFlipWonders(): List<WonderCard> {
        return BgUtils.cloneList(this.flipWonders).toList()
    }


    override val gameType: GameType
        get() = GameType.TTA

    /**
     * 取得版本对应的牌组
     * @param version
     * @return
     */
    private fun getGroup(version: String): CardGroup {
        return this.groups.computeIfAbsent(version) { CardGroup() }
    }

    /**
     * 取得指定世纪的领袖牌堆副本
     * @param config
     * @param age
     * @return
     */
    fun getLeaderCards(config: TTAConfig, age: Int): List<TTACard> {
        return config.versions.map(this::getGroup).map { it.leaderCards[age] }.map { BgUtils.cloneList(it) }.fold(emptyList(), this::replaceAddCards)
    }

    /**
     * 取得指定世纪的军事牌堆副本
     * @param config
     * @param age
     * @return
     */
    fun getMilitaryCards(config: TTAConfig, age: Int): List<TTACard> {
        return config.versions.map(this::getGroup).map { it.militaryCards[age] }.map { BgUtils.cloneList(it) }.fold(emptyList(), this::replaceAddCards)
    }

    /**
     * 取得玩家的起始牌组
     * @param config
     * @param player
     * @return
     */
    fun getStartDeck(config: TTAConfig, player: TTAPlayer): List<TTACard> {
        return config.versions.map(this::getGroup).map { it.startCards[player.position] }.map { BgUtils.cloneList(it) }.fold(emptyList(), this::replaceAddCards)
    }

    /**
     * 取得指定世纪的科技牌堆副本
     * @param config
     * @param age
     * @return
     */
    fun getTechCards(config: TTAConfig, age: Int): List<TTACard> {
        return config.versions.map(this::getGroup).mapNotNull { it.techCards[age][config.playerNumber] }.map { BgUtils.cloneList(it) }.fold(emptyList(), this::replaceAddCards)
    }

    /**
     * 取得地主牌
     * @return
     */
    fun getTichuCard(): List<TTACard> {
        return tichuCard
    }

    /**
     * 取得指定世纪的奇迹牌堆副本
     * @param config
     * @param age
     * @return
     */
    fun getWonderCards(config: TTAConfig, age: Int): List<TTACard> {
        return config.versions.map(this::getGroup).map { it.wonderCards[age] }.map { BgUtils.cloneList(it) }.fold(emptyList(), this::replaceAddCards)
    }

    /**
     * 初始化
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun init() {
        readXls("./game/TTA1.xls")
        readXls("./game/TTA2.xls")
    }

    /**
     * 初始化
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun readXls(xlsTable: String) {
        val wb = HSSFWorkbook(BgUtils.getFileInputStream(this, xlsTable))
        // 第一个sheet是玩家起始科技牌组的信息
        ExcelUtils.sheetToList(wb.getSheetAt(0), TechCard::class.java, this::addToStartDeck)
        // 第二个sheet是玩家的起始政府信息
        ExcelUtils.sheetToList(wb.getSheetAt(1), GovermentCard::class.java, this::addToStartDeck)
        // 第三个sheet是所有科技牌堆
        ExcelUtils.sheetToList(wb.getSheetAt(2), TechCard::class.java, this::addToTechDeck)
        // 第四个sheet是所有奇迹牌堆
        ExcelUtils.sheetToList(wb.getSheetAt(3), WonderCard::class.java, this::addToWonderDeck)
        // 第五个sheet是所有领袖牌堆
        ExcelUtils.sheetToList(wb.getSheetAt(4), LeaderCard::class.java, this::addToLeaderDeck)
        // 第六个sheet是所有政府牌堆
        ExcelUtils.sheetToList(wb.getSheetAt(5), GovermentCard::class.java, this::addToTechDeck)
        // 第七个sheet是所有行动牌堆
        ExcelUtils.sheetToList(wb.getSheetAt(6), ActionCard::class.java, this::addToActionDeck)
        // 第八个sheet是所有事件牌堆
        ExcelUtils.sheetToList(wb.getSheetAt(7), EventCard::class.java, this::addToEventDeck)
        // 第九个sheet是所有防御加成牌堆
        ExcelUtils.sheetToList(wb.getSheetAt(8), BonusCard::class.java, this::addToMilitaryDeck)
        // 第十个sheet是所有战争+侵略牌堆
        ExcelUtils.sheetToList(wb.getSheetAt(9), AttackCard::class.java, this::addToMilitaryDeck)
        // 第十一个sheet是所有条约牌堆
        ExcelUtils.sheetToList(wb.getSheetAt(10), PactCard::class.java, this::addToMilitaryDeck)
        // 第十二个sheet是所有战术牌堆
        ExcelUtils.sheetToList(wb.getSheetAt(11), TacticsCard::class.java, this::addToMilitaryDeck)
        // 第十三个sheet是所有额外牌堆,现用于存放废弃奇迹牌
        ExcelUtils.sheetToList(wb.getSheetAt(12), WonderCard::class.java, this::addToFlipWonder)
        if (wb.numberOfSheets > 13) {
            // 第十四个sheet是所有地主牌堆
            ExcelUtils.sheetToList(wb.getSheetAt(13), TTACard::class.java, this::addToTichuCard)
        }

        super.init()
    }

    /**
     * 按游戏版本分组的容器对象
     * @author F14eagle
     */
    class CardGroup {
        /**
         * 起始牌组 Integer表示玩家顺位,每个玩家都有一套相同的起始牌组
         */
        var startCards: List<MutableList<TTACard>>
        /**
         * 按卡牌等级+游戏人数分组的科技牌堆
         */
        var techCards: List<Map<Int, MutableList<TTACard>>>
        /**
         * 按卡牌等级分组的奇迹牌堆
         */
        var wonderCards: List<MutableList<TTACard>>
        /**
         * 按卡牌等级分组的领袖牌堆
         */
        var leaderCards: List<MutableList<TTACard>>
        /**
         * 按卡牌等级分组的行动牌(黄牌)堆
         */
        var actionCards: List<MutableList<TTACard>>
        /**
         * 按卡牌等级分组的军事牌堆
         */
        var militaryCards: List<MutableList<TTACard>>

        /**
         * 分配空间给所有牌堆
         */
        init {
            val ages = 0 until TTAConsts.AGE_COUNT
            startCards = ages.map { ArrayList() }
            wonderCards = ages.map { ArrayList() }
            leaderCards = ages.map { ArrayList() }
            actionCards = ages.map { ArrayList() }
            militaryCards = ages.map { ArrayList() }
            techCards = ages.map { listOf(2, 3, 4)  .map { it to ArrayList<TTACard>() }.toMap() }
        }
    }

    /**
     * 将新牌合并入牌组,并移除原牌组要被新牌替换的牌
     * @param dst 原牌组
     * @param src 新牌
     */
    private fun replaceAddCards(dst: Collection<TTACard>, src: Collection<TTACard>): List<TTACard> {
        val replaceSet = src.mapNotNull(TTACard::replaceNo).flatMap { it.split(",".toRegex()).dropLastWhile(String::isEmpty) }.toSet()
        return dst.filterNot { replaceSet.contains(it.cardNo) } + src
    }
}
