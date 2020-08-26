package com.f14.loveletter

import com.f14.F14bg.consts.GameType
import com.f14.F14bg.manager.ResourceManager
import com.f14.bg.utils.BgUtils
import com.f14.utils.ExcelUtils
import com.f14.utils.SequenceUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.util.*

class LLResourceManager : ResourceManager() {

    var cards: MutableList<LLCard> = ArrayList()

    override fun createResourceResponse() = super.createResourceResponse().public("cards", cards)


    override val gameType: GameType
        get() = GameType.LoveLetter

    @Throws(Exception::class)
    override fun init() {
        val wb = HSSFWorkbook(BgUtils.getFileInputStream(this, "./game/LoveLetter.xls"))
        ExcelUtils.sheetToList(wb.getSheetAt(0), LLCard::class.java) { card ->
            //            repeat(card.qty) {
//                val c = card.clone()
//                c.id = SequenceUtils.generateId(LoveLetter::class.java)
//                cards.add(c)
//            }
            (1..card.qty).mapTo(cards) { card.clone().also { it.id = SequenceUtils.generateId(LoveLetter::class.java) } }
        }
        super.init()
    }

}
