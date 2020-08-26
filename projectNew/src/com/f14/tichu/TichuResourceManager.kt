package com.f14.tichu

import com.f14.F14bg.consts.GameType
import com.f14.F14bg.manager.ResourceManager
import com.f14.bg.action.BgResponse
import com.f14.bg.utils.BgUtils
import com.f14.tichu.componet.TichuCard
import com.f14.utils.ExcelUtils
import com.f14.utils.SequenceUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.util.*

class TichuResourceManager : ResourceManager() {
    private val cards: MutableList<TichuCard> = ArrayList()

    override fun createResourceResponse(): BgResponse {
        return super.createResourceResponse()
                .public("cards", this.cards)
    }

    /**
     * 取得所有卡牌的副本
     * @return
     */
    val allCardsInstance: Collection<TichuCard>
        get() = BgUtils.cloneList(this.cards)

    override val gameType: GameType
        get() = GameType.Tichu

    @Throws(Exception::class)
    override fun init() {
        val wb = HSSFWorkbook(BgUtils.getFileInputStream(this, "./game/Tichu.xls"))
        ExcelUtils.sheetToList(wb.getSheetAt(0), TichuCard::class.java) {
            it.id = SequenceUtils.generateId(Tichu::class.java)
            cards.add(it)
        }
        super.init()
    }
}
