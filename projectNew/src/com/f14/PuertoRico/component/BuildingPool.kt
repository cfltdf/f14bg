package com.f14.PuertoRico.component

import com.f14.PuertoRico.consts.BuildingType
import com.f14.bg.component.Card
import com.f14.bg.component.CardPool
import com.f14.bg.exception.BoardGameException
import com.f14.utils.CollectionUtils
import java.util.*

class BuildingPool : CardPool() {
    /**
     * 冲突的建筑对
     */
    private var conflictBuildings: MutableList<Array<String>> = ArrayList()
    /**
     * 所有使用建筑的cardNo,PRTile仅用来记录该建筑的信息用
     */
    private var usedBuildings: MutableMap<String, PRTile> = HashMap()
    /**
     * 经过排序的所有使用建筑的cardNo
     */
    private var sortedCardNos: MutableList<String> = ArrayList()
    private lateinit var buildings: BuildingContainer

    init {
        this.initConflictBuildings()
    }

    override fun addCard(card: Card) {
        val tile = card as PRTile
        super.addCard(tile)
        this.usedBuildings.putIfAbsent(tile.cardNo, tile)
    }

    /**
     * 选择建筑,如果不能选择则抛出异常
     * @param cardNo
     * @param userName
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun chooseBuilding(cardNo: String, userName: String) = this.buildings.chooseBuilding(cardNo, userName)

    /**
     * 按等级分组取得所有的建筑cardNo
     * @return
     */
    val allBuildings: Map<Int, Collection<String>>
        get() = this.buildings.cardNos

    /**
     * 设置游戏中所有的建筑
     * @param allBuildings
     */
    fun setAllBuildings(allBuildings: List<PRTile>) {
        this.buildings = BuildingContainer(allBuildings)
    }

    /**
     * 按价格分组取得所有已经选中的建筑
     * @return
     */
    val selectedBuildings: Map<Int, Map<String, String>>
        get() = this.buildings.selectedBuildings

    /**
     * 初始化建筑池,将玩家选择的建筑加入到建筑池中
     */
    fun initBuildingPool() {
        this.buildings.addToBuildingPool()
        this.sort()
    }

    /**
     * 初始化冲突的建筑
     */
    private fun initConflictBuildings() {
        // 庄园和森林小屋冲突
        conflictBuildings.add(arrayOf(HACIENDA, FOREST_HUT))
    }

    /**
     * 判断cardNos中是否存在冲突的建筑
     * @param cardNos
     * @return
     */
    fun isConflict(cardNos: Collection<String>)
    // 检查cardNos存在的冲突建筑数量
    // 如果数量大于1,则存在冲突
            = this.conflictBuildings.any { it.count(cardNos::contains) > 1 }

    /**
     * 判断cardNo是否和cardNos冲突
     * @param cardNo
     * @param cardNos
     * @return
     */
    fun isConflict(cardNo: String, cardNos: Collection<String>) = this.isConflict(setOf(cardNo) + cardNos)

    /**
     * 判断是否已经选择完所有的建筑
     */
    val isSelectedBuildingFull: Boolean
        get() = this.buildings.isSelectedBuildingFull

    /**
     * 随机选择建筑物
     */
    fun randomChooseBuildings() {
        this.buildings.randomChooseBuildings()
    }

    /**
     * 将所有使用的建筑排序
     */
    fun sort() {
        this.sortedCardNos.clear()
        this.usedBuildings.values.sorted().mapTo(sortedCardNos, PRTile::cardNo)
    }


    override fun toMap(): Map<String, Any> {
        val list = this.sortedCardNos.map { mapOf(it to this.getDeckSize(it)) }
        return mapOf("buildings" to list)
    }

