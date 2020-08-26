package com.f14.PuertoRico.manager

import com.f14.F14bg.consts.GameType
import com.f14.F14bg.manager.ResourceManager
import com.f14.PuertoRico.component.CharacterCard
import com.f14.PuertoRico.component.PRTile
import com.f14.PuertoRico.consts.Part
import com.f14.PuertoRico.game.PrConfig
import com.f14.PuertoRico.game.PuertoRico
import com.f14.bg.action.BgResponse
import com.f14.bg.utils.BgUtils
import com.f14.utils.ExcelUtils
import com.f14.utils.SequenceUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.util.*

class PRResourceManager : ResourceManager() {

    private val characterCards = ArrayList<CharacterCard>()

    private val groups = HashMap<String, CardGroup>()

    /**
     * 添加板块
     * @param tile
     */
    private fun addPRTile(tile: PRTile) {
        this.getTiles(tile.gameVersion, tile.part).add(tile)
    }

    override fun createResourceResponse(): BgResponse {
        val res = super.createResourceResponse()
        res.public("characterCards", this.getCharacterCards())
        res.public("plantations", this.getAllTiles(Part.PLANTATION))
        res.public("quarries", this.getAllTiles(Part.QUARRY))
        res.public("buildings", this.getAllTiles(Part.BUILDING))
        res.public("forest", this.getAllTiles(Part.FOREST))
        return res
    }

    /**
     * 取得指定配件类型的所有板块列表
     * @param part
     * @return
     */
    private fun getAllTiles(part: Part): List<PRTile> {
        return this.groups.values.mapNotNull { it.tiles[part] }.flatMap { BgUtils.cloneList(it) }
    }

    /**
     * 取得建筑实例
     * @param config
     * @return
     */
    fun getBuildings(config: PrConfig): List<PRTile> {
        return config.versions.map { this.getTiles(it, Part.BUILDING) }.flatMap { BgUtils.cloneList(it) }
    }

    /**
     * 取得角色卡实例
     * @return
     */
    fun getCharacterCards(): MutableList<CharacterCard> {
        return BgUtils.cloneList(this.characterCards) as MutableList<CharacterCard>
    }

    /**
     * 取得森林实例
     * @param config
     * @return
     */
    fun getForest(config: PrConfig): List<PRTile> {
        return config.versions.map { this.getTiles(it, Part.FOREST) }.flatMap { BgUtils.cloneList(it) }
    }


    override val gameType: GameType
        get() = GameType.PuertoRico

    /**
     * 取得版本对应的牌组
     * @param version
     * @return
     */
    private fun getGroup(version: String): CardGroup {
        return this.groups.computeIfAbsent(version) { CardGroup() }
    }

    /**
     * 取得种植园实例
     * @param config
     * @return
     */
    fun getPlantations(config: PrConfig): List<PRTile> {
        return config.versions.map { this.getTiles(it, Part.PLANTATION) }.flatMap { BgUtils.cloneList(it) }
    }

    /**
     * 取得采石场实例
     * @param config
     * @return
     */
    fun getQuarries(config: PrConfig): List<PRTile> {
        return config.versions.map { this.getTiles(it, Part.QUARRY) }.flatMap { BgUtils.cloneList(it) }
    }

    /**
     * 取得指定配件类型的板块列表
     * @param part
     * @return
     */
    private fun getTiles(version: String, part: Part): MutableList<PRTile> {
        return this.getGroup(version).tiles.computeIfAbsent(part) { ArrayList() }
    }

    /**
     * 初始化
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun init() {
        val wb = HSSFWorkbook(BgUtils.getFileInputStream(this, "./game/PuertoRico.xls"))
        // 第一个sheet是角色牌的信息
        ExcelUtils.sheetToList(wb.getSheetAt(0), CharacterCard::class.java) { card ->
            repeat(card.qty) {
                val c = card.clone()
                c.id = SequenceUtils.generateId(PuertoRico::class.java)
                characterCards.add(c)
            }
        }
        // 第二个sheet是建筑,种植园等的信息
        ExcelUtils.sheetToList(wb.getSheetAt(1), PRTile::class.java) { card ->
            repeat(card.qty) {
                val c = card.clone()
                c.id = SequenceUtils.generateId(PuertoRico::class.java)
                this.addPRTile(c)
            }
        }
        super.init()
    }

    /**
     * 按游戏版本分组的容器对象
     * @author F14eagle
     */
    internal inner class CardGroup {
        var tiles: MutableMap<Part, MutableList<PRTile>> = HashMap()
    }
}
