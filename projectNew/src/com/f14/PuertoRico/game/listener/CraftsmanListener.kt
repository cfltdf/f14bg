package com.f14.PuertoRico.game.listener

import com.f14.PuertoRico.component.BuildingPool
import com.f14.PuertoRico.component.PrPartPool
import com.f14.PuertoRico.consts.Ability
import com.f14.PuertoRico.consts.Character
import com.f14.PuertoRico.consts.GameCmdConst
import com.f14.PuertoRico.consts.GoodType
import com.f14.PuertoRico.game.PRGameMode
import com.f14.PuertoRico.game.PRPlayer
import com.f14.PuertoRico.utils.PrUtils
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException

class CraftsmanListener(gameMode: PRGameMode) : PROrderActionListener(gameMode) {

    /**
     * 玩家生产货物后的行动
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun afterProduced(player: PRPlayer) {
        val goods = this.getProducedGoods(player.position)
        if (player.hasAbility(Ability.PRODUCE_DOUBLOON)) {
            // 如果拥有生产得到金钱的能力,则处理以下代码
            val doubloon = when (goods.partNum) {
                2 -> 1
                3 -> 2
                4 -> 3
                5 -> 5
                else -> 0
            }
            if (doubloon > 0) {
                gameMode.game.getDoubloon(player, doubloon)
                gameMode.report.getDoubloon(player, doubloon)
            }
        }
        if (player.hasAbility(Ability.PRODUCE_SPECIAL)) {
            // 如果按照生产货物数量得到金钱的能力,则处理以下代码
            val maxProducedNum = goods.parts
                    // 不取玉米的数量
                    .filter { it !== GoodType.CORN }
                    .map(goods::getAvailableNum)
                    // 取得生产货物数量最大值
                    .max() ?: 0
            // 得到生产数量最大值-1的金钱
            val doubloon = maxProducedNum - 1
            if (doubloon > 0) {
                gameMode.game.getDoubloon(player, doubloon)
                gameMode.report.getDoubloon(player, doubloon)
            }
        }
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        // 阶段开始前,为所有玩家生产货物
        val players = gameMode.game.playersByOrder
        players.forEach(this::produceGood)
    }

    /**
     * 判断玩家是否可以生产指定的货物
     * @param player
     * @param goodType
     * @return
     */
    private fun canProduce(player: PRPlayer, goodType: GoodType): Boolean {
        // 如果玩家生产过指定的货物,则可以生产
        val res = this.getProducedGoods(player.position)
        return res.getAvailableNum(goodType) > 0
    }

    override fun createStartListenCommand(player: PRPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        val resource = this.getProducedGoods(player.position)
        // 整理生产货物的信息
        val showMessage = if (resource.totalNum > 0) {
            "你生产了: " + resource.resourceDescr
        } else {
            "你没有生产任何货物!"
        }
        res.public("showMessage", showMessage)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val player = action.getPlayer<PRPlayer>()
        when (action.getAsString("subact")) {
            "produce" -> {
                // 生产,并设置回应
                this.produce(action)
                // 检查玩家是否使用了双倍特权
                player.checkUsedDoublePriv()
                this.setPlayerResponsed(player)
            }
            "pass" -> {
                // 直接结束回合
                gameMode.report.doPass(player)
                this.setPlayerResponsed(player)
            }
            else -> throw BoardGameException("无效的行动代码!")
        }
    }

    /**
     * 取得生产的货物
     * @param position
     * @return
     */
    private fun getProducedGoods(position: Int) = this.getPlayerParamSet(position).get<Any>("resources") as PrPartPool

    override val validCode: Int
        get() = GameCmdConst.GAME_CODE_CRAFTSMAN

    /**
     * 取得玩家允许的生产货物数量
     * @param player
     * @return
     */
    private fun getValidProduceNum(player: PRPlayer): Int {
        var res = 0
        if (player.character == Character.CRAFTSMAN) {
            res += 1
            // 拥有双倍特权则再加1
            if (player.canUseDoublePriv) {
                res += 1
            }
        }
        return res
    }

