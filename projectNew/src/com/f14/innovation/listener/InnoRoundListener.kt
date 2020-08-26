package com.f14.innovation.listener

import com.f14.F14bg.network.CmdFactory
import com.f14.F14bg.utils.SystemUtil
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.consts.ConditionResult
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ListenerType
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.command.InnoCommand
import com.f14.innovation.command.InnoCommandList
import com.f14.innovation.consts.InnoActiveType
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoConsts
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.utils.InnoUtils


/**
 * Innovation的回合监听器
 * @author F14eagle
 */
class InnoRoundListener(gameMode: InnoGameMode, startPlayer: InnoPlayer) : InnoOrderListener(gameMode, startPlayer, ListenerType.NORMAL) {

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建回合参数
        for (player in gameMode.game.players) {
            val param = RoundParam(gameMode, player)
            // 如果是起始玩家,则只有一个行动
            if (player.firstAction) {
                param.ap = 1
                player.firstAction = false
            }
            this.setParam(player, param)
        }
    }

    override fun createStartListenCommand(player: InnoPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        val param = this.getParam<RoundParam>(player.position)
        // 发送当前的行动点数
        res.public("ap", param.ap)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        when (action.getAsString("subact")) {
            "DRAW_CARD" -> this.drawCard(action)
            "MELD_CARD" -> this.meldCard(action)
            "ACHIEVE" -> this.drawAchieveCard(action)
            "DOGMA" -> this.dogmaCard(action)
            else -> throw BoardGameException("无效的指令!")
        }
    }

    /**
     * 使用卡牌的能力
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun dogmaCard(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val colorString = action.getAsString("color")
        val color = InnoColor.valueOf(colorString)
        val card = player.getTopCard(color) ?: throw BoardGameException("没有找到该颜色对应的置顶牌!")
        // 发送dogma的效果
        gameMode.game.playerDogmaCard(player, card)
        // 如果存在,则处理该牌的能力
        if (card.abilityGroups.isNotEmpty()) {
            val commandList = this.currentCommandList
            // 清理命令列表以供使用
            commandList.reset(card)
            commandList.setDogmaCommandList()
            // 处理命令列表
            this.processCommandList(commandList)
        }
        this.refreshAp(player)
    }

    /**
     * 拿成就牌
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun drawAchieveCard(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val cardId = action.getAsString("cardId")
        val card = gameMode.achieveManager.achieveCards.getCard(cardId)
        val maxLevel = player.maxLevel
        if (card.level > maxLevel) {
            throw BoardGameException("你只能拿" + maxLevel + "级的成就牌!")
        }
        if (player.score < card.level * 5) {
            throw BoardGameException("你的分数不够拿这张成就牌!")
        }
        gameMode.game.playerDrawAchieveCard(player, card)
        this.refreshAp(player)
    }

    /**
     * 摸牌
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun drawCard(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        var level = action.getAsInt("level")
        InnoUtils.checkLevel(level)

        val isEmpty = gameMode.drawDecks.getCardDeck(level).empty
        if (isEmpty) {
            level += 1
        }

        val maxLevel = this.getMaxAvailableLevel(player)
        if (level != maxLevel && level <= InnoConsts.MAX_LEVEL) {
            // 调试模式下不限制摸牌等级
            if (!SystemUtil.isDebugMode) {
                throw BoardGameException("你只能摸" + maxLevel + "级的牌!")
            }
        }

        gameMode.game.playerDrawCard(player, level, 1)
        this.refreshAp(player)
    }

    /**
     * 取得当前行动玩家对应的命令列表

     * @return
     */
    private val currentCommandList: InnoCommandList
        get() {
            val p = this.getParam<RoundParam>(this.listeningPlayer!!)
            return p.commandList
        }

    /**
     * 取得玩家可以摸的最高等级的牌
     * @param player
     * @return
     */
    private fun getMaxAvailableLevel(player: InnoPlayer): Int {
        var maxLevel = player.maxLevel
        while (maxLevel < InnoConsts.MAX_LEVEL && gameMode.drawDecks.getCardDeck(maxLevel).empty) {
            maxLevel += 1
        }
        return maxLevel
    }

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_ROUND

    /**
     * 合并手牌
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun meldCard(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val cardId = action.getAsString("cardId")
        val card = player.hands.getCard(cardId)
        gameMode.game.playerMeldHandCard(player, card)
        this.refreshAp(player)
    }

    @Throws(BoardGameException::class)
    override fun onPlayerTurn(player: InnoPlayer) {
        super.onPlayerTurn(player)
        // 玩家回合开始时,需要清空所有玩家的回合垫底/计分牌数
        for (p in gameMode.game.players) {
            p.clearRoundCount()
        }
    }

    /**
     * 处理命令列表
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun processCommandList(commandList: InnoCommandList) {
        val player = listeningPlayer!!
        while (!commandList.isEmpty()) {
            var cmd = commandList.pop()
            while (cmd != null) {
                gameMode.game.processInnoCommand(cmd, commandList)
                cmd = commandList.pop()
            }
            if (!commandList.isNoDogmaActived) {
                val cr = if (commandList.isOtherPlayerDemanded(player)) ConditionResult.TRUE
                else ConditionResult.ELSE
                val abilityGroup = commandList.mainCard.getDogmaResultAbilitiyGroup(cr)
                if (abilityGroup != null) {
                    commandList.isNoDogmaActived = true
                    val card = commandList.mainCard
                    if (abilityGroup.activeType == InnoActiveType.NORMAL) {
                        gameMode.game.getPlayersByOrder(player).drop(1).filter(commandList::isSharedPlayer).mapTo(commandList) { InnoCommand(it, player, abilityGroup, card) }
                        commandList.add(InnoCommand(player, player, abilityGroup, card))
                    }
                    processCommandList(commandList)
                }
            }
        }
        if (!commandList.isAddCardDrawn && commandList.isOtherPlayerActived(player)) {
            commandList.isAddCardDrawn = true
            val level = player.maxLevel
            gameMode.game.playerDrawCard(player, level, 1)
        }
    }

    /**
     * 刷新行动点数
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun refreshAp(player: InnoPlayer) {
        val p = this.getParam<RoundParam>(player)
        p.ap -= 1
        this.sendRefreshApResponse(player)
        if (p.ap <= 0) {
            setPlayerResponsed(player)
        }
    }

    /**
     * 发送刷新玩家行动点数的消息
     * @param player
     */
    private fun sendRefreshApResponse(player: InnoPlayer) {
        val param = this.getParam<RoundParam>(player.position)
        // 发送当前的行动点数
        CmdFactory.createGameResponse(InnoGameCmd.GAME_CODE_REFRESH_AP, player.position).public("ap", param.ap).send(gameMode)
    }

    override fun sendStartListenCommand(player: InnoPlayer, receiver: InnoPlayer?) {
        super.sendStartListenCommand(player, receiver)
        this.sendRefreshApResponse(player)
    }

    /**
     * 玩家的回合参数
     * @author F14eagle
     */
    internal class RoundParam(gameMode: InnoGameMode, player: InnoPlayer) {
        val commandList: InnoCommandList = InnoCommandList(gameMode, player)
        /**
         * 默认有2个行动
         */
        var ap = 2

        @Throws(BoardGameException::class)
        fun checkAp() {
            if (ap <= 0) {
                throw BoardGameException("行动点已经用完了!")
            }
        }

    }

}
