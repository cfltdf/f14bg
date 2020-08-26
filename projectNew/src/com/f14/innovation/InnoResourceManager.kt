package com.f14.innovation

import com.f14.F14bg.consts.GameType
import com.f14.F14bg.manager.ResourceManager
import com.f14.bg.action.BgResponse
import com.f14.bg.common.ListMap
import com.f14.bg.utils.BgUtils
import com.f14.innovation.component.InnoCard
import com.f14.utils.ExcelUtils
import com.f14.utils.SequenceUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.util.*

class InnoResourceManager : ResourceManager() {

    private var cards = ListMap<String, InnoCard>()

    private var achieveCards = ListMap<String, InnoCard>()

    /**
     * 添加成就牌

     * @param c
     */
    private fun addAchieveCard(c: InnoCard) {
        achieveCards.getList(c.gameVersion).add(c)
    }

    /**
     * 添加卡牌

     * @param c
     */
    private fun addCard(c: InnoCard) {
        cards.getList(c.gameVersion).add(c)
    }

    override fun createResourceResponse(): BgResponse {
        return super.createResourceResponse().public("cards", this.allCards)
    }

    /**
     * 按照设置取得所有成就牌的实例

     * @param config

     * @return
     */

    fun getAchieveCardsInstanceByConfig(config: InnoConfig): Collection<InnoCard> {
        val res = ArrayList<InnoCard>()
        for (version in config.versions) {
            res.addAll(BgUtils.cloneList(this.achieveCards.getList(version)))
        }
        return res
    }

    /**
     * 取得所有卡牌

     * @return
     */

    private val allCards: Collection<InnoCard>
        get() {
            val res = LinkedHashSet<InnoCard>()
            for (key in this.cards.keySet()) {
                res.addAll(this.cards.getList(key))
            }
            for (key in this.achieveCards.keySet()) {
                res.addAll(this.achieveCards.getList(key))
            }
            return res
        }

    /**
     * 按照设置取得所有卡牌的实例

     * @param config

     * @return
     */

    fun getCardsInstanceByConfig(config: InnoConfig): Collection<InnoCard> {
        val res = ArrayList<InnoCard>()
        for (version in config.versions) {
            res.addAll(BgUtils.cloneList(this.cards.getList(version)))
        }
        return res
    }


    override val gameType: GameType
        get() = GameType.Innovation

    @Throws(Exception::class)
    override fun init() {
        val wb = HSSFWorkbook(BgUtils.getFileInputStream(this, "./game/Innovation.xls"))
        // 第一个sheet是卡牌的信息
        ExcelUtils.sheetToList(wb.getSheetAt(0), InnoCard::class.java) {
            it.id = SequenceUtils.generateId(Innovation::class.java)
            this.addCard(it)
        }
        // 第二个sheet是成就牌的信息
        ExcelUtils.sheetToList(wb.getSheetAt(1), InnoCard::class.java) {
            it.id = SequenceUtils.generateId(Innovation::class.java)
            this.addAchieveCard(it)
        }
        super.init()
    }

}
