package com.f14.PuertoRico.game.listener

import com.f14.F14bg.network.CmdFactory
import com.f14.PuertoRico.component.PRTile
import com.f14.PuertoRico.component.PrPartPool
import com.f14.PuertoRico.consts.*
import com.f14.PuertoRico.consts.Character
import com.f14.PuertoRico.game.PRGameMode
import com.f14.PuertoRico.game.PRPlayer
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import net.sf.json.JSONObject
import kotlin.math.max
import kotlin.math.min


class BuilderListener(gameMode: PRGameMode) : PROrderActionListener(gameMode) {

    /**
     * 建造完成后
     * @param player
     * @param building
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun afterBuild(player: PRPlayer, building: PRTile) {
        // 检查玩家是否拥有建造建筑得VP的能力
        if (player.hasAbility(Ability.VP_BUILD)) {
            val vp = when (building.level) {
                2, 3 -> 1
                4 -> 2
                else -> 0
            }
            if (vp > 0) {
                gameMode.report.getVP(player, vp)
                gameMode.game.getVP(player, vp)
            }
        }
        // 检查玩家是否拥有给刚完成建造的建筑放置移民的能力
        if (player.hasAbility(Ability.COLONIST_BUILDER)) {
            // 创建建造参数
            val param = BuilderParam(building)
            this.setParam(player.position, param)

            // 发送消息到客户端提示是否使用能力
            this.addActionStep(player, GetColonistStep())
        }
    }

    /**
     * 玩家进行建造行动
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun build(action: GameAction) {
        val player = action.getPlayer<PRPlayer>()
        if (!this.canBuild(player)) {
            throw BoardGameException("你不能再建造建筑了!")
        }
        val cardNo = action.getAsString("cardNo")
        val building: PRTile = gameMode.buildingPool.getCard(cardNo) ?: throw BoardGameException("不能建造这个建筑!")
        if (!this.canBuild(player, building)) {
            throw BoardGameException("你没有空地建造这个建筑!")
        }
        if (player.hasTile(cardNo)) {
            throw BoardGameException("不能建造相同的建筑!")
        }
        // 取得黑市交易的参数
        val blackString = action.getAsString("blackString")
        val blackParam = BlackTradeParam(blackString)
        // 校验玩家黑市交易的参数
        blackParam.checkForPlayer(player)
        val blackDoubloon = blackParam.tradeDoubloon
        val cost = this.getRealCost(player, building)

        if (blackDoubloon == 0) {
            if (cost > player.doubloon) {
                throw BoardGameException("你的金钱不足!")
            }
        } else {
            if (cost != player.doubloon + blackDoubloon) {
                throw BoardGameException("黑市交易后的金钱必须和建筑价格相等!")
            }
        }

        // 成功购买建筑
        gameMode.buildingPool.takeCard<PRTile>(cardNo)
        player.tiles.addCard(building)
        // 减去玩家应负的钱
        val doubloon = max(-player.doubloon, -cost)
        gameMode.game.getDoubloon(player, doubloon)
        // 减去黑市交易掉的金钱/VP/移民
        blackParam.doTrade(player)

        gameMode.report.doBuild(player, building, doubloon)

        // 发送购买建筑的信息到客户端
        CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_GET_BUILDING, player.position)
                .public("id", building.id)
                .send(gameMode)
        CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_REMOVE_BUILDING_SUPPLY, -1)
                .public("cardNo", building.cardNo)
                .send(gameMode)

        this.afterBuild(player, building)
    }

    /**
     * 判断玩家是否还可以建造
     * @param player
     * @return
     */
    private fun canBuild(player: PRPlayer): Boolean {
        val size = player.buildingsSize
        return size < gameMode.builtNum
    }

    /**
     * 判断玩家是否可以建造指定的建筑
     * @param player
     * @param building
     * @return
     */
    private fun canBuild(player: PRPlayer, building: PRTile): Boolean {
        val size = player.buildingsSize
        val buildingSize = when (building.buildingType) {
            BuildingType.LARGE_BUILDING -> 2 // 大建筑占2格
            else -> 1
        }
        return size + buildingSize <= gameMode.builtNum
    }