    /**
     * 建筑物容器
     * @author F14eagle
     */
    private inner class BuildingContainer(allBuildings: List<PRTile>) {
        /**
         * 按等级分组的所有cardNo
         */
        var cardNos: MutableMap<Int, MutableSet<String>> = HashMap()
        /**
         * cardNo对应的所有版块实例
         */
        var tiles: MutableMap<String, MutableList<PRTile>> = HashMap()
        /**
         * 已选择的建筑,Integer为建筑的费用,Map中的元素为cardNo,所选用户名称
         */
        var selectedBuildings: MutableMap<Int, MutableMap<String, String>> = HashMap()

        init {
            // this.allBuildings.plusAssign(allBuildings);
            // 将所有的建筑的cardNo按等级分组
            for (tile in allBuildings) {
                when (tile.buildingType) {
                    BuildingType.LARGE_FACTORY, BuildingType.SMALL_FACTORY -> // 如果是工厂,则直接添加到牌组
                        this@BuildingPool.addCard(tile)
                    else -> {
                        // 取得建筑等级对应的集合,如果不存在则创建一个新集合
                        // 如果列表中不存在该cardNo则添加到列表中
                        cardNos.computeIfAbsent(tile.level) { HashSet() }.add(tile.cardNo)
                        // 添加cardNo对应的tile实例
                        tiles.computeIfAbsent(tile.cardNo) { ArrayList() }.add(tile)
                    }
                }
            }
        }

        /**
         * 将选择的建筑加入到建筑池中
         */
        fun addToBuildingPool() = // 将选择出来的建筑放入牌组
                this.selectedBuildings.values.flatMap(MutableMap<String, String>::keys).mapNotNull(tiles::get).forEach { this@BuildingPool.addCards(it) }

        /**
         * 选择建筑,如果不能选择则抛出异常
         * @param cardNo
         * @param userName
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun chooseBuilding(cardNo: String, userName: String): PRTile {
            val tile = this.getCard(cardNo)
            val costBuildings = this.getSelectedBuildingsByCost(tile.cost)
            cardNo in costBuildings && throw BoardGameException("该建筑已经被选择过了!")
            costBuildings.size >= this.getBuildingNumber(tile.cost) && throw BoardGameException("该费用的建筑已经满了,不能选择该建筑!")
            // 另外需要检查冲突的建筑
            isConflict(cardNo, costBuildings.keys) && throw BoardGameException("存在冲突的建筑,不能选择该建筑")
            // 添加到已选建筑中
            costBuildings[cardNo] = userName
            return tile
        }

        /**
         * 取得指定价格所用的建筑数量(不包括工厂)
         * @param cost
         * @return
         */
        fun getBuildingNumber(cost: Int) = when (cost) {
            1, 3, 4, 6, 7, 9 -> 1
            2, 5, 8 -> 2
            10 -> 5
            else -> 0
        }

        /**
         * 取得cardNo对应的建筑
         * @param cardNo
         * @return
         */
        private fun getCard(cardNo: String) = this.tiles[cardNo]!![0]

        /**
         * 取得指定费用的所有已选建筑Map
         * @return
         */
        private fun getSelectedBuildingsByCost(cost: Int) = this.selectedBuildings.computeIfAbsent(cost) { HashMap() }

        /**
         * 判断是否已经选择完所有的建筑
         */
        // 总共有10种费用的建筑
        val isSelectedBuildingFull: Boolean
            get() = (1..10).none { this.getSelectedBuildingsByCost(it).size < this.getBuildingNumber(it) }

        /**
         * 随机选择建筑物
         */
        fun randomChooseBuildings() {
            // 随机抽取将用到的建筑物
            for (level in cardNos.keys) {
                val nos = cardNos[level]
                val list = ArrayList(nos)
                // 打乱建筑顺序
                CollectionUtils.shuffle(list)
                for (cardNo in list) {
                    try {
                        // 遍历选择建筑
                        this.chooseBuilding(cardNo, "system")
                    } catch (e: Exception) {
                        // 如果选择失败则尝试下一个建筑
                    }

                }
            }
            // 选择完成后初始化建筑池
            this@BuildingPool.initBuildingPool()
        }
    }

    companion object {
        /**
         * 大型染料厂
         */
        const val INDIGO_FACTORY = "13.0"
        /**
         * 大型糖厂
         */
        const val SUGAR_FACTORY = "14.0"
        /**
         * 庄园
         */
        const val HACIENDA = "10.0"
        /**
         * 森林小屋
         */
        const val FOREST_HUT = "31.0"
    }

}
