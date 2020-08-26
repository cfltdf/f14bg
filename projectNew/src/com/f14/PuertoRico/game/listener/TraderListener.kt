package com.f14.PuertoRico.game.listener

import com.f14.F14bg.network.CmdFactory
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


class TraderListener(gameMode: PRGameMode) : PROrderActionListener(gameMode) {

    override fun beforeListeningCheck(player: PRPlayer): Boolean {
        return this.canTrade(player)
    }

    /**
     * 判断玩家是否可以自己出售货物
     * @param player
     * @return
     */
    private fun canSelfTrade(player: PRPlayer): Boolean {
        return player.hasAbility(Ability.SELF_TRADE)
    }

    /**
     * 判断玩家是否可以执行交易行动
     * @param gameMode
     * @param player
     * @return
     */
    private fun canTrade(player: PRPlayer): Boolean {
        if (player.resources.isEmpty) {
            return false
        }
        // 如果用户拥有直接卖货的能力,则总是可以执行交易行动
        if (this.canSelfTrade(player)) {
            return true
        } else {
            // 如果没有该能力,则判断交易所是否满了,满了则不能执行交易行动
            if (gameMode.tradeHouse.isFull) {
                return false
            }
        }
        player.resources.parts.map { it as GoodType }.filter { player.resources.getAvailableNum(it) > 0 }.forEach {
            when {
                this.canTradeSameGood(player) -> // 如果可以出售相同类型的货物,则该玩家总能进行交易
                    return true
                !gameMode.tradeHouse.contain(it) -> // 否则就判断交易所是否已经拥有相同种类的货物
                    return true
            }
        }
        return false
    }

    /**
     * 判断玩家是否可以出售相同类型的货物
     * @param player
     * @return
     */
    private fun canTradeSameGood(player: PRPlayer): Boolean {
        return player.hasAbility(Ability.SELL_SAME)
    }

    override fun createStartListenCommand(player: PRPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 是否可以使用贸易站的标志
        res.public("canSelfTrade", this.canSelfTrade(player))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val player = action.getPlayer<PRPlayer>()
        when (action.getAsString("subact")) {
            "trade" -> {
                // 交易,并设置回应
                this.trade(action)
                // 检查玩家是否使用了双倍特权
                player.checkUsedDoublePriv()
                this.setPlayerResponsed(player)
            }
            "pass" -> {
                // 随时可以跳过
                gameMode.report.doPass(player)
                this.setPlayerResponsed(player)
            }
            else -> throw BoardGameException("无效的行动代码!")
        }
    }

    /**
     * 取得额外的交易价格
     * @param player
     * @param goodType
     * @param selfTrade
     * @return
     */
    private fun getAdditionalCost(player: PRPlayer, goodType: GoodType, selfTrade: Boolean): Int {
        var res = 0
        if (player.character == Character.TRADER) {
            res += 1
            // 如果拥有特权则再多卖1块钱
            if (player.canUseDoublePriv) {
                res += 1
            }
        }
        if (!selfTrade) {
            // 使用私人交易所时不能应用其他能力
            if (player.hasAbility(Ability.SELL_1)) {
                res += 1
            }
            if (player.hasAbility(Ability.SELL_2)) {
                res += 2
            }
        }
        return res
    }

    override val validCode: Int
        get() = GameCmdConst.GAME_CODE_TRADER

    override fun initListeningPlayers() {
        // 如果某个玩家没有货物,则跳过他
        for (p in gameMode.game.players) {
            if (p.resources.isEmpty) {
                this.setNeedPlayerResponse(p.position, false)
            } else {
                this.setNeedPlayerResponse(p.position, true)
            }
        }
    }

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        // 结束时需要检查,如果交易所已经满了,则清空
        if (gameMode.tradeHouse.isFull) {
            // 将清空的货物放回到公共资源
            val parts = PrPartPool()
            for (goodType in gameMode.tradeHouse.goods) {
                gameMode.partPool.putPart(goodType, 1)
                parts.putPart(goodType, 1)
            }
            gameMode.tradeHouse.clear()
            // 将公共资源得到货物的信息发送到客户端
            gameMode.game.sendSupplyGetPartResponse(parts, 1)
            gameMode.report.clearTradeHouse()
        }
        // 将交易所的信息发送到客户端
        gameMode.game.sendTradeHouseInfo()
    }

    /**
     * 玩家进行交易行动
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun trade(action: GameAction) {
        val player = action.getPlayer<PRPlayer>()
        val good = action.getAsString("goodType")
        val goodType = PrUtils.getGoodType(good)
        if (player.resources.getAvailableNum(goodType) <= 0) {
            throw BoardGameException("你没有该种货物!")
        }
        val selfTrade = action.getAsBoolean("selfTrade")
        if (selfTrade) {
            if (!this.canSelfTrade(player)) {
                throw BoardGameException("你不能使用交易所进行交易!")
            }
            // 如果使用自己的交易所进行交易
            player.resources.takePart(goodType, 1)
            // 取得交易价格
            var doubloon = gameMode.tradeHouse.getBaseCost(goodType)
            doubloon += this.getAdditionalCost(player, goodType, true)
            // 发送出售货物的消息到客户端
            val goods = PrPartPool()
            goods.putPart(goodType, 1)
            gameMode.game.sendPlayerGetPartResponse(player, goods, -1)
            // 发送取得金钱的消息到客户端
            gameMode.game.getDoubloon(player, doubloon)
            // 将卖掉的货物放回到公共资源
            gameMode.partPool.putPart(goodType, 1)
            // 将公共资源得到货物的信息发送到客户端
            gameMode.game.sendSupplyGetPartResponse(goods, 1)
            gameMode.report.useAbility(player, Ability.SELF_TRADE)
            gameMode.report.doTrade(player, goodType, doubloon)
        } else {
            if (gameMode.tradeHouse.isFull) {
                throw BoardGameException("交易所已满,不能进行交易!")
            }
            if (!this.canTradeSameGood(player)) {
                // 如果不能出售相同类型的货物,则需要检查交易所中是否已经存在该种货物
                if (gameMode.tradeHouse.contain(goodType)) {
                    throw BoardGameException("不能出售相同类型的货物!")
                }
            }
            player.resources.takePart(goodType, 1)
            // 卖出货物并取得交易价格
            var doubloon = gameMode.tradeHouse.sell(goodType)
            doubloon += this.getAdditionalCost(player, goodType, false)
            // 发送出售货物的消息到客户端
            val goods = PrPartPool()
            goods.putPart(goodType, 1)
            gameMode.game.sendPlayerGetPartResponse(player, goods, -1)
            // 发送取得金钱的消息到客户端
            gameMode.game.getDoubloon(player, doubloon)
            // 发送交易所得到货物的消息
            CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_GET_TRADEHOUSE, -1)
                    .public("goodType", goodType)
                    .send(gameMode)
            gameMode.report.doTrade(player, goodType, doubloon)
        }
    }

}