    override fun createStartListenCommand(player: PRPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 设置是否可以使用黑市的参数
        res.public("blackTrade", player.hasAbility(Ability.BLACK_TRADE))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val player = action.getPlayer<PRPlayer>()
        when (action.getAsString("subact")) {
            "build" -> {
                // 执行建造
                this.build(action)
                // 检查玩家是否使用了双倍特权
                player.checkUsedDoublePriv()
                if (this.getCurrentActionStep(player) == null) {
                    // 如果没有后续步骤,则设置回应
                    this.setPlayerResponsed(player)
                }
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
     * 取得建筑物的实际费用

     * @param player
     * @param building
     * @return
     */
    private fun getRealCost(player: PRPlayer, building: PRTile): Int {
        var res = building.cost
        // 建筑师费用-1
        if (player.character == Character.BUILDER) {
            res--
            // 如果拥有双倍特权则再-1
            if (player.canUseDoublePriv) {
                res--
            }
        }
        // 采石场可以抵消一定的费用
        res -= min(building.level, player.availableQuarryNum)
        // 每2个森林可以降低一点费用
        res -= player.forests.size / 2
        // 费用不能小于0
        res = max(0, res)
        return res
    }

    override val validCode: Int
        get() = GameCmdConst.GAME_CODE_BUILDER

    /**
     * 建筑师阶段的步骤

     * @author F14eagle
     */
    internal enum class BuilderStep {
        BUILDER_COLONIST
    }

    /**
     * 黑市交易时所用的参数
     * @author F14eagle
     */
    private inner class BlackTradeParam(jsonStr: String) {
        private val tradeVP: Boolean
        /**
         * 黑市交易时移民所在建筑或种植园的id,如果选择的空闲的移民,则该id为-1,如果没有选择,则为空
         */
        private val colonistTileId: String?
        private val goodType: GoodType?

        /**
         * 构造函数
         */
        init {
            if (jsonStr.isNotEmpty()) {
                val jsonObject = JSONObject.fromObject(jsonStr)
                tradeVP = jsonObject.getBoolean("tradeVP")
                colonistTileId = jsonObject.getString("tileId").takeUnless(String::isNullOrEmpty)
                goodType = jsonObject.getString("goodType").takeUnless(String::isNullOrEmpty)?.let(GoodType::valueOf)
            }else{
                tradeVP = false
                colonistTileId = null
                goodType = null
            }
        }

        /**
         * 为玩家校验是否可以应用该黑市参数
         * @param player
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun checkForPlayer(player: PRPlayer) {
            val doubloon = this.tradeDoubloon
            // 如果交易价格为0则未使用黑市
            if (doubloon > 0) {
                if (!player.hasAbility(Ability.BLACK_TRADE)) {
                    throw BoardGameException("你不能使用黑市!")
                }
                if (tradeVP) {
                    if (player.vp <= 0) {
                        throw BoardGameException("你没有足够的VP进行黑市交易!")
                    }
                }
                colonistTileId?.let { colonistTileId ->
                    when (colonistTileId) {
                        "-1" -> // 交易的是空闲的移民
                            if (player.colonist <= 0) {
                                throw BoardGameException("你没有空闲的移民来进行黑市交易!")
                            }
                        else -> {
                            val tile = player.tiles.getCard(colonistTileId)
                            // 不能选择黑市或采石场上的移民
                            if (tile.ability == Ability.BLACK_TRADE || tile.part == Part.QUARRY) {
                                throw BoardGameException("你不能选择该建筑上的移民!")
                            }
                            if (tile.colonistNum <= 0) {
                                throw BoardGameException("所选的建筑中没有足够的移民来进行黑市交易!")
                            }
                        }
                    }
                }
                goodType?.let { goodType ->
                    if (player.resources.getAvailableNum(goodType) <= 0) {
                        throw BoardGameException("你没有足够的货物进行黑市交易!")
                    }
                }
            }
        }

        /**
         * 执行玩家的黑市交易行动
         * @param player
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun doTrade(player: PRPlayer) {
            if (this.tradeDoubloon > 0) {
                gameMode.report.useAbility(player, Ability.BLACK_TRADE)
            }
            if (tradeVP) {
                gameMode.report.getVP(player, -1)
                gameMode.game.getVP(player, -1)
            }
            colonistTileId?.let { colonistTileId ->
                val parts = PrPartPool()
                parts.putPart(Part.COLONIST, 1)
                when (colonistTileId) {
                    "-1" -> {
                        // 交易的是空闲的移民
                        player.colonist -= 1
                        gameMode.report.getColonist(player, -1)
                        gameMode.game.sendPlayerGetPartResponse(player, parts, -1)
                    }
                    else -> {
                        // 交易的是建筑或郊区的移民
                        val tile = player.tiles.getCard(colonistTileId)
                        tile.colonistNum -= 1
                        gameMode.report.getColonist(player, tile, -1)
                        gameMode.game.sendPlayerColonistInfo(player, tile)
                    }
                }
                gameMode.partPool.putParts(parts)
                gameMode.game.sendSupplyGetPartResponse(parts, 1)
            }
            goodType?.let { goodType ->
                val parts = PrPartPool()
                parts.putPart(goodType, 1)
                player.resources.takeParts(parts)
                gameMode.partPool.putParts(parts)
                gameMode.report.getResource(player, parts, -1)
                gameMode.game.sendPlayerGetPartResponse(player, parts, -1)
                gameMode.game.sendSupplyGetPartResponse(parts, 1)
            }
        }

        /**
         * 取得该交易参数可以交易到的金钱
         * @return
         */
        val tradeDoubloon: Int
            get() = listOf(tradeVP, colonistTileId != null, goodType != null).count { it }
    }

    /**
     * 建造参数
     * @author F14eagle
     */
    data class BuilderParam(val building: PRTile)

    /**
     * 建造后得到移民的步骤
     * @author F14eagle
     */
    private inner class GetColonistStep : PrActionStep(this) {

        @Throws(BoardGameException::class)
        override fun doAction(action: GameAction) {
            val confirm = action.getAsBoolean("confirm")
            if (confirm) {
                val player = action.getPlayer<PRPlayer>()
                // 为玩家建造完成的建筑放置一个移民
                val param = getParam<BuilderParam>(player)
                val building = param.building
                val num: Int = when {
                    gameMode.getAvailablePartNum(Part.COLONIST) > 0 -> // 如果公共资源堆还有移民则从公共资源堆中获取移民
                        gameMode.partPool.takePart(Part.COLONIST)
                    gameMode.getAvailablePartNum(Part.SHIP_COLONIST) > 0 -> // 如果公共资源堆没有移民,则看移民船上是否还有移民
                        gameMode.partPool.takePart(Part.SHIP_COLONIST)
                    else -> throw BoardGameException("已经没有剩余的移民了!")
                }
                building.colonistNum += num

                gameMode.report.useAbility(player, Ability.COLONIST_BUILDER)
                gameMode.report.getColonist(player, building, num)

                // 刷新公共资源和移民船上移民数到客户端
                gameMode.game.sendColonistInfo()
                // 刷新玩家建造建筑的移民分配情况
                gameMode.game.sendPlayerColonistInfo(player, building)
            }
        }

        override val message: String
            get() = "是否为刚建造的建筑分配移民?"

        override val stepCode: String
            get() = BuilderStep.BUILDER_COLONIST.toString()

        @Throws(BoardGameException::class)
        override fun onStepOver(player: PRPlayer) {
            super.onStepOver(player)
            // 设置玩家回应
            setPlayerResponsed(player)
        }
    }
}
