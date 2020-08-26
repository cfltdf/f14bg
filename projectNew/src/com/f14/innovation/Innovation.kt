package com.f14.innovation

import com.f14.F14bg.network.CmdFactory
import com.f14.bg.BoardGame
import com.f14.bg.anim.AnimType
import com.f14.bg.consts.BgVersion
import com.f14.bg.consts.ConditionResult
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.innovation.anim.InnoAnimParamFactory
import com.f14.innovation.command.InnoCommand
import com.f14.innovation.command.InnoCommandList
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.*
import com.f14.innovation.executor.*
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoParamFactory
import com.f14.innovation.param.InnoResultParam
import net.sf.json.JSONObject
import java.util.*

class Innovation : BoardGame<InnoPlayer, InnoConfig, InnoReport>() {
    override val mode
        get() = gameMode
    lateinit var gameMode: InnoGameMode

    @Throws(BoardGameException::class)
    override fun createConfig(obj: JSONObject): InnoConfig {
        val config = InnoConfig()
        config.versions.add(BgVersion.BASE)
        // String versions = object.getString("versions");
        // if(!StringUtils.empty(versions)){
        // String[] vs = versions.split(",");
        // for(String v : vs){
        // config.versions.add(v);
        // }
        // }
        val teamMatch = obj.getBoolean("teamMatch")
        config.isTeamMatch = teamMatch
        val mode = obj.getString("mode")
        config.mode = mode
        // 组队战时,可以选择是否随机安排座位
        config.randomSeat = teamMatch && "RANDOM" == mode
        // 非组队战时,总是随机安排座位
        return config
    }


    /**
     * 取得执行行动的玩家
     * @param param
     * @param player
     * @param commandList
     * @return
     */
    private fun getTrigPlayer(param: InnoInitParam?, player: InnoPlayer, commandList: InnoCommandList): InnoPlayer = if (param == null || param.trigPlayer != InnoPlayerTargetType.MAIN_PLAYER) player else commandList.mainPlayer

    override fun initConfig() {
        val config = InnoConfig()
        config.versions.add(BgVersion.BASE)
        config.mode = "RANDOM"
        config.isTeamMatch = true
        this.config = config
    }

    override fun initConst() {

    }

    override fun initPlayerTeams() {
        if (this.isTeamMatch) {
            // 13 vs 24
            this.players.forEach { it.team = it.position % 2 }
        } else {
            super.initPlayerTeams()
        }

    }

    override fun initReport() {
        this.report = InnoReport(this)
    }

    override // 必须要4人游戏才会是组队赛
    val isTeamMatch: Boolean
        get() = this.players.size == 4 && super.isTeamMatch

    /**
     * 玩家得到手牌
     * @param player
     * @param result 存放得到的手牌及其来源
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerAddHandCard(player: InnoPlayer, result: InnoResultParam) {
        val param = InnoParamFactory.createInitParam()
        InnoAddHandExecutor(this.gameMode, player, param, result, null, null).execute()
    }

    /**
     * 玩家得到计分牌
     * @param player
     * @param result 存放计分牌的来源
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerAddScoreCard(player: InnoPlayer, result: InnoResultParam) {
        this.playerAddScoreCard(player, result, false)
    }

    /**
     * 玩家得到计分牌
     * @param player
     * @param result       存放计分牌的来源
     * @param checkAchieve 是否检查成就
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerAddScoreCard(player: InnoPlayer, result: InnoResultParam, checkAchieve: Boolean) {
        val param = InnoParamFactory.createInitParam()
        param.isCheckAchieve = checkAchieve
        InnoAddScoreExecutor(this.gameMode, player, param, result, null, null).execute()
    }

    /**
     * 玩家得到特殊成就牌
     * @param player
     */
    fun playerAddSpecialAchieveCard(player: InnoPlayer, card: InnoCard) {
        player.addAchieveCard(card)
        this.gameMode.achieveManager.removeSpecialAchieve(card)
        this.sendRemoveSpecialAchieveCardResponse(card)
        this.sendAnimationResponse(InnoAnimParamFactory.createDrawSpecialAchieveCardParam(player, card))
        this.sendPlayerAddAchieveCardResponse(player, card, null)
        this.report.playerDrawAchieveCard(player, card)
        // 检查是否达成成就胜利的条件
        this.gameMode.checkAchieveVictory(player)
    }

