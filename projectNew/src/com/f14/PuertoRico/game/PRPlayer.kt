package com.f14.PuertoRico.game

import com.f14.PuertoRico.component.PRDeck
import com.f14.PuertoRico.component.PRTile
import com.f14.PuertoRico.component.PRTileDeck
import com.f14.PuertoRico.component.PrPartPool
import com.f14.PuertoRico.consts.Ability
import com.f14.PuertoRico.consts.Character
import com.f14.PuertoRico.consts.Part
import com.f14.bg.hall.GameRoom
import com.f14.bg.hall.User
import com.f14.bg.player.Player

class PRPlayer(user: User, room: GameRoom) : Player(user, room) {
    var vp: Int = 0
    var doubloon: Int = 0

    var tiles = PRDeck<PRTile>()

    var character: Character? = null
    var colonist: Int = 0

    var resources = PrPartPool()
    var isUsedDoublePriv = false

    /**
     * 玩家添加板块
     * @param tile
     */
    fun addTile(tile: PRTile) {
        this.tiles.addCard(tile)
    }

    /**
     * 判断玩家是否可以使用双倍特权(一个回合中只有在第一次选择角色时才能使用特权)
     * @return
     */
    val canUseDoublePriv: Boolean
        get() = this.hasAbility(Ability.DOUBLE_PRIVILEGE)

    /**
     * 检查玩家是否使用了双倍特权,只有拥有在可以使用双倍特权时,才会设为已使用
     */
    fun checkUsedDoublePriv() {
        if (this.canUseDoublePriv) {
            this.isUsedDoublePriv = true
        }
    }

    /**
     * 取得实际生效的采石场总数
     * @return
     */
    // 有移民在时才有效
    val availableQuarryNum: Int
        get() = quarries.sumBy(PRTile::colonistNum)

    /**
     * 取得所有建筑
     * @return
     */
    val buildings: List<PRTile>
        get() = this.getByPart(Part.BUILDING)

    /**
     * 取得玩家所有建筑的容量
     * @return
     */
    val buildingsSize: Int
        get() = this.buildings.sumBy(PRTile::size)

    /**
     * 按照cardNo取得建筑板块
     * @param cardNo
     * @return
     */
    fun getBuildingTile(cardNo: String) = this.buildings.firstOrNull { it.cardNo == cardNo }

    /**
     * 按照配件类型取得板块
     * @param part
     * @return
     */
    private fun getByPart(part: Part) = tiles.cards.filter { it.part == part }

    /**
     * 取得所有建筑和种植园的未被占用的移民数
     * @return
     */
    val emptyAllColonistNum: Int
        get() = tiles.cards.sumBy(PRTile::emptyNum)

    /**
     * 取得所有建筑的未被占用的移民数
     * @return
     */
    val emptyBuildingColonistNum: Int
        get() = buildings.sumBy(PRTile::emptyNum)

    /**
     * 取得所有郊区地板块,包括种植园,采石场和森林
     * @return
     */
    val fields: List<PRTile>
        get() = plantations + quarries + forests

    /**
     * 取得所有森林
     * @return
     */
    val forests: List<PRTile>
        get() = this.getByPart(Part.FOREST)

    /**
     * 取得所有种植园
     * @return
     */
    val plantations: List<PRTile>
        get() = this.getByPart(Part.PLANTATION)

    /**
     * 取得所有采石场
     * @return
     */
    val quarries: List<PRTile>
        get() = this.getByPart(Part.QUARRY)

    /**
     * 取得所有移民的总数
     * @return
     */
    val totalColonist: Int
        get() = colonist + tiles.cards.sumBy(PRTile::colonistNum)

    /**
     * 判断玩家是否拥有指定的技能并有效
     * @param ability
     * @return
     */
    fun hasAbility(ability: Ability) = this.buildings.any { it.ability == ability && it.colonistNum > 0 }

    /**
     * 判断玩家是否还有未分配移民的建筑或种植园
     * @return
     */
    fun hasEmptyTile(): Boolean = this.tiles.cards.any { it.emptyNum > 0 }

    /**
     * 判断玩家是否拥有parts中的所有配件
     * @param parts
     * @return
     */
    fun noParts(parts: PrPartPool) = !this.resources.hasParts(parts)

    /**
     * 判断玩家是否已经拥有相同cardNo的板块
     * @param cardNo
     * @return
     */
    fun hasTile(cardNo: String) = this.tiles.cards.any { it.cardNo == cardNo }

    /**
     * 重置玩家的游戏信息
     */
    override fun reset() {
        super.reset()
        this.vp = 0
        this.doubloon = 0
        this.tiles = PRTileDeck()
        this.character = null
        this.colonist = 0
        this.resources.clear()
        this.isUsedDoublePriv = false
    }

}