    override fun initListeningPlayers() {
        // 只有选择手工业者玩家才需要进行动作
        gameMode.game.players.forEach { p ->
            this.setNeedPlayerResponse(p.position, p.character == Character.CRAFTSMAN)
        }
    }

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        super.onAllPlayerResponsed()
        // 检查所有玩家生产货物后的建筑能力
        gameMode.game.players.forEach(this::afterProduced)
    }

    /**
     * 玩家进行生产行动
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun produce(action: GameAction) {
        val player = action.getPlayer<PRPlayer>()
        val resourceString = action.getAsString("resourceString")
        val resources = PrUtils.getPartInfo(resourceString)
        if (resources.totalNum <= 0) {
            throw BoardGameException("你没有选择货物!")
        }
        val validNum = this.getValidProduceNum(player)
        if (resources.totalNum > validNum) {
            throw BoardGameException("你只能生产 $validNum 个货物!")
        }
        if (!gameMode.partPool.hasParts(resources)) {
            throw BoardGameException("公共资源堆的货物数量不足,请重新选择!")
        }
        resources.parts
                .map { it as GoodType }
                .any { resources.getAvailableNum(it) > 0 && !this.canProduce(player, it) }
                && throw BoardGameException("你不能生产该种货物!")

        // 生产货物并添加给玩家
        gameMode.partPool.takeParts(resources)
        player.resources.putParts(resources)
        // 将生产资源的信息发送到客户端
        gameMode.game.sendPlayerGetPartResponse(player, resources, 1)
        gameMode.game.sendSupplyGetPartResponse(resources, -1)
        gameMode.report.doProduce(player, resources)
        // 将该货物的数量添加到玩家生产货物的总数中
        val pp = this.getProducedGoods(player.position)
        pp.putParts(resources)
    }

    /**
     * 为玩家生产货物
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun produceGood(player: PRPlayer) {
        val buildings = player.buildings
        val plantations = player.fields
        // 整理出建筑中可以生产货物的总数
        val bmap = buildings.mapNotNull { it.goodType?.to(it.colonistNum) }
                .groupBy(Pair<GoodType, Int>::first)
                .mapValues { it.value.sumBy(Pair<GoodType, Int>::second) }
        // 整理出种植园中可以生产货物的总数
        val pmap = plantations.mapNotNull { it.goodType?.to(it.colonistNum) }
                .groupBy(Pair<GoodType, Int>::first)
                .mapValues { it.value.sumBy(Pair<GoodType, Int>::second) }
        val goods = PrPartPool()
        // 取得实际生产货物的数量,为两者中小的那个
        for (goodType in GoodType.values()) {
            var num: Int = if (goodType == GoodType.CORN) {
                // 玉米无需建筑就能生产
                pmap.getOrDefault(goodType, 0)
            } else {
                minOf(bmap.getOrDefault(goodType, 0), pmap.getOrDefault(goodType, 0))
            }
            // 从资源堆中取得可用的货物数量
            num = gameMode.partPool.takePart(goodType, num)
            goods.putPart(goodType, num)
        }
        this.whenProducing(player, goods)
        // 将生产的资源添加给玩家
        player.resources.putParts(goods)
        // 将生产资源的信息发送到客户端
        gameMode.game.sendPlayerGetPartResponse(player, goods, 1)
        gameMode.game.sendSupplyGetPartResponse(goods, -1)
        gameMode.report.doProduce(player, goods)
        this.setProducedGoods(player.position, goods)
    }

    /**
     * 保存生产的货物
     * @param position
     * @param res
     */
    private fun setProducedGoods(position: Int, res: PrPartPool) {
        this.getPlayerParamSet(position)["resources"] = res
    }

    /**
     * 玩家生产货物时的行动
     * @param player
     * @param goods
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun whenProducing(player: PRPlayer, goods: PrPartPool) {
        if (player.hasAbility(Ability.PRODUCE_ADDITION)) {
            // 如果拥有大型糖厂或大型染料厂生产额外货物的能力,则处理以下代码
            // 13-大型染料厂 14-大型糖厂
            listOf(
                    BuildingPool.INDIGO_FACTORY to GoodType.INDIGO,
                    BuildingPool.SUGAR_FACTORY to GoodType.SUGAR
            ).forEach { (buildingType, goodType) ->
                val tile = player.getBuildingTile(buildingType)
                if (tile != null && tile.colonistNum > 0 && goods.getAvailableNum(goodType) > 0) {
                    // 如果存在大型染料厂,并且上面有拓荒者,并且生产了染料,则得到额外1个染料
                    val num = gameMode.partPool.takePart(goodType, 1)
                    if (num > 0) {
                        goods.putPart(goodType, num)
                    }
                }
            }
//            var tile = player.getBuildingTile(BuildingPool.INDIGO_FACTORY)
//            if (tile != null && tile.colonistNum > 0 && goods.getAvailableNum(GoodType.INDIGO) > 0) {
//                // 如果存在大型染料厂,并且上面有拓荒者,并且生产了染料,则得到额外1个染料
//                val num = gameMode.partPool.takePart(GoodType.INDIGO, 1)
//                if (num > 0) {
//                    goods.putPart(GoodType.INDIGO, num)
//                }
//            }
//            tile = player.getBuildingTile(BuildingPool.SUGAR_FACTORY)
//            if (tile != null && tile.colonistNum > 0 && goods.getAvailableNum(GoodType.SUGAR) > 0) {
//                // 如果存在大型糖厂,并且上面有拓荒者,并且生产了糖,则得到额外1个糖
//                val num = gameMode.partPool.takePart(GoodType.SUGAR, 1)
//                if (num > 0) {
//                    goods.putPart(GoodType.SUGAR, num)
//                }
//            }
        }
    }
}
