package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.*
import com.f14.TTA.executor.*
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

class TTARoundListener(gameMode: TTAGameMode) : TTAOrderListener(gameMode) {
    var politicalAction: PoliticalAction = PoliticalAction()

    /**
     * 玩家使用卡牌的能力
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun activeCard(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val cardId = action.getAsString("cardId")
        val param = this.getParam<RoundParam>(player.position)
        val card = player.getPlayedCard(cardId)
        // 检查玩家是否可以使用该卡牌
        val activeAbility = card.activeAbility ?: throw BoardGameException("该卡牌没有可以使用的能力!")
        activeAbility.checkCanActive(param.currentStep, player)

        val executor = TTAExecutorFactory.createActiveExecutor(param, card)
        executor.execute()
        if (executor.actived && executor.ability.abilityType == ActiveAbilityType.PA_RED_CROSS) {
            val target = gameMode.realPlayers.single {
                it.uncompletedWonder?.abilities?.any {
                    it.abilityType == CivilAbilityType.PA_RED_CROSS
                } == true
            }
            val targetParam = this.getParam<RoundParam>(target.position)
            targetParam.afterBuild(target.uncompletedWonder!!)
        }
        if (executor.actived && activeAbility.endPhase) {
            if (activeAbility.activeStep == RoundStep.POLITICAL) {
                this.politicalAction.endPoliticalPhase(player, false)
            } else {
                this.endActionPhase(player)
            }
        }
    }

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 结束宗教裁判所
        if (gameMode.inquisitionPosition == player.position) {
            gameMode.inquisitionPosition = -1
            gameMode.report.info("宗教裁判所结束!")
            gameMode.game.sendAlertToAll("宗教裁判所结束!")
        }
        // 奥运会结束
        if (gameMode.olympicsPosition == player.position) {
            gameMode.olympicsPosition = -1
            gameMode.report.info("奥运会结束!")
            gameMode.game.sendAlertToAll("奥运会结束!")
        }

        val param = this.getParam<RoundParam>(player)
        when {
            player.resigned -> return false
            gameMode.game.config.mode == TTAMode.SIMPLE -> // 简单模式跳过政治行动阶段
                param.currentStep = RoundStep.NORMAL
            gameMode.round == 2 -> // 第二回合无政治行动阶段
                param.currentStep = RoundStep.NORMAL
            else -> param.currentStep = RoundStep.POLITICAL
        }
        return true
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建回合参数
        gameMode.game.players.forEach { this.setParam(it.position, RoundParam(gameMode, this, it)) }
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        val param = this.getParam<RoundParam>(player.position)
        // 发送当前阶段
        res.public("currentStep", param.currentStep)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val param = this.getParam<RoundParam>(action.getPlayer<TTAPlayer>().position)
        when (param.currentStep) {
            RoundStep.POLITICAL // 政治行动阶段
            -> this.politicalAction.execute(action)
            RoundStep.NORMAL // 内政行动阶段
            -> this.doRoundAction(action)
            else -> throw BoardGameException("阶段错误,不能进行行动!")
        }
    }

    /**
     * 执行玩家行动阶段
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doRoundAction(action: GameAction) {
        when (action.getAsString("subact")) {
            TTACmdString.ACTION_PLAY_CARD -> // 出牌
                this.playCard(action)
            TTACmdString.ACTION_TAKE_CARD -> // 拿牌
                this.takeCard(action)
            TTACmdString.ACTION_POPULATION -> // 扩张人口
                this.increasePopulation(action)
            TTACmdString.REQUEST_BUILD -> // 请求建造界面
                this.requestBuild(action)
            TTACmdString.REQUEST_UPGRADE -> // 请求升级的界面
                this.requestUpgrade(action)
            TTACmdString.REQUEST_DESTORY -> // 请求摧毁建筑的界面
                this.requestDestory(action)
            TTACmdString.REQUEST_COPY_TAC -> // 新版请求拷贝阵型
                this.requestCopyTactics(action)
            TTACmdString.ACTION_ACTIVE_CARD -> // 使用卡牌能力
                this.activeCard(action)
            TTACmdString.ACTION_PASS -> // 结束内政阶段
                this.processEndAction(action)
            TTACmdString.ACTION_BUILD -> this.build(action)
            TTACmdString.ACTION_UPGRADE -> this.upgrade(action)
            TTACmdString.ACTION_DESTORY -> //摧毁
                this.destroy(action)
            "DEFAULT" -> this.doDefault(action)
            else -> throw BoardGameException("无效的指令!")
        }
    }

    private fun build(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player.position)
        val cardId = action.getAsString("card")
        val card = player.getPlayedCard(cardId)
        val executor = TTAExecutorFactory.createBuildExecutor(param, card)
        executor.execute()
    }

    private fun upgrade(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player.position)
        val fromCardId = action.getAsString("fromCard")
        val toCardId = action.getAsString("toCard")
        val fromCard = player.getPlayedCard(fromCardId) as TechCard
        val toCard = player.getPlayedCard(toCardId) as TechCard
        val executor = TTAUpgradeExecutor(param, fromCard, toCard)
        executor.execute()
    }

    private fun destroy(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player.position)
        val cardId = action.getAsString("card")
        val card = player.getPlayedCard(cardId)
        val executor = TTADestroyExecutor(param, card as TechCard)
        executor.execute()
    }

    private fun doDefault(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player.position)
        val cardId = action.getAsString("cardId")
        val card = player.getPlayedCard(cardId, true)
        val executor = TTACardDefaultExecutor(param, card)
        executor.execute()
    }


    /**
     * 结束行动阶段
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun endActionPhase(player: TTAPlayer) {
        val param = this.getParam<RoundParam>(player.position)
        gameMode.report.playerEndAction(player)
        if (gameMode.game.isVersion2 && param.needDiscardMilitary) {
            // 新版,如果玩家的军事手牌超过上限,则需要进行弃牌操作
            TTADiscardHandLimitExecutor(param).execute()
        }
        TTAEndTurnExecutor(param).execute()
        param.currentStep = RoundStep.NONE
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_ROUND

    /**
     * 玩家扩张人口
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun increasePopulation(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player)
        TTAIncreasePopExecutor(param).execute()
    }

    @Throws(BoardGameException::class)
    override fun onPlayerTurn(player: TTAPlayer) {
        super.onPlayerTurn(player)
        val game = gameMode.game
        val param = this.getParam<RoundParam>(player)

        // 设置当前玩家
        gameMode.game.setCurrentPlayer(player)
        // 牌列执行补牌的行动
        if (gameMode.round == 2 && player === game.startPlayer) {
            // 如果是第2回合起始玩家,则不进行补牌的动作,因为在1回合结束时已经补过了
            game.sendCardRowReport()
        } else {
            // 弃牌并补牌
            param.regroupCardRow(gameMode.doregroup)
            // 补牌完成后,如果游戏结束,并且当前玩家是起始玩家,则这是最后一个回合
            if (gameMode.gameOver && player === gameMode.game.startPlayer) {
                gameMode.finalRound = true
                gameMode.report.gameOverWarning()
                // 向所有玩家发送游戏即将结束的警告
                game.sendAlertToAll("游戏即将结束!")
            }
        }
        // 第二回合没有政治阶段，所以...
        gameMode.doregroup = gameMode.game.config.mode == TTAMode.SIMPLE || gameMode.round == 2
        // 重置临时资源和卡牌能力
        player.tempResManager.resetTemplateResource()
        player.abilityManager.getAbility(CivilAbilityType.PA_BONDICA)?.let {
            if (!player.params.getBoolean(CivilAbilityType.PA_BONDICA)) {
                TTABondicaExecutor(param).execute()
            }
        }
        player.resetRoundFlags()
        gameMode.report.playerRoundStart(player)
        // 重置政治阶段参数
        this.politicalAction.shouldPass = false
        this.politicalAction.eventCardPlayed = false

        // 检查玩家下回合中临时的属性调整值
        if (!player.roundTempParam.isEmpty) {
            // 计划发展
            player.roundTempParam.get<List<TTACard>>(EventType.CHOOSE_EVENT)?.let { events ->
                val executor = ChooseEventExecutor(param, events)
                executor.execute()
            }
            // 调整CA(老版叛乱)
            val num = player.roundTempParam.getInteger(CivilizationProperty.CIVIL_ACTION)
            if (num != 0) gameMode.game.playerAdjustCivilAction(player, num)
            // 是否跳过政治阶段
            val passed = player.roundTempParam.getString(RoundStep.POLITICAL)
            if (TTACmdString.POLITICAL_PASS == passed) {
                this.politicalAction.shouldPass = true
                gameMode.game.sendAlert(player, "你必须放弃政治阶段!")
            }
            // 弗莱明
            val target = player.roundTempParam.get<TTAPlayer>(ActiveAbilityType.VIEW_MILITARY_HAND)
            player.params.setGameParameter(ActiveAbilityType.VIEW_MILITARY_HAND, target)

            // 清空
            player.roundTempParam.clear()
        }

        // 新版当前阵型要移动到公共阵型区
        if (game.isVersion2) {
            if (gameMode.cardBoard.publicTactics(player.tactics)) {
                gameMode.report.publicTactics(player, player.tactics!!)
            }
        }

        // 向房间内玩家发送基本信息
        game.sendBaseInfo(null)

        // 新版贞德技能,向当前玩家发送即将触发的事件牌信息
        if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_VIEW_TOP_EVENT)) {
            game.sendTopEvent(player)
        }

        // 最后,检查玩家是否需要结算战争牌,如果有,则解决战争
        val war = player.war ?: return
        TTAWarCardExecutor(this.getParam(player), war).execute()
        gameMode.game.removeOvertimeCard(war)

    }

    /**
     * 玩家内政行动阶段从手牌中出牌
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun playCard(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val cardId = action.getAsString("cardId")
        val param = this.getParam<RoundParam>(player.position)
        val card = player.getCard(cardId)
        TTAExecutorFactory.createPlayCardExecutor(param, card).execute()
    }

    @Throws(BoardGameException::class)
    private fun processEndAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player)
        val executor = TTAEndActionExecutor(param).also(TTAEndActionExecutor::execute)
        if (executor.endPhase) this.endActionPhase(player)
    }

    /**
     * 玩家请求建造界面
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun requestBuild(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player.position)
        TTARequestBuildExecutor(param).execute()
    }

    /**
     * 玩家请求拷贝阵型的界面
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun requestCopyTactics(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player.position)
        TTACopyTacticsExecutor(param).execute()
    }

    /**
     * 玩家请求摧毁建筑的界面
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun requestDestory(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player.position)
        TTARequestDestroyExecutor(param).execute()
    }

    /**
     * 玩家请求升级的界面
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun requestUpgrade(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player.position)
        TTARequestUpgradeExecutor(param).execute()
    }

    @Throws(BoardGameException::class)
    override fun sendReconnectResponse(player: TTAPlayer) {
        super.sendReconnectResponse(player)
        if (this.listeningPlayer === player) {
            val param = this.getParam<RoundParam>(player)
            // 新版贞德技能,向当前玩家发送即将触发的事件牌信息
            if (param.currentStep == RoundStep.POLITICAL && !this.politicalAction.eventCardPlayed && player.abilityManager.hasAbilitiy(CivilAbilityType.PA_VIEW_TOP_EVENT)) {
                gameMode.game.sendTopEvent(this.listeningPlayer)
            }
        }
    }

    override fun sendStartListenCommand(player: TTAPlayer, receiver: TTAPlayer?) {
        super.sendStartListenCommand(player, receiver)
        // 发送玩家可激活的卡牌列表
        val param = this.getParam<RoundParam>(player.position)
        gameMode.game.sendPlayerActivableCards(param.currentStep, player)
    }

    /**
     * 玩家从卡牌序列中拿牌
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun takeCard(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val cardId = action.getAsString("cardId")
        val param = this.getParam<RoundParam>(player.position)
        val executor = TTATakeCardExecutor(param, cardId)
        executor.execute()
    }

    /**
     * 政治行动处理对象
     * @author F14eagle
     */
    inner class PoliticalAction {
        var shouldPass = false
        var eventCardPlayed = false

        @Throws(BoardGameException::class)
        fun endPoliticalPhase(player: TTAPlayer, forceEnd: Boolean) {
            val param = this@TTARoundListener.getParam<RoundParam>(player.position)
            ++param.politicalAction
            // 有2次政治行动能力还未发动,若不是点结束,这是第1次政治行动结束
            if (!forceEnd && player.checkAbility(CivilAbilityType.PA_DOUBLE_POLITICAL)) {
                if (param.politicalAction == 1) {
                    // 第一次政治行动结束
                    gameMode.game.sendAlert(player, "你可以额外执行一次政治行动!")
                    return
                } else {
                    // 第二次政治行动结束
                    if (player.abilityManager.getAbility(CivilAbilityType.PA_DOUBLE_POLITICAL)!!.limit > 0) {
                        player.params.setGameParameter(CivilAbilityType.PA_DOUBLE_POLITICAL, true)
                    } else {
                        player.params.setRoundParameter(CivilAbilityType.PA_DOUBLE_POLITICAL, true)
                    }
                }
            }
            if (!gameMode.game.isVersion2 && param.needDiscardMilitary) {
                // 老版,如果玩家的军事手牌超过上限,则需要进行弃牌操作
                TTADiscardHandLimitExecutor(param).execute()
            }
            // 结束政治行动阶段
            param.currentStep = RoundStep.NORMAL
            gameMode.doregroup = true
            // 发送玩家结束政治行动的信息
            gameMode.game.sendPlayerPoliticalEndResponse(player, param.currentStep)
            gameMode.report.playerEndPoliticalPhase(player)
            // 发送玩家可激活的卡牌列表
            gameMode.game.sendPlayerActivableCards(param.currentStep, player)
        }

        /**
         * 处理行动代码
         * @param action
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun execute(action: GameAction) {
            val player = action.getPlayer<TTAPlayer>()
            val subact = action.getAsString("subact")
            val param = this@TTARoundListener.getParam<RoundParam>(player.position)
            // 检查当前阶段是否是政治行动阶段
            param.checkPoliticalPhase()
            // 如果玩家不执行政治行动阶段
            when (subact) {
                TTACmdString.POLITICAL_PASS -> // 结束政治行动
                    this.endPoliticalPhase(player, true)
                TTACmdString.RESIGN -> {
                    // 体面退出游戏
                    gameMode.game.playerResign(player)
                    param.currentStep = RoundStep.RESIGNED
                    this@TTARoundListener.setPlayerResponsed(player)
                }
                else -> {
                    CheckUtils.check(this.shouldPass, "你必须放弃政治阶段!")
                    when (subact) {
                        TTACmdString.ACTION_PLAY_CARD -> // 玩家打出手牌
                            this.playCard(action)
                        TTACmdString.ACTION_ACTIVE_CARD -> // 使用卡牌能力
                            this@TTARoundListener.activeCard(action)
                        TTACmdString.REQUEST_BREAK_PACT -> // 请求废除条约
                            this.breakPack(action)
                        else -> throw BoardGameException("无效的指令!")
                    }
                }
            }
        }

        /**
         * 玩家从手牌中出黑牌
         * @param action
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        private fun playCard(action: GameAction) {
            val player = action.getPlayer<TTAPlayer>()
            val param = this@TTARoundListener.getParam<RoundParam>(player)
            val cardId = action.getAsString("cardId")
            val card = player.getCard(cardId)
            gameMode.game.beforePlayerPlayEvent(player, card)
            val executor = TTAExecutorFactory.createPoliticalExecutor(param, card)// ?: throw BoardGameException("不能打出这张牌!")
            if (card.cardType == CardType.EVENT) this.eventCardPlayed = true
            executor.execute()
            if (executor.finished) {
                // 如果处理完毕结束政治阶段
                this.endPoliticalPhase(player, false)
            }
        }

        /**
         * 玩家请求废除条约
         * @param action
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        private fun breakPack(action: GameAction) {
            val player = action.getPlayer<TTAPlayer>()
            val param = this@TTARoundListener.getParam<RoundParam>(player)
            val executor = TTARequestBreakPactExecutor(param)
            executor.execute()
            if (executor.finished) {
                this.endPoliticalPhase(player, false)
            }
        }
    }
}
