package com.f14.PuertoRico.game.listener

import com.f14.F14bg.network.CmdFactory
import com.f14.PuertoRico.component.PRTile
import com.f14.PuertoRico.component.PrPartPool
import com.f14.PuertoRico.consts.Ability
import com.f14.PuertoRico.consts.Character
import com.f14.PuertoRico.consts.GameCmdConst
import com.f14.PuertoRico.consts.Part
import com.f14.PuertoRico.game.PRGameMode
import com.f14.PuertoRico.game.PRPlayer
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException

class SettleListener(gameMode: PRGameMode) : PROrderActionListener(gameMode) {

    /**
     * 增加玩家进行拓荒行动的次数
     * @param player
     */
    private fun addActionTime(player: PRPlayer) {
        val actionTimes = this.getPlayerParamSet(player.position).getInteger("actionTimes")
        this.setPlayerParam(player.position, "actionTimes", actionTimes + 1)
    }

    /**
     * 拓荒完成后
     * @param player
     * @param plantation
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun afterSettle(player: PRPlayer, plantation: PRTile) {
        // 创建拓荒参数
        val param = SettleParam()
        param.plantation = plantation
        this.setParam(player.position, param)
        // 检查玩家是否拥有将刚才完成拓荒的地换成森林的能力
        if (player.hasAbility(Ability.FOREST)) {
            this.addActionStep(player, ChooseForestStep())
        }
        // 检查玩家是否拥有给刚完成拓荒的种植园放置移民的能力
        if (plantation.colonistMax > 0 && player.hasAbility(Ability.COLONIST_SETTLE)) {
            // 发送消息到客户端提示是否使用能力
            this.addActionStep(player, GetColonistStep())
        }
    }

    /**
     * 判断玩家是否可以进行开拓阶段
     * @param player
     * @return
     */
    private fun canSettle(player: PRPlayer): Boolean {
        return !(player.fields.size >= gameMode.builtNum || gameMode.plantations.empty)
    }

    /**
     * 判断玩家是否可以拿取采石场
     * @param player
     * @return
     */
    private fun canTakeQuarry(player: PRPlayer): Boolean {
        return player.character == Character.SETTLE || player.hasAbility(Ability.QUARRY)
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val player = action.getPlayer<PRPlayer>()
        when (action.getAsString("subact")) {
            "settle" -> {
                // 执行开拓
                this.settle(action)
                if (this.getCurrentActionStep(player) == null) {
                    // 如果没有后续步骤,则按照拓荒玩家的状态设置其状态
                    this.setSettlePlayerResponsed(player)
                }
                // 检查玩家是否使用了双倍特权
                player.checkUsedDoublePriv()
            }
            "pass" -> {
                // 直接结束回合
                gameMode.report.doPass(player)
                this.setPlayerResponsed(player)
            }
            else -> throw BoardGameException("无效的行动代码!")
        }
    }

    override val validCode: Int
        get() = GameCmdConst.GAME_CODE_SETTLE

    override fun initListeningPlayers() {
        // 如果某个玩家的种植园已经满了,则他无须选择
        for (p in gameMode.game.players) {
            // 如果玩家的种植园已经达到上限,则跳过他
            if (p.fields.size >= gameMode.builtNum) {
                this.setNeedPlayerResponse(p.position, false)
            } else {
                this.setNeedPlayerResponse(p.position, true)
            }
        }
    }

