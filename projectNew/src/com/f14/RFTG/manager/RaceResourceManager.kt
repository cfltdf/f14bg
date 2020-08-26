package com.f14.RFTG.manager

import com.f14.F14bg.consts.GameType
import com.f14.F14bg.manager.ResourceManager
import com.f14.RFTG.RFTG
import com.f14.RFTG.RaceConfig
import com.f14.RFTG.card.Goal
import com.f14.RFTG.card.RaceCard
import com.f14.bg.action.BgResponse
import com.f14.bg.utils.BgUtils
import com.f14.utils.ExcelUtils
import com.f14.utils.SequenceUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.util.*

class RaceResourceManager : ResourceManager() {

    private val cards = ArrayList<RaceCard>()

    private val cardNoCache = LinkedHashMap<String, RaceCard>()

    private val cardGroups = HashMap<String, CardGroup>()

    private val goals = HashMap<String, MutableList<Goal>>()

    override fun createResourceResponse(): BgResponse {
        return super.createResourceResponse().apply {
            public("cards", cardList)
            public("goals", goalList)
        }
    }

    /**
     * 按照cardNo取得卡牌
     * @return
     */
    fun getByCardNo(cardNo: String): RaceCard {
        return cardNoCache[cardNo]!!
    }

    /**
     * 按照版本取得对应的牌组
     * @param raceVersion
     * @return
     */
    private fun getCardGroup(raceVersion: String) = this.cardGroups.computeIfAbsent(raceVersion) { CardGroup() }

    /**
     * 取得所有卡牌信息
     * @return
     */

    val cardList: List<RaceCard>
        get() = cards


    override val gameType: GameType
        get() = GameType.RFTG

    /**
     * 取得所有的goal对象
     * @return
     */

    val goalList: List<Goal>
        get() {
            return goals.values.flatten()
        }

    /**
     * 按照配置取得目标牌副本
     * @param config
     * @return
     */

    fun getGoals(config: RaceConfig): List<Goal> = config.versions.flatMap(this::getGoals).map(Goal::clone)

    /**
     * 按照牌组版本取得目标组
     * @param raceVersion
     * @return
     */
    fun getGoals(raceVersion: String) = this.goals.computeIfAbsent(raceVersion) { ArrayList() }


    /**
     * 按照配置取得其他卡牌
     * @param config
     * @return
     */

    fun getOtherCards(config: RaceConfig) = config.versions.map(this::getCardGroup).flatMap(CardGroup::otherCards).map(RaceCard::clone)

    /**
     * 按照配置取得起始卡牌
     * @param config
     * @return
     */

    fun getStartCards(config: RaceConfig) = config.versions.map(this::getCardGroup).flatMap(CardGroup::startCards).map(RaceCard::clone)

    /**
     * 初始化卡牌管理器
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun init() {
        this.loadCards()
        this.loadGoals()
        super.init()
    }

    /**
     * 装载卡牌
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun loadCards() {
        val wb = HSSFWorkbook(BgUtils.getFileInputStream(this, "./game/RFTG.xls"))
        val sheet = wb.getSheetAt(0)
        ExcelUtils.sheetToList(sheet, RaceCard::class.java) { card ->
            repeat(card.qty) {
                val c = card.clone()
                c.id = SequenceUtils.generateId(RFTG::class.java)
                this.put(c)
            }
        }
    }

    /**
     * 装载Goal对象
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun loadGoals() {
        val wb = HSSFWorkbook(BgUtils.getFileInputStream(this, "./game/goal.xls"))
        ExcelUtils.sheetToList(wb.getSheetAt(0), Goal::class.java) {
            it.id = SequenceUtils.generateId(Goal::class.java)
            this.putGoal(it)
        }
    }

    /**
     * 将卡牌加入管理器
     * @param card
     */
    fun put(card: RaceCard) {
        val group = this.getCardGroup(card.gameVersion)
        group.put(card)
        cardNoCache[card.cardNo] = card
        cards.add(card)
    }

    /**
     * 存放goal对象
     * @param goal
     */
    private fun putGoal(goal: Goal) {
        val goals = this.getGoals(goal.gameVersion)
        goals.add(goal)
    }

    /**
     * 牌组
     * @author F14eagle
     */
    internal inner class CardGroup {
        var startCards: MutableList<RaceCard> = ArrayList()
        var otherCards: MutableList<RaceCard> = ArrayList()

        fun put(card: RaceCard) {
            if (card.startWorld >= 0) {
                startCards.add(card)
            } else {
                otherCards.add(card)
            }
        }
    }
}
