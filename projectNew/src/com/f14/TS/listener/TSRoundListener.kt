package com.f14.TS.listener

import com.f14.TS.ActiveResult
import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.consts.*
import com.f14.TS.executor.TSAdjustInfluenceExecutor
import com.f14.TS.executor.TSOpActionExecutor
import com.f14.TS.factory.InitParamFactory
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class TSRoundListener(gameMode: TSGameMode) : TSOrderListener(gameMode) {

    /**
     * 使用OP进行行动
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun activeOpAction(player: TSPlayer) {
        val p = this.getParam<RoundParam>(player)
        val initParam = InitParamFactory.createOpActionParam(gameMode, player, p.selectedCard!!, TrigType.ACTION)
        val executor = TSOpActionExecutor(player, gameMode, initParam)
        executor.execute()
    }

    /**
     * 触发所选牌的事件
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun activeSelectedEvent(player: TSPlayer) {
        val p = this.getParam<RoundParam>(player)
        p.result = gameMode.game.activeCardEvent(player, p.selectedCard!!)
    }

    override fun beforeListeningCheck(player: TSPlayer) =// 如果当前轮数已经超过了玩家本回合可执行的轮数,则跳过玩家执行
            gameMode.turn <= player.actionRoundNumber && super.beforeListeningCheck(player)

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建参数
        for (player in gameMode.game.players) {
            val p = RoundParam()
            this.setParam(player, p)
        }
    }

    /**
     * 检查是否需要触发#106-北美空军司令部的效果
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun checkEvent106() {
        if (gameMode.game.usaPlayer.params.getBoolean(106)) {
            // 重置参数
            gameMode.game.usaPlayer.params.setGameParameter(106, false)
            // 创建监听器参数并插入执行该监听器
            val initParam = InitParamFactory.createGivenPointInfluence(SuperPower.USA, 1)
            val executor = TSAdjustInfluenceExecutor(gameMode.game.usaPlayer, gameMode, initParam)
            executor.execute()
        }

    }

    /**
     * 检查一些强制生效的效果
     * @param player
     */
    private fun checkForceEffect(player: TSPlayer) {
        gameMode.game.checkForceEffect(player, TrigType.EVENT, null)
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        when (action.getAsString("subact")) {
            TSCmdString.ACTION_USE_OP -> this.doOpAction(action)
            TSCmdString.ACTION_ACTIVE_EVENT -> this.doActiveEvent(action)
            TSCmdString.ACTION_SPACE_RACE -> this.doSpaceRace(action)
            TSCmdString.ACTION_CHINA_CARD -> this.playChinaCard(action)
            ConfirmString.PASS -> this.doPass(action)
            TSCmdString.RESIGN -> this.resign(action)
            else -> throw BoardGameException("无效的行动指令!")
        }
    }

    /**
     * 触发事件
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doActiveEvent(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val p = this.getParam<RoundParam>(player)
        val card = this.getSelectedCard(action)
        val type = TrigType.EVENT

        // 只有发生自己或者中立牌的事件时,才检查是否可以发生事件
        if (card.superPower == SuperPower.NONE || card.superPower == player.superPower) {
            if (!gameMode.eventManager.canActiveCard(card)) {
                throw BoardGameException("所选牌的事件不能发生!")
            }
        }
        p.selectedCard = card

        // 输出战报信息
        gameMode.game.playerPlayCard(player, card)
        gameMode.report.playerPlayCard(player, card, type)
        // 触发前置事件
        gameMode.game.onPlayerAction(player, type, card)
        // 执行触发事件
        this.activeSelectedEvent(player)
        if (card.superPower == player.superPower.oppositeSuperPower) {
            this.activeOpAction(player)
        }
        this.checkEvent106()
        this.setPlayerResponsed(player)
    }

    /**
     * 使用OP进行行动
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doOpAction(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val card = this.getSelectedCard(action)
        val type = TrigType.ACTION

        val p = this.getParam<RoundParam>(player)
        p.checkDoAction(card)
        p.selectedCard = card

        // 输出战报信息
        gameMode.game.playerPlayCard(player, card)
        gameMode.report.playerPlayCard(player, card, type)
        // 触发前置事件
        gameMode.game.onPlayerAction(player, type, card)
        // 使用OP进行行动
        this.activeOpAction(player)
        if (card.superPower == player.superPower.oppositeSuperPower) {
            this.activeSelectedEvent(player)
        } else {
            // 如果是使用OP的话,则将该牌加入弃牌堆
            val res = ActiveResult(player, false)
            gameMode.game.processActivedCard(p.selectedCard!!, res)
        }
        this.checkEvent106()
        this.setPlayerResponsed(player)
    }

    /**
     * 结束回合
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doPass(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        // 第八回合总是可以结束回合
        // 只有没有手牌时,才可以结束回合
        if (gameMode.turn <= player.params.getInteger("actionRound") && !player.hands.empty) {
            throw BoardGameException("你不能结束回合!")
        }
        // 触发前置事件
        gameMode.game.onPlayerAction(player, null, null)
        // 完成回应
        this.setPlayerResponsed(player)
    }

    /**
     * 执行太空竞赛
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doSpaceRace(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val p = this.getParam<RoundParam>(player)
        // 只有未执行过行动或事件时才能进行太空竞赛
        if (p.selectedCard != null) throw BoardGameException("不能进行太空竞赛!")

        val card = this.getSelectedCard(action)
        this.spaceRace(player, card)

        // 我是分割线... 不加线不舒服斯基
        gameMode.report.line()

        // 完成回应
        this.setPlayerResponsed(player)
    }

    /**
     * 从参数中取得选中的卡牌
     * @param action
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun getSelectedCard(action: GameAction): TSCard {
        val player = action.getPlayer<TSPlayer>()
        val p = this.getParam<RoundParam>(player)
        return if (p.selectedCard != null) {
            // 如果存在选中的卡牌,返回该卡牌
            p.selectedCard!!
        } else {
            // 否则从界面选择取得卡牌
            val cardId = action.getAsString("cardId")
            player.getCard(cardId)
        }
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_ROUND

    @Throws(BoardGameException::class)
    override fun onPlayerResponsed(player: TSPlayer) {
        super.onPlayerResponsed(player)
        val param = this.getParam<RoundParam>(player)
        if (param.selectedCard != null) {
            // 如果selectedCard为空,则表示玩家没有进行行动
            if (gameMode.game.isChinaCard(param.selectedCard)) {
                // 如果打出的是中国牌,则更换中国牌的所属玩家
                val opposite = gameMode.game.getOppositePlayer(player.superPower)
                gameMode.game.changeChinaCardOwner(opposite, false)
            }
            // 玩家行动轮结束时,移除行动轮生效的能力
            val cards = gameMode.eventManager.removeRoundEffectCards(player.superPower)
            for (o in gameMode.game.players) cards.forEach(o::removeEffect)
            gameMode.game.sendRemoveActivedCardsResponse(cards, null)

            // 我是分割线...
            gameMode.report.line()
        }
    }

    override fun onPlayerStartListen(player: TSPlayer) {
        super.onPlayerStartListen(player)
        // 发送按钮的信息
        this.sendButtonInfo(player)
    }

    @Throws(BoardGameException::class)
    override fun onPlayerTurn(player: TSPlayer) {
        super.onPlayerTurn(player)
        // 发送行动开始的信息
        gameMode.report.action(player, "行动开始...")
        // 设置回合玩家属性
        gameMode.turnPlayer = player

        // 刷新玩家效果的回合记数
        gameMode.eventManager.refreshActiveRound(player.superPower)

        val forcePlayScoreCard = player.forcePlayScoreCards(gameMode.turn)
        // 不强制出计分牌
        when {
            player.hasEffect(EffectType.KREMLIN_FLU) -> {
                // 检查玩家是否有 克里姆林流感 的效果，有的话就创建打计分牌的中断监听器
                val card = player.getCardByEffectType(EffectType.KREMLIN_FLU)
                // 先处理玩家其他需要强制执行的效果
                this.checkForceEffect(player)
                if (player.scoreCardsCount > 0) {
                    val ip = InitParamFactory.createActionInitParam(player, null, null)
                    val l = TSScoreCardListener(player, gameMode, ip)
                    gameMode.insertListener(l)
                }
                this.setPlayerResponsed(player)
                player.removeEffect(card)
            }
            player.hasEffect(EffectType.QUAGMIRE) -> {
                // 检查玩家是否有 困境 的效果，有的话就创建困境的中断监听器
                // 先处理玩家其他需要强制执行的效果
                this.checkForceEffect(player)
                if (forcePlayScoreCard) {
                    // 如果需要强制出计分牌,则允许出计分牌,创建出计分牌的监听器
                    val ip = InitParamFactory.createActionInitParam(player, null, null)
                    val l = TSScoreCardListener(player, gameMode, ip)
                    gameMode.insertListener(l)
                } else {
                    // 创建困境监听器
                    val card = player.getCardByEffectType(EffectType.QUAGMIRE)
                    val ip = InitParamFactory.createActionInitParam(player, card, null)
                    val l = TSQuagmireListener(player, gameMode, ip)
                    gameMode.insertListener(l)
                }
                this.setPlayerResponsed(player)
            }
            player.hasEffect(EffectType.EFFECT_49) -> // 检查玩家是否有#49-导弹嫉妒的效果,本回合必须用导弹嫉妒作为行动
                if (forcePlayScoreCard) {
                    // 先处理玩家其他需要强制执行的效果
                    this.checkForceEffect(player)
                    // 如果需要强制出计分牌,则允许出计分牌,创建出计分牌的监听器
                    val ip = InitParamFactory.createActionInitParam(player, null, null)
                    val l = TSScoreCardListener(player, gameMode, ip)
                    gameMode.insertListener(l)
                    this.setPlayerResponsed(player)
                } else {
                    // 创建执行导弹嫉妒的监听器
                    val card = player.getCardByEffectType(EffectType.EFFECT_49)
                    if (player.hands.cards.contains(card)) {
                        // 先处理玩家其他需要强制执行的效果
                        this.checkForceEffect(player)
                        do {
                            try {
                                val ip = InitParamFactory.createActionInitParam(player, card, null)
                                val l = Custom49RoundListener(player, gameMode, ip)
                                val res = gameMode.insertListener(l)
                                val subact = res.getString("subact")
                                val p = this.getParam<RoundParam>(player.position)
                                p.selectedCard = card
                                if (TSCmdString.ACTION_USE_OP == subact) {
                                    // 执行OP
                                    // 输出战报信息
                                    gameMode.report.playerPlayCard(player, card, TrigType.ACTION)
                                    // 使用OP进行行动
                                    this.activeOpAction(player)
                                    gameMode.game.playerRemoveHand(player, card)
                                    // 将#49牌放入弃牌堆
                                    gameMode.game.discardCard(card)
                                    this.checkEvent106()
                                } else if (TSCmdString.ACTION_SPACE_RACE == subact) {
                                    // 太空竞赛
                                    this.spaceRace(player, card)
                                }
                                // 行动完成后,移除玩家的#49-导弹嫉妒的效果
                                gameMode.game.playerRemoveActivedCard(player, card)
                                break
                            } catch (e: BoardGameException) {
                                player.sendException(gameMode.game.room.id, e)
                            }

                        } while (true)
                        this.setPlayerResponsed(player)
                    } else {
                        player.removeEffect(card)
                    }
                }
        }
    }

    /**
     * 打出中国牌
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun playChinaCard(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        // 检查玩家是否可以打出中国牌
        if (gameMode.cardManager.chinaOwner != player.superPower || !gameMode.cardManager.chinaCanUse) {
            throw BoardGameException("你不能打出中国牌!")
        }
        val card = gameMode.cardManager.chinaCard
        val p = this.getParam<RoundParam>(player)
        p.checkDoAction(card)
        val type = TrigType.ACTION
        p.selectedCard = card
        // 使用OP进行行动
        val initParam = InitParamFactory.createChoiceInitParam(gameMode, player, card, type)
        val l = TSChoiceChinaCardListener(player, gameMode, initParam)
        val res = gameMode.insertListener(l)
        when (res.getInteger("choice")) {
            1 -> {
                // 将中国牌的效果添加给出牌的玩家
                gameMode.game.activeCardEvent(player, card)
                // 触发前置事件
                gameMode.game.onPlayerAction(player, type, card)
                // 输出战报信息
                gameMode.report.playerPlayCard(player, card, type)
                // 使用OP进行行动
                this.activeOpAction(player)
            }
            2 ->
                // 只有未执行过行动或事件时才能进行太空竞赛
                this.spaceRace(player, card)
        }
        // 完成回应
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    private fun resign(action: GameAction) {
        gameMode.game.playerResigen(action.getPlayer())
    }

    /**
     * 发送玩家的按键信息
     * @param player
     */
    private fun sendButtonInfo(player: TSPlayer) {
        val p = this.getParam<RoundParam>(player)
        val res = this.createSubactResponse(player, "button")
        res.public("spaceRaceChance", player.availableSpaceRaceTimes)
        var style = "normal"
        if (p.eventActived) {
            // 如果已经发生过事件,则将按键的状态设为行动模式
            style = "action"
        }
        res.public("style", style)
        if (p.selectedCard != null) {
            // res.public("cardId", p.selectedCard.id);
        }
        // 设置中国牌是否可用的状态
        res.public("chinaCard", gameMode.cardManager.chinaOwner == player.superPower && gameMode.cardManager.chinaCanUse)
        gameMode.game.sendResponse(player, res)
    }

    @Throws(BoardGameException::class)
    private fun spaceRace(player: TSPlayer, card: TSCard) {
        // 检查玩家是否可以进行太空竞赛
        gameMode.spaceRaceManager.checkSpaceRace(player, card)
        // 触发前置事件
        gameMode.game.onPlayerAction(player, null, null)
        // 执行太空竞赛
        gameMode.game.playerSpaceRace(player, card)
    }

    /**
     * 玩家的回合参数
     * @author F14eagle
     */
    private inner class RoundParam {
        /**
         * 选择的卡牌
         */
        var selectedCard: TSCard? = null
        /**
         * 是否发生过事件
         */
        var eventActived = false
        /**
         * 是否使用过行动点
         */
        var actionActived = false
        /**
         * 事件触发结果
         */
        var result: ActiveResult? = null

        /**
         * 检查是否可以以行动方式出牌
         * @param card
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun checkDoAction(card: TSCard) {
            if (card.cardType == CardType.SCORING) {
                throw BoardGameException("计分牌只能以 发生事件 的方式打出!")
            }
            // if(this.selectedCard!=null){
            // throw new BoardGameException("每个行动论只能打出1张牌!");
            // }
        }

    }

}