    /**
     * 发送玩家触发卡牌效果
     * @param player
     * @param card
     */
    fun playerDogmaCard(player: InnoPlayer, card: InnoCard) {
        this.sendAnimationResponse(InnoAnimParamFactory.createDogmaCardParam(player, card))
        this.report.playerDogmaCard(player, card)
    }

    /**
     * 玩家拿成就牌
     * @param player
     */
    fun playerDrawAchieveCard(player: InnoPlayer, card: InnoCard) {
        player.addAchieveCard(card)
        this.gameMode.achieveManager.achieveCards.removeCard(card)
        this.sendRemoveAchieveCardResponse(card)
        this.sendAnimationResponse(InnoAnimParamFactory.createDrawAchieveCardParam(player, card))
        this.sendPlayerAddAchieveCardResponse(player, card, null)
        this.report.playerDrawAchieveCard(player, card)
        // 检查是否达成成就胜利的条件
        this.gameMode.checkAchieveVictory(player)
    }

    /**
     * 玩家摸牌融合
     * @param player
     * @param level
     * @param num
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerDrawAndMeldCard(player: InnoPlayer, level: Int, num: Int) {
        val initParam = InnoParamFactory.createInitParam(1, level)
        repeat(num) {
            val resultParam = InnoResultParam()
            InnoDrawCardExecutor(this.gameMode, player, initParam, resultParam, null, null).execute()
            InnoMeldExecutor(this.gameMode, player, initParam, resultParam, null, null).execute()
        }
    }

    /**
     * 玩家摸牌计分
     * @param player
     * @param level
     * @param num
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerDrawAndScoreCard(player: InnoPlayer, level: Int, num: Int) {
        val initParam = InnoParamFactory.createInitParam(1, level)
        // 摸牌计分的都算回合计分牌
        initParam.isCheckAchieve = true
        repeat(num) {
            val resultParam = InnoResultParam()
            InnoDrawCardExecutor(this.gameMode, player, initParam, resultParam, null, null).execute()
            InnoAddScoreExecutor(this.gameMode, player, initParam, resultParam, null, null).execute()
        }
    }

    /**
     * 玩家摸牌追加
     * @param player
     * @param level
     * @param num
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerDrawAndTuckCard(player: InnoPlayer, level: Int, num: Int) {
        val initParam = InnoParamFactory.createInitParam(1, level)
        // 摸牌垫底的都算回合垫底牌
        initParam.isCheckAchieve = true
        repeat(num) {
            val resultParam = InnoResultParam()
            InnoDrawCardExecutor(this.gameMode, player, initParam, resultParam, null, null).execute()
            InnoTuckExecutor(this.gameMode, player, initParam, resultParam, null, null).execute()
        }
    }

    /**
     * 玩家摸牌(暗抽)
     * @param player
     * @param level
     * @param num
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun playerDrawCard(player: InnoPlayer, level: Int, num: Int): List<InnoCard> {
        return this.playerDrawCard(player, level, num, false)
    }

    /**
     * 玩家摸牌
     * @param player
     * @param level
     * @param num
     * @param reveal 是否明抽
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun playerDrawCard(player: InnoPlayer, level: Int, num: Int, reveal: Boolean): List<InnoCard> {
        val cards = ArrayList<InnoCard>()
        val animType = if (reveal) AnimType.REVEAL else AnimType.DIRECT
        val param = InnoParamFactory.createInitParam(1, level)
        param.animType = animType
        // 每次摸一张牌,通过循环实现摸多张牌
        repeat(num) {
            val result = InnoResultParam()
            InnoDrawCardExecutor(this.gameMode, player, param, result, null, null).execute()
            InnoAddHandExecutor(this.gameMode, player, param, result, null, null).execute()
            cards.addAll(result.cards.cards)
        }
        return cards
    }

    /**
     * 玩家摸牌
     * @param player
     * @param level
     * @param num
     * @param reveal 是否明抽
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun playerDrawCardAction(player: InnoPlayer, level: Int, num: Int, reveal: Boolean): InnoResultParam {
        val animType = if (reveal) AnimType.REVEAL else AnimType.DIRECT
        val param = InnoParamFactory.createInitParam(1, level)
        param.animType = animType
        val result = InnoResultParam()
        // 每次摸一张牌,通过循环实现摸多张牌
        repeat(num) {
            InnoDrawCardExecutor(this.gameMode, player, param, result, null, null).execute()
        }
        return result
    }

    /**
     * 玩家合并牌
     * @param player
     * @param result
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerMeldCard(player: InnoPlayer, result: InnoResultParam) {
        val param = InnoParamFactory.createInitParam()
        InnoMeldExecutor(this.gameMode, player, param, result, null, null).execute()
    }

    // /**
    // * 发送玩家得分的信息
    // *
    // * @param player
    // * @param receiver
    // */
    // public void sendPlayerScoreInfoResponse(InnoPlayer player, Player
    // receiver){
    // BgResponse res =
    // CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_SCORE_INFO,
    // player.getPosition());
    // res.public("scoreInfo", player.getScores().toMap());
    // res.public("scoreNum", player.getScore());
    // this.sendResponse(receiver, res);
    // }

