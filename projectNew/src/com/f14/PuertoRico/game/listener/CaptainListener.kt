package com.f14.PuertoRico.game.listener

import com.f14.F14bg.network.CmdFactory
import com.f14.PuertoRico.component.PrPartPool
import com.f14.PuertoRico.component.Ship
import com.f14.PuertoRico.consts.Ability
import com.f14.PuertoRico.consts.Character
import com.f14.PuertoRico.consts.GameCmdConst
import com.f14.PuertoRico.consts.GoodType
import com.f14.PuertoRico.game.PRGameMode
import com.f14.PuertoRico.game.PRPlayer
import com.f14.PuertoRico.utils.PrUtils
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException

class CaptainListener(gameMode: PRGameMode) : PROrderActionListener(gameMode) {

    /**
     * 执行自动装船
     * @param player
     * @return
     */
    private fun autoShip(player: PRPlayer): Boolean {
        // 如果玩家拥有私船,就不能进行自动装货
        // 执行自动装货
        when {
            this.canUseSelfShip(player) -> return false
        // 玩家拥有小码头,也不能进行自动装货
            this.canUseSmallShip(player) -> return false
        // 首先整理出所有货物可以装运的船只
        // 如果所有货物中只剩1种货物可以装船,并且只能装1只船,则自动装货
            else -> {
                val map = player.resources.parts
                        .filterIsInstance<GoodType>()
                        .filter { player.resources.getAvailableNum(it) > 0 }
                        .map { it to gameMode.shipPort.getAvailableShips(it) }
                        .toMap()
                        .filterValues { it.isNotEmpty() }
                // 如果所有货物中只剩1种货物可以装船,并且只能装1只船,则自动装货
                if (map.size == 1) {
                    map.filterValues { it.size == 1 }.forEach { (goodType, ships) ->
                        // 执行自动装货
                        try {
                            this.doShip(player, goodType, ships[0])
                            return true
                        } catch (e: BoardGameException) {
                            log.error(e, e)
                        }

                    }
                }
                return false
            }
        }
    }

    override fun beforeListeningCheck(player: PRPlayer): Boolean {
        return when {
        // 如果玩家没有货物,则不需要回应
            player.resources.isEmpty -> false
        // 如果玩家拥有私船,则总是需要回应
            this.canUseSelfShip(player) -> true
        // 如果玩家拥有小码头,则总是需要回应
            this.canUseSmallShip(player) -> true
        // 执行自动装船,如果成功则不需要回应
            this.autoShip(player) -> false
        // 判断玩家是否可以跳过,如果可以跳过则不需要回应
            else -> this.cannotPass(player)
        }
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        // 设置玩家在船长阶段的一些能力参数
        for (player in gameMode.game.players) {
            // 是否可以使用私船
            this.setCanUseSelfShip(player, player.hasAbility(Ability.SELF_SHIP))
            // 是否可以在运货时得到金钱
            this.setShippedDoubloon(player, player.hasAbility(Ability.DOUBLOON_SHIP))
            // 是否可以使用小私船
            this.setCanUseSmallShip(player, player.hasAbility(Ability.VP_SHIP_HALF))
        }
        // 如果玩家拥有按照货物数量得VP的能力,则在此计算
        for (player in gameMode.game.players) {
            if (player.hasAbility(Ability.VP_BEFORE_SHIP)) {
                val vp = player.resources.parts.filterIsInstance<GoodType>().map(player.resources::getAvailableNum).sumBy { it / 2 }
                if (vp > 0) {
                    gameMode.game.getVP(player, vp)
                    gameMode.report.getVP(player, vp)
                }
            }
        }
    }

    /**
     * 判断玩家是否可以跳过
     * @param player
     */
    private fun cannotPass(player: PRPlayer): Boolean {
        return when {
            player.resources.isEmpty -> false
        // 判断玩家所有资源中是否有可以装船的
        // 如果该配件是资源,并且数量大于0,则需判断是否可以装货
            else -> player.resources.parts.filterIsInstance<GoodType>().any { player.resources.getAvailableNum(it) > 0 && gameMode.shipPort.canShip(it) }
        }
    }

    /**
     * 判断玩家在该回合中运货时是否可以得到金钱
     * @param player
     * @return
     */
    private fun canShippedDoubloon(player: PRPlayer) = this.getPlayerParamSet(player.position).getBoolean("DOUBLOON_SHIP")