    /**
     * 判断玩家是否第一次进行拓荒行动
     * @param player
     * @return
     */
    private fun isFirstAction(player: PRPlayer): Boolean {
        val actionTimes = this.getPlayerParamSet(player.position).getInteger("actionTimes")
        return actionTimes == 0
    }

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        // 结束时需要抽取新的种植园板块,数量为玩家人数+1
        val num = gameMode.game.currentPlayerNumber + 1
        val tiles = gameMode.plantationsDeck.draw(num)
        gameMode.plantationsDeck.discard(gameMode.plantations.cards)
        gameMode.plantations.cards.clear()
        gameMode.plantations.cards.addAll(tiles)
        // 将种植园的信息发送到客户端
        gameMode.game.sendPlantationsInfo()
        gameMode.report.listPlantations(tiles)
    }

    @Throws(BoardGameException::class)
    override fun onPlayerTurn(player: PRPlayer) {
        super.onPlayerTurn(player)
        // 判断玩家是否拥有取得种植园的能力,并且可以取得种植园,只有第一次进行拓荒行动时才能触发该能力
        // 如果有则提示玩家选择是否使用该能力
        if (player.hasAbility(Ability.PLANTATION) && player.fields.size < gameMode.builtNum && !gameMode.plantationsDeck.empty && this.isFirstAction(player)) {
            this.addActionStep(player, DrawPlantationStep())
        }
    }

    /**
     * 设置拓荒玩家的回应状态
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun setSettlePlayerResponsed(player: PRPlayer) {
        if (player.character == Character.SETTLE && player.canUseDoublePriv && this.isFirstAction(player)) {
            // 如果该玩家是拓荒者,并且他拥有双倍权限,则他可以在其他玩家行动结束后再进行一次拓荒
            this.setPlayerResponsedTemp(player)
            this.addActionTime(player)
        } else {
            // 否则则设置回应
            this.setPlayerResponsed(player)
        }
    }

    /**
     * 玩家进行开拓行动
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun settle(action: GameAction) {
        val player = action.getPlayer<PRPlayer>()
        if (!this.canSettle(player)) {
            throw BoardGameException("不能进行拓荒行动!")
        }
        val quarry = action.getAsBoolean("quarry")
        if (quarry) {
            if (!this.isFirstAction(player)) {
                throw BoardGameException("只有第一次拓荒行动时才能选择采石场!")
            }
            // 拿取采石场
            if (!this.canTakeQuarry(player)) {
                throw BoardGameException("你不能选择采石场!")
            }
            val tile = gameMode.quarriesDesk.draw() ?: throw BoardGameException("已经没有采石场了!")
            player.tiles.addCard(tile)
            gameMode.partPool.takePart(Part.QUARRY)

            // 将信息发送给客户端
            CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_GET_PLANTATION, player.position)
                    .public("id", tile.id)
                    .send(gameMode)
            // 采石场数量-1消息
            val parts = PrPartPool()
            parts.putPart(Part.QUARRY, 1)
            gameMode.game.sendSupplyGetPartResponse(parts, -1)
            gameMode.report.getTile(player, tile)
            this.afterSettle(player, tile)
        } else {
            // 拿取种植园
            val id = action.getAsString("id")
            val tile = gameMode.plantations.getCard(id)
            // 取得板块并添加给玩家,从公开的板块区移除该板块
            player.tiles.addCard(tile)
            gameMode.plantations.cards.remove(tile)

            // 将信息发送给客户端
            CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_GET_PLANTATION, player.position)
                    .public("id", id)
                    .send(gameMode)
            CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_REMOVE_PLANTATION, -1)
                    .public("id", tile.id)
                    .send(gameMode)
            gameMode.report.getTile(player, tile)
            this.afterSettle(player, tile)
        }
    }

    /**
     * 拓荒者阶段步骤
     * @author F14eagle
     */
    private enum class SettleStep {
        DRAW_PLANTATION, SETTLE_COLONIST, CHOOSE_FOREST
    }

    /**
     * 选择森林的步骤
     * @author F14eagle
     */
    private inner class ChooseForestStep : PrActionStep(this) {

        @Throws(BoardGameException::class)
        override fun doAction(action: GameAction) {
            val confirm = action.getAsBoolean("confirm")
            if (confirm) {
                val player = action.getPlayer<PRPlayer>()
                // 将玩家拓荒完成的种植园换成森林
                val param = getParam<SettleParam>(player.position)
                val plantation = param.plantation ?: throw BoardGameException("你没有进行拓荒,不能选择森林!")

                val tile = gameMode.forestDeck.draw() ?: throw BoardGameException("已经没有森林了!")
                // 移除刚拓荒的地并添加森林
                player.tiles.removeCard(plantation)
                player.tiles.addCard(tile)

                // 将信息发送给客户端
                CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_REMOVE_PLANTATION, player.position)
                        .public("id", plantation.id)
                        .send(gameMode)
                CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_GET_PLANTATION, player.position)
                        .public("id", tile.id)
                        .send(gameMode)
                gameMode.report.changeTile(player, plantation, tile)

                // 完成该操作后移除所有剩余的步骤
                this.clearOtherStep = true
            }
        }


        override val message: String
            get() = "是否要将刚开拓的种植园换成森林?"

        override val stepCode: String
            get() = SettleStep.CHOOSE_FOREST.toString()

        @Throws(BoardGameException::class)
        override fun onStepOver(player: PRPlayer) {
            super.onStepOver(player)
            // 如果没有下一步骤了,则设置玩家回应
            if (getCurrentActionStep(player) == null) {
                setSettlePlayerResponsed(player)
            }
        }

    }

    /**
     * 抽取种植园板块的步骤
     * @author F14eagle
     */
    private inner class DrawPlantationStep : PrActionStep(this) {

        @Throws(BoardGameException::class)
        override fun doAction(action: GameAction) {
            val confirm = action.getAsBoolean("confirm")
            if (confirm) {
                val player = action.getPlayer<PRPlayer>()
                if (player.fields.size >= gameMode.builtNum) {
                    throw BoardGameException("你的种植园已经满了!")
                }
                // 执行抽取种植园板块
                val tile = gameMode.plantationsDeck.draw() ?: throw BoardGameException("已经没有可用的种植园了!")
                player.addTile(tile)
                // 将信息发送给客户端
                CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_GET_PLANTATION, player.position).public("id", tile.id).send(gameMode)
                gameMode.report.useAbility(player, Ability.PLANTATION)
                gameMode.report.getTile(player, tile)
            }
        }


        override val message: String
            get() = "是否抽取种植园板块?"

        override val stepCode: String
            get() = SettleStep.DRAW_PLANTATION.toString()

    }

    /**
     * 拓荒后得到移民的步骤
     * @author F14eagle
     */
    private inner class GetColonistStep : PrActionStep(this) {

        @Throws(BoardGameException::class)
        override fun doAction(action: GameAction) {
            val confirm = action.getAsBoolean("confirm")
            if (confirm) {
                val player = action.getPlayer<PRPlayer>()
                // 为玩家拓荒完成的种植园放置一个移民
                val param = getParam<SettleParam>(player.position)
                if (param.plantation == null) {
                    throw BoardGameException("你没有进行拓荒,不能分配移民!")
                }
                val num: Int = when {
                    gameMode.getAvailablePartNum(Part.COLONIST) > 0 -> // 如果公共资源堆还有移民则从公共资源堆中获取移民
                        gameMode.partPool.takePart(Part.COLONIST)
                    gameMode.getAvailablePartNum(Part.SHIP_COLONIST) > 0 -> // 如果公共资源堆没有移民,则看移民船上是否还有移民
                        gameMode.partPool.takePart(Part.SHIP_COLONIST)
                    else -> throw BoardGameException("已经没有剩余的移民了!")
                }
                param.plantation!!.colonistNum += num

                // 刷新公共资源和移民船上移民数到客户端
                gameMode.game.sendColonistInfo()
                // 刷新玩家拓荒种植园的移民分配情况
                gameMode.game.sendPlayerColonistInfo(player, param.plantation!!)
                gameMode.report.useAbility(player, Ability.COLONIST_SETTLE)
                gameMode.report.getColonist(player, param.plantation!!, num)
            }
        }


        override val message: String
            get() = "是否为刚开拓的种植园分配移民?"

        override val stepCode: String
            get() = SettleStep.SETTLE_COLONIST.toString()

        @Throws(BoardGameException::class)
        override fun onStepOver(player: PRPlayer) {
            super.onStepOver(player)
            // 设置玩家回应
            setSettlePlayerResponsed(player)
        }

    }

    /**
     * 拓荒阶段参数
     * @author F14eagle
     */
    internal inner class SettleParam {
        var plantation: PRTile? = null
    }

}