    /**
     * 从手上合并牌
     * @param player
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerMeldHandCard(player: InnoPlayer, card: InnoCard) {
        val param = InnoParamFactory.createInitParam()
        val result = InnoResultParam()
        result.addCard(card)
        InnoRemoveHandExecutor(this.gameMode, player, param, result, null, null).execute()
        InnoMeldExecutor(this.gameMode, player, param, result, null, null).execute()
    }

    /**
     * 玩家失去手牌
     * @param player
     * @param card
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun playerRemoveHandCard(player: InnoPlayer, card: InnoCard): InnoResultParam {
        val param = InnoParamFactory.createInitParam()
        val result = InnoResultParam()
        result.addCard(card)
        InnoRemoveHandExecutor(this.gameMode, player, param, result, null, null).execute()
        return result
    }

    /**
     * 玩家失去分数
     * @param player
     * @param card
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun playerRemoveScoreCard(player: InnoPlayer, card: InnoCard): InnoResultParam {
        val param = InnoParamFactory.createInitParam()
        val result = InnoResultParam()
        result.addCard(card)
        InnoRemoveScoreExecutor(this.gameMode, player, param, result, null, null).execute()
        return result
    }

    /**
     * 玩家失去牌堆中的牌
     * @param player
     * @param card
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun playerRemoveStackCard(player: InnoPlayer, card: InnoCard): InnoResultParam {
        val param = InnoParamFactory.createInitParam()
        val result = InnoResultParam()
        result.addCard(card)
        InnoRemoveStackCardExecutor(this.gameMode, player, param, result, null, null).execute()
        return result
    }

    /**
     * 玩家失去置顶牌
     * @param player
     * @param color
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun playerRemoveTopCard(player: InnoPlayer, color: InnoColor): InnoResultParam {
        val param = InnoParamFactory.createInitParam()
        param.color = color
        val result = InnoResultParam()
        InnoRemoveTopCardExecutor(this.gameMode, player, param, result, null, null).execute()
        return result
    }

    /**
     * 玩家退回手牌
     * @param player
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerReturnCard(player: InnoPlayer, card: InnoCard) {
        val param = InnoParamFactory.createInitParam()
        val result = InnoResultParam()
        result.addCard(card)
        InnoRemoveHandExecutor(this.gameMode, player, param, result, null, null).execute()
        InnoReturnCardExecutor(this.gameMode, player, param, result, null, null).execute()
    }

    /**
     * 玩家归还牌
     * @param player
     * @param result
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerReturnCard(player: InnoPlayer, result: InnoResultParam) {
        val param = InnoParamFactory.createInitParam()
        InnoReturnCardExecutor(this.gameMode, player, param, result, null, null).execute()
    }

    /**
     * 玩家展示手牌
     * @param player
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerRevealHands(player: InnoPlayer, card: InnoCard) {
        val initParam = InnoParamFactory.createInitParam()
        val resultParam = InnoResultParam()
        resultParam.addCard(card)
        InnoRevealHandExecutor(this.gameMode, player, initParam, resultParam, null, null).execute()
    }

    /**
     * 玩家展开牌堆
     * @param player
     * @param color
     * @param splayDirection
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerSplayStack(player: InnoPlayer, color: InnoColor, splayDirection: InnoSplayDirection) {
        val initParam = InnoParamFactory.createInitParam()
        initParam.color = color
        initParam.splayDirection = splayDirection
        val resultParam = InnoResultParam()
        InnoSplayExecutor(this.gameMode, player, initParam, resultParam, null, null).execute()
    }

    /**
     * 玩家追加牌
     * @param player
     * @param result
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerTuckCard(player: InnoPlayer, result: InnoResultParam) {
        this.playerTuckCard(player, result, false)
    }

    /**
     * 玩家追加牌
     * @param player
     * @param result
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerTuckCard(player: InnoPlayer, result: InnoResultParam, checkAchieve: Boolean) {
        val param = InnoParamFactory.createInitParam()
        param.isCheckAchieve = checkAchieve
        InnoTuckExecutor(this.gameMode, player, param, result, null, null).execute()
    }

    /**
     * 从手上追加牌
     * @param player
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerTuckHandCard(player: InnoPlayer, card: InnoCard) {
        val param = InnoParamFactory.createInitParam()
        val result = InnoResultParam()
        result.addCard(card)
        InnoRemoveHandExecutor(this.gameMode, player, param, result, null, null).execute()
        InnoTuckExecutor(this.gameMode, player, param, result, null, null).execute()
    }

    /**
     * 处理AbilityGroup
     * @param group
     * @param player
     * @param commandList
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun processAbilityGroup(group: InnoAbilityGroup, player: InnoPlayer, commandList: InnoCommandList, origResult: InnoResultParam?) {
        val abilities = group.abilities
        if (abilities.isNotEmpty()) {
            // 按照设定的循环次数执行效果
            repeat(group.repeat) {
                val result = if (origResult == null) InnoResultParam() else InnoResultParam(origResult)
                for (ability in abilities) {
                    when (ability.abilityType) {
                        InnoAbilityType.EXECUTOR -> { // 行动执行器
                            val executor = InnoClassFactory.createExecutor(ability, gameMode, player, result, group)
                            executor.commandList = commandList
                            executor.execute()
                        }
                        InnoAbilityType.CHECKER -> { // 检查器
                            val checker = InnoClassFactory.createChecker(ability, gameMode, player, result)
                            checker.commandList = commandList
                            checker.execute()
                        }
                        InnoAbilityType.LISTENER -> { // 监听器
                            val trigPlayer = this.getTrigPlayer(ability.initParam, player, commandList)
                            val listener = InnoClassFactory.createListener(ability, gameMode, group, trigPlayer, result)
                            // 将该监听器插入到回合监听器中
                            listener.let(commandList::insertInterrupteListener)
                        }
                    }
                }
                // 如果执行过检查器,则需要按照检查器的执行结果执行相关的行动
                if (commandList.commandParam.isChecked) {
                    // 为空则取ELSE
                    val conditionResult = result.conditionResult ?: ConditionResult.ELSE
                    val conditionAbilityGroup = group.getConditionAbilityGroup(conditionResult)
                    // 取得AbilityGroup就执行
                    conditionAbilityGroup?.let { this.processAbilityGroup(it, player, commandList, result) }
                }
            }
        }
    }

    /**
     * 处理命令
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun processInnoCommand(cmd: InnoCommand, commandList: InnoCommandList) {
        // 重置指令的参数
        do {
            // 每次循环开始时,先重置参数值
            commandList.resetCommandParam(cmd.player)
            this.processAbilityGroup(cmd.abilityGroup, cmd.player, commandList, null)
        } while (commandList.commandParam.isSetActiveAgain) // 如果需要再次触发,则在执行一次AbilityGroup

    }

    /**
     * 发送成就牌堆的信息
     * @param receiver
     */
    fun sendAchievementInfo(receiver: InnoPlayer?) {
        CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_ACHIEVE_INFO, -1).public("normalCardIds", BgUtils.card2String(this.gameMode.achieveManager.achieveCards.getCards())).public("specialCardIds", BgUtils.card2String(this.gameMode.achieveManager.specialAchieveCards.cards)).send(this, receiver)
    }

    /**
     * 发送摸牌堆的信息
     * @param receiver
     */
    fun sendDrawDeckInfo(receiver: InnoPlayer?) {
        CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_DRAW_DECK_INFO, -1).public("deckInfo", this.gameMode.drawDecks.toMap()).send(this, receiver)
    }

    @Throws(BoardGameException::class)
    override fun sendGameInfo(receiver: InnoPlayer?) {
        // 发送摸牌堆信息
        this.sendDrawDeckInfo(receiver)
        // 发送成就牌堆信息
        this.sendAchievementInfo(receiver)
    }

    @Throws(BoardGameException::class)
    override fun sendInitInfo(receiver: InnoPlayer?) = Unit

    /**
     * 发送玩家得到成就牌的信息
     * @param player
     * @param receiver
     */
    fun sendPlayerAddAchieveCardResponse(player: InnoPlayer, card: InnoCard, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_ADD_ACHIEVE, player.position)
        res.public("cardIds", card.id)
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家得到成就牌的信息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerAddAchieveCardsResponse(player: InnoPlayer, cards: List<InnoCard>, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_ADD_ACHIEVE, player.position)
        res.public("cardIds", BgUtils.card2String(cards))
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家得到手牌的信息
     * @param player
     * @param card
     * @param receiver
     */
    fun sendPlayerAddHandResponse(player: InnoPlayer, card: InnoCard, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_ADD_HANDS, player.position)
        res.public("handInfo", player.hands.toMap())
        res.public("handNum", player.hands.size())
        res.private("cardIds", card.id)
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家得到手牌的信息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerAddHandsResponse(player: InnoPlayer, cards: List<InnoCard>, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_ADD_HANDS, player.position)
        res.public("handInfo", player.hands.toMap())
        res.public("handNum", player.hands.size())
        res.private("cardIds", BgUtils.card2String(cards))
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家得到计分牌的信息
     * @param player
     * @param card
     * @param receiver
     */
    fun sendPlayerAddScoreResponse(player: InnoPlayer, card: InnoCard, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_ADD_SCORES, player.position)
        res.public("scoreInfo", player.scores.toMap())
        res.public("scoreNum", player.score)
        res.private("cardIds", card.id)
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家得到计分牌的信息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerAddScoresResponse(player: InnoPlayer, cards: List<InnoCard>, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_ADD_SCORES, player.position)
        res.public("scoreInfo", player.scores.toMap())
        res.public("scoreNum", player.score)
        res.private("cardIds", BgUtils.card2String(cards))
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家合并卡牌堆的信息
     * @param player
     * @param receiver
     */
    fun sendPlayerCardStackResponse(player: InnoPlayer, color: InnoColor, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_CARD_STACK, player.position)
        res.public("stacksInfo", player.getStackInfo(color))
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家所有卡牌堆的信息
     * @param player
     * @param receiver
     */
    fun sendPlayerCardStacksInfoResponse(player: InnoPlayer, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_CARD_STACK, player.position)
        res.public("stacksInfo", player.stacksInfo)
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家的总符号信息
     * @param player
     * @param receiver
     */
    fun sendPlayerIconsInfoResponse(player: InnoPlayer, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_ICON_INFO, player.position)
        res.public("iconsInfo", player.iconCounter.toMap())
        this.sendResponse(receiver, res)
    }

    @Throws(BoardGameException::class)
    override fun sendPlayerPlayingInfo(receiver: InnoPlayer?) {
        for (player in this.players) {
            // 发送手牌信息
            this.sendPlayerAddHandsResponse(player, player.hands.getCards(), receiver)
            // 发送得分信息
            this.sendPlayerAddScoresResponse(player, player.scores.getCards(), receiver)
            // 发送成就牌信息
            this.sendPlayerAddAchieveCardsResponse(player, player.achieveCards.cards, receiver)
            // 发送玩家所有打出牌堆的信息
            this.sendPlayerCardStacksInfoResponse(player, receiver)
            // 发送玩家的总符号数信息
            this.sendPlayerIconsInfoResponse(player, receiver)
        }
    }

    /**
     * 发送移除玩家选择中的计分牌的信息
     * @param player
     * @param card
     */
    fun sendPlayerRemoveChooseScoreCardResponse(player: InnoPlayer, card: InnoCard) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REMOVE_CHOOSE_SCORE_CARD, player.position)
        res.private("cardIds", card.id)
        this.sendResponse(player, res)
    }

    /**
     * 发送移除其他玩家选择中的计分牌的信息
     * @param player
     * @param target
     * @param cards
     */
    fun sendPlayerRemoveChooseScoreCardsResponse(player: InnoPlayer, target: InnoPlayer, cards: List<InnoCard>) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REMOVE_CHOOSE_SCORE_CARD, player.position)
        res.private("targetPosition", target.position)
        res.private("cardIds", BgUtils.card2String(cards))
        this.sendResponse(player, res)
    }

    /**
     * 发送移除玩家选择中的计分牌的信息
     * @param player
     * @param cards
     */
    fun sendPlayerRemoveChooseScoreCardsResponse(player: InnoPlayer, cards: List<InnoCard>) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REMOVE_CHOOSE_SCORE_CARD, player.position)
        res.private("cardIds", BgUtils.card2String(cards))
        this.sendResponse(player, res)
    }

    /**
     * 发送玩家失去手牌的信息
     * @param player
     * @param receiver
     */
    fun sendPlayerRemoveHandResponse(player: InnoPlayer, card: InnoCard, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REMOVE_HANDS, player.position)
        res.public("handInfo", player.hands.toMap())
        res.public("handNum", player.hands.size())
        res.private("cardIds", card.id)
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家失去手牌的信息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerRemoveHandsResponse(player: InnoPlayer, cards: List<InnoCard>, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REMOVE_HANDS, player.position)
        res.public("handInfo", player.hands.toMap())
        res.public("handNum", player.hands.size())
        res.private("cardIds", BgUtils.card2String(cards))
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家失去计分牌的信息
     * @param player
     * @param receiver
     */
    fun sendPlayerRemoveScoreResponse(player: InnoPlayer, card: InnoCard, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REMOVE_SCORES, player.position)
        res.public("scoreInfo", player.scores.toMap())
        res.public("scoreNum", player.score)
        res.private("cardIds", card.id)
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家失去计分牌的信息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerRemoveScoresResponse(player: InnoPlayer, cards: List<InnoCard>, receiver: InnoPlayer?) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REMOVE_SCORES, player.position)
        res.public("scoreInfo", player.scores.toMap())
        res.public("scoreNum", player.score)
        res.private("cardIds", BgUtils.card2String(cards))
        this.sendResponse(receiver, res)
    }

    /**
     * 发送移除玩家特定牌的信息
     * @param player
     * @param card
     */
    fun sendPlayerRemoveSpecificCardResponse(player: InnoPlayer, card: InnoCard) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REMOVE_SPECIFIC_CARD, player.position)
        res.private("cardIds", card.id)
        this.sendResponse(player, res)
    }

    /**
     * 发送移除玩家特定牌的信息
     * @param player
     * @param cards
     */
    fun sendPlayerRemoveSpecificCardsResponse(player: InnoPlayer, cards: List<InnoCard>) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REMOVE_SPECIFIC_CARD, player.position)
        res.private("cardIds", BgUtils.card2String(cards))
        this.sendResponse(player, res)
    }

    /**
     * 发送移除成就牌的信息
     * @param card
     */
    fun sendRemoveAchieveCardResponse(card: InnoCard) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REMOVE_ACHIEVE_CARD, -1)
        res.public("level", card.level)
        this.sendResponse(null, res)
    }

    /**
     * 发送移除特殊成就牌的信息
     * @param card
     */
    fun sendRemoveSpecialAchieveCardResponse(card: InnoCard) {
        val res = CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REMOVE_SPECIAL_ACHIEVE_CARD, -1)
        res.public("cardId", card.id)
        this.sendResponse(null, res)
    }

    @Throws(BoardGameException::class)
    override fun setupGame() {
        this.config.playerNumber = this.currentPlayerNumber
        this.gameMode = InnoGameMode(this)
    }

}
