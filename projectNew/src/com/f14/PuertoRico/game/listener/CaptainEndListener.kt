package com.f14.PuertoRico.game.listener

import com.f14.PuertoRico.component.PrPartPool
import com.f14.PuertoRico.consts.Ability
import com.f14.PuertoRico.consts.GameCmdConst
import com.f14.PuertoRico.consts.GoodType
import com.f14.PuertoRico.game.PRGameMode
import com.f14.PuertoRico.game.PRPlayer
import com.f14.PuertoRico.utils.PrUtils
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.utils.StringUtils

class CaptainEndListener(gameMode: PRGameMode) : PROrderActionListener(gameMode) {

    /**
     * 为玩家自动保存货物
     * @param player
     * @return 返回是否自动保存成功
     */
    private fun autoSaveGood(player: PRPlayer): Boolean {
        val saveTypeNum = this.getSaveGoodTypeNum(player)

        val goodNums = player.resources.allPartsNumber
                .filterValues { it > 0 }
                .map { GoodObject(it.key as GoodType, it.value) }
//        val goodNums = ArrayList<GoodObject>()
//        for (part in player.resources.parts) {
//            val num = player.resources.getAvailableNum(part)
//            // 只保存数量大于0的货物
//            if (num > 0) {
//                val o = GoodObject(part as GoodType, num)
//                goodNums.add(o)
//            }
//        }

        // 如果可以保存货物的种类大于拥有的货物种类,则保存所有的货物
        if (saveTypeNum >= goodNums.size) {
            gameMode.report.saveResources(player)
            return true
        }

        val saveSingleNum = this.getSaveGoodNum(player)
        if (goodNums.size == 1) {
            // 如果玩家只有1种货物,则保存该货物
            // 取得需要丢弃的数量
            val goodType = goodNums.single().goodType
            val num = player.resources.getAvailableNum(goodType) - saveSingleNum
            if (num <= 0) {
                // 如果不需要弃货,则自动保存
                gameMode.report.saveResources(player)
                return true
            }
            player.resources.setPart(goodType, saveSingleNum)
            gameMode.partPool.putPart(goodType, num)
            val parts = PrPartPool()
            parts.setPart(goodType, num)
            gameMode.game.sendPlayerGetPartResponse(player, parts, -1)
            gameMode.game.sendSupplyGetPartResponse(parts, 1)
            gameMode.report.saveResources(player)
            return true
        } else {
            val restNums = goodNums
                    .sortedDescending() // 顺序排列货物数量
                    .drop(saveTypeNum) // 将按种类保留最多数量的货物
//            goodNums.sort()
//            for (i in 0 until saveTypeNum) {
//                goodNums.removeAt(goodNums.size - 1)
//            }
            val restNum = restNums.sumBy(GoodObject::num)
            // 如果剩余货物的数量小于等于玩家允许保存的货物数量,则自动保存
            if (restNum <= saveSingleNum) {
                gameMode.report.saveResources(player)
                return true
            }
        }
        return false
    }

    override fun beforeListeningCheck(player: PRPlayer): Boolean {
        // 如果玩家没有货,则跳过
        return !player.resources.isEmpty && !this.autoSaveGood(player)
    }

    /**
     * 转换成GoodType数组
     * @param goods
     * @return
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    private fun convertToGoodTypes(goods: String): Array<GoodType> {
        if (goods.isEmpty()) {
            return emptyArray()
        }
        return StringUtils.string2List(goods).map(PrUtils::getGoodType).toTypedArray()
    }

    /**
     * 玩家进行弃货行动
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun discardGood(action: GameAction) {
        val player = action.getPlayer<PRPlayer>()
        // 保存单个货物
        val resourceString: String = action.getAsString("resourceString").takeUnless(String::isEmpty) ?: "{}"
        val resources = PrUtils.getPartInfo(resourceString)
        if (player.noParts(resources)) {
            throw BoardGameException("你选择的货物数量错误,请重新选择!")
        }
        val saveSingleNum = this.getSaveGoodNum(player)
        if (resources.totalNum > saveSingleNum) {
            throw BoardGameException("你最多只能保存 $saveSingleNum 个货物!")
        }

        // 保存某类货物
        val goods = action.getAsString("goodTypeGroup")
        val goodTypes = this.convertToGoodTypes(goods)
        val saveNum = this.getSaveGoodTypeNum(player)
        if (goodTypes.isNotEmpty() && saveNum == 0) {
            throw BoardGameException("你不能按类型保存货物!")
        }
        if (goodTypes.size > saveNum) {
            throw BoardGameException("你最多只能保存 $saveNum 种货物!")
        }

        // 先按种类保存货物
        player.resources.parts.filterIsInstance<GoodType>().filterNot { it in goodTypes }.forEach {
            val num = player.resources.takePartAll(it)
            gameMode.partPool.putPart(it, num)
        }

        // 再保存单个货物
        resources.parts.filter { player.resources.getAvailableNum(it) < 0 }.forEach {
            player.resources.setPart(it, resources.getAvailableNum(it))
            gameMode.partPool.takePart(it, resources.getAvailableNum(it))
        }

        gameMode.report.saveResources(player)
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val player = action.getPlayer<PRPlayer>()
        // 执行弃货行动
        this.discardGood(action)
        // 刷新玩家和公共资源的配件信息
        gameMode.game.refreshPlayerResource(player)
        gameMode.game.sendPartsInfo()
        // 设置玩家已回应
        this.setPlayerResponsed(player)
    }

    /**
     * 取得玩家可以保存单个货物的数量
     * @param player
     * @return
     */
    private fun getSaveGoodNum(player: PRPlayer): Int {
        var res = 1
        if (player.hasAbility(Ability.SAVE_SINGLE_3)) {
            res += 3
        }
        return res
    }

    /**
     * 取得玩家可以保存货物种类的数量
     * @param player
     * @return
     */
    private fun getSaveGoodTypeNum(player: PRPlayer): Int {
        var res = 0
        if (player.hasAbility(Ability.SAVE_1)) {
            res += 1
        }
        if (player.hasAbility(Ability.SAVE_2)) {
            res += 2
        }
        return res
    }

    override val validCode: Int
        get() = GameCmdConst.GAME_CODE_CAPTAIN_END

    override fun initListeningPlayers() {
        // 所有拥有货物的玩家需要执行弃货阶段
        gameMode.game.players.forEach { this.setNeedPlayerResponse(it.position, !it.resources.isEmpty) }
    }

    private inner class GoodObject(val goodType: GoodType, val num: Int = 0) : Comparable<GoodObject> {
        override fun compareTo(other: GoodObject) = this.num.compareTo(other.num)
    }
}