    /**
     * 判断玩家在该回合中是否可以使用私船装货
     * @param player
     * @return
     */
    private fun canUseSelfShip(player: PRPlayer) = this.getPlayerParamSet(player.position).getBoolean("SELF_SHIP")

    /**
     * 判断玩家在该回合中是否可以使用小私船装货
     * @param player
     * @return
     */
    private fun canUseSmallShip(player: PRPlayer) = this.getPlayerParamSet(player.position).getBoolean("SMALL_SHIP")

    override fun createStartListenCommand(player: PRPlayer) = super.createStartListenCommand(player)
            // 设置玩家是否可以使用私船和小码头的参数
            .public("selfShip", this.canUseSelfShip(player))
            .public("smallShip", this.canUseSmallShip(player))

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val player = action.getPlayer<PRPlayer>()
        when (action.getAsString("subact")) {
            "ship" -> {
                // 运货上船
                this.ship(action)
                // 检查玩家是否使用了双倍特权
                player.checkUsedDoublePriv()
                // 设置玩家暂时完成行动
                this.setPlayerResponsedTemp(player)
                // //下一位玩家行动
                // this.sendNextListenerCommand(mode);
            }
            "pass" -> {
                // 只有在不能进行装船时,才能跳过
                this.cannotPass(player) && throw BoardGameException("你还有货物可以装船,不能结束!")
                gameMode.report.doPass(player)
                this.setPlayerResponsed(player)
            }
            else -> throw BoardGameException("无效的行动代码!")
        }
    }

    /**
     * 执行装船并发送相应的信息到客户端
     * @param player
     * @param goodType
     * @param ship
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doShip(player: PRPlayer, goodType: GoodType, ship: Ship) {
        val num = ship.doShip(player, goodType)
        num <= 0 && throw BoardGameException("装货失败!")
        val result = this.getShippedResult(player, num, false)

        // 发送装货的消息到客户端
        val goods = PrPartPool().apply { putPart(goodType, num) }
        gameMode.game.sendPlayerGetPartResponse(player, goods, -1)
        // 发送得到VP的消息到客户端
        gameMode.game.getVP(player, result.vp)
        // 发送货船得到货物的消息
        CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_GET_SHIP, -1)
                .public("shipSize", ship.maxSize)
                .public("goodNum", num)
                .public("goodType", goodType)
                .send(gameMode.game)
        // 如果运货得到了金钱,则发送消息到客户端
        if (result.doubloon > 0) gameMode.game.getDoubloon(player, result.doubloon)
        gameMode.report.doShip(player, goods, result.vp, result.doubloon)
    }

    /**
     * 取得玩家运货后得到的VP和金钱
     * @param player
     * @param num
     * @param halfVp
     * @return
     */
    private fun getShippedResult(player: PRPlayer, num: Int, halfVp: Boolean): ShippedResult {
        val result = ShippedResult()
        result.vp = if (halfVp) num / 2 else num
        // 如果玩家是船长,则在第一次装货时,可以得到1点额外的VP
        if (player.character == Character.CAPTAIN && !this.hasShipped(player)) {
            this.setShipped(player, true)
            result.vp += 1
            // 如果拥有双倍特权则再得到1点VP
            if (player.canUseDoublePriv) result.vp += 1
        }
        // 如果拥有船坞,还能再得到1点额外的VP
        if (player.hasAbility(Ability.VP_SHIP)) result.vp += 1
        // 如果拥有运货得到金钱的能力,则还能得到1个金钱
        if (this.canShippedDoubloon(player)) result.doubloon += 1
        return result
    }

    override val validCode: Int
        get() = GameCmdConst.GAME_CODE_CAPTAIN

    /**
     * 判断玩家在该回合中是否已经装过货
     * @param player
     * @return
     */
    private fun hasShipped(player: PRPlayer) = this.getPlayerParamSet(player.position).getBoolean("SHIPPED")

    override fun initListeningPlayers() = // 如果某个玩家没有货物,则跳过
            gameMode.game.players.forEach { this.setNeedPlayerResponse(it.position, !it.resources.isEmpty) }

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        // 结束时需要检查,如果有满载的货船,则清空
        gameMode.shipPort.ships.values.filter(Ship::isFull).forEach { ship ->
            val goodType = ship.goodType!!
            val num = ship.clear()
            val parts = PrPartPool()
            parts.putPart(goodType, num)
            gameMode.partPool.putPart(goodType, num)
            gameMode.game.sendSupplyGetPartResponse(parts, 1)
            gameMode.report.clearShip(ship.maxSize)
        }
        // 将货船的信息发送到客户端
        gameMode.game.sendShipsInfo()
    }

    /**
     * 设置玩家是否可以使用私船
     * @param player
     */
    private fun setCanUseSelfShip(player: PRPlayer, can: Boolean) {
        this.getPlayerParamSet(player.position)["SELF_SHIP"] = can
    }

    /**
     * 设置玩家是否可以使用小私船
     * @param player
     */
    private fun setCanUseSmallShip(player: PRPlayer, can: Boolean) {
        this.getPlayerParamSet(player.position)["SMALL_SHIP"] = can
    }

    /**
     * 设置玩家是否已经装过货
     * @param player
     */
    private fun setShipped(player: PRPlayer, shipped: Boolean) {
        this.getPlayerParamSet(player.position)["SHIPPED"] = shipped
    }

    /**
     * 设置玩家在运货时是否可以得到金钱
     * @param player
     * @param can
     */
    private fun setShippedDoubloon(player: PRPlayer, can: Boolean) {
        this.getPlayerParamSet(player.position)["DOUBLOON_SHIP"] = can
    }

    /**
     * 玩家进行装货行动
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun ship(action: GameAction) {
        val player = action.getPlayer<PRPlayer>()
        val shipSize = action.getAsInt("shipSize")

        if (shipSize == -1) {
            this.canUseSmallShip(player) || throw BoardGameException("你不能使用小私船运货!")
            // 选择的是小私船
            val resourceString = action.getAsString("resourceString")
            val resources = PrUtils.getPartInfo(resourceString)
            resources.totalNum <= 0 && throw BoardGameException("你没有选择任何货物!")
            player.noParts(resources) && throw BoardGameException("你选择的货物数量有错,请重新选择!")
            val goodNum = resources.totalNum
            // 小私船运货只能得到一半的VP
            val result = this.getShippedResult(player, goodNum, true)
            // 丢弃的货物将直接返回公共资源堆
            player.resources.takeParts(resources)
            gameMode.partPool.putParts(resources)

            // 用过小私船后在该回合中就不能再次使用
            this.setCanUseSmallShip(player, false)

            // 发送装货的消息到客户端
            gameMode.game.sendPlayerGetPartResponse(player, resources, -1)
            gameMode.game.sendSupplyGetPartResponse(resources, 1)
            // 发送得到VP的消息到客户端
            gameMode.game.getVP(player, result.vp)
            // 如果运货得到了金钱,则发送消息到客户端
            if (result.doubloon > 0) gameMode.game.getDoubloon(player, result.doubloon)
            gameMode.report.useAbility(player, Ability.VP_SHIP_HALF)
            gameMode.report.doShip(player, resources, result.vp, result.doubloon)
        } else {
            val good = action.getAsString("goodType")
            val goodType = PrUtils.getGoodType(good)
            player.resources.getAvailableNum(goodType) <= 0 && throw BoardGameException("你没有该种货物!")
            if (shipSize == 0) {
                // 选择的是私船
                this.canUseSelfShip(player) || throw BoardGameException("你不能使用私船装货!")
                val num = player.resources.takePartAll(goodType)
                // 丢弃的货物将直接返回公共资源堆
                gameMode.partPool.putPart(goodType, num)
                val result = this.getShippedResult(player, num, false)
                // 用过私船后在该回合中就不能再次使用
                this.setCanUseSelfShip(player, false)

                // 发送装货的消息到客户端
                val goods = PrPartPool()
                goods.putPart(goodType, num)
                gameMode.game.sendPlayerGetPartResponse(player, goods, -1)
                gameMode.game.sendSupplyGetPartResponse(goods, 1)
                // 发送得到VP的消息到客户端
                gameMode.game.getVP(player, result.vp)
                // 如果运货得到了金钱,则发送消息到客户端
                if (result.doubloon > 0) gameMode.game.getDoubloon(player, result.doubloon)
                gameMode.report.useAbility(player, Ability.SELF_SHIP)
                gameMode.report.doShip(player, goods, result.vp, result.doubloon)
            } else {
                // 选择货船装货
                val ship = gameMode.shipPort[shipSize]
                gameMode.shipPort.canShip(goodType, ship) || throw BoardGameException("你不能这样装货!")
                this.doShip(player, goodType, ship)
            }
        }
    }

    /**
     * 运货后得到的结果
     * @author F14eagle
     */
    data class ShippedResult(var vp: Int = 0, var doubloon: Int = 0)
}
