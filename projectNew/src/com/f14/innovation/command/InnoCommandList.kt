package com.f14.innovation.command

import com.f14.bg.common.ParamSet
import com.f14.bg.consts.ConditionResult
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.consts.InnoActiveType
import com.f14.innovation.listener.InnoInterruptListener
import com.f14.innovation.param.InnoCommandParam
import com.f14.innovation.param.InnoResultParam
import java.util.*

class InnoCommandList(private val gameMode: InnoGameMode, val mainPlayer: InnoPlayer) : ArrayList<InnoCommand>() {
    /**
     * 是否摸过奖励的牌
     */
    var isAddCardDrawn = false
    /**
     * 是否执行过没人被要求时的效果
     */
    var isNoDogmaActived = false

    private var playerParam: MutableMap<InnoPlayer, ParamSet> = HashMap()

    val commandParam = InnoCommandParam()
    /**
     * 当前执行的玩家
     */
    lateinit var currentPlayer: InnoPlayer
    /**
     * 触发的卡牌
     */
    lateinit var mainCard: InnoCard

    val sharedPlayers: MutableList<InnoPlayer> = ArrayList()

    val demandPlayers: MutableList<InnoPlayer> = ArrayList()

    /**
     * 取得玩家的参数集
     * @param player
     * @return
     */
    fun getPlayerParamSet(player: InnoPlayer) = this.playerParam.computeIfAbsent(player) { ParamSet() }


    /**
     * 插入监听器
     * @param al
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun insertInterrupteListener(al: InnoInterruptListener) {
        var currListener: InnoInterruptListener? = al
        while (currListener != null) {
            al.commandList = this
            val param = gameMode.insertListener(currListener)
            val abilityGroups = currListener.resultParam.nextAbilityGroups
            val abilityGroup = currListener.abilityGroup
            if (abilityGroup != null) {
                abilityGroup.getConditionAbilityGroup(ConditionResult.THEN)?.let { g ->
                    abilityGroups[ConditionResult.THEN]?.let { p ->
                        val resultParam = InnoResultParam()
                        param.get<List<InnoCard>>("cards")?.let(resultParam::addCards)
                        gameMode.game.processAbilityGroup(g, p, this, resultParam)
                    }
                }
                arrayOf(ConditionResult.TRUE, ConditionResult.MAX, ConditionResult.ANYWAY, ConditionResult.HAVE).forEach {
                    abilityGroup.getConditionAbilityGroup(it)?.let { g ->
                        abilityGroups[it]?.let { p ->
                            gameMode.game.processAbilityGroup(g, p, this, null)
                        }
                    }
                }
            }
            currListener = param.get<InnoInterruptListener>("next")
        }
    }

    /**
     * 判断是否有指定玩家以外的玩家,激活过能力
     * @param player
     * @return
     */
    fun isOtherPlayerActived(player: InnoPlayer): Boolean =// 只要有任意敌对玩家激活过,就返回true
            this.playerParam.keys.filter { it !== player && gameMode.isEnemy(it, player) }.any(this::isPlayerActived)

    /**
     * 判断是否有指定玩家以外的玩家,被要求执行过能力
     * @param player
     * @return
     */
    fun isOtherPlayerDemanded(player: InnoPlayer) =// 只要有任意玩家激活过,就返回true
            this.playerParam.keys.filterNot(player::equals).any(this::isPlayerDemanded)

    /**
     * 检查玩家是否激活过能力
     * @param player
     * @return
     */
    fun isPlayerActived(player: InnoPlayer) = this.getPlayerParamSet(player).getBoolean(PLAYER_ACTIVED)

    /**
     * 检查玩家是否被要求执行过能力
     * @param player
     * @return
     */
    fun isPlayerDemanded(player: InnoPlayer) = this.getPlayerParamSet(player).getBoolean(PLAYER_DEMANDED)

    /**
     * 判断玩家是否在可分享的玩家列表中
     * @param player
     * @return
     */
    fun isSharedPlayer(player: InnoPlayer) = this.sharedPlayers.contains(player)

    /**
     * 返回第一个对象
     * @return
     */
    fun pop(): InnoCommand? = when {
        this.isEmpty() -> null
        else -> this.removeAt(0)
    }

    /**
     * 重置命令列表
     */
    fun reset(card: InnoCard) {
        this.clear()
        this.mainCard = card
        this.playerParam.clear()
        this.isAddCardDrawn = false
        this.isNoDogmaActived = false
        this.sharedPlayers.clear()
        this.demandPlayers.clear()
    }

    /**
     * 重置指令参数
     */
    fun resetCommandParam(player: InnoPlayer) {
        this.commandParam.reset()
        this.currentPlayer = player
    }

    /**
     * 设置dogma效果列表
     */
    fun setDogmaCommandList() {
        val card = this.mainCard
        val player = this.mainPlayer

        val mainIcon = card.mainIcon!!
        val playerIcon = player.getIconCount(mainIcon)
        // 设置所有玩家所在的行动列表
        val groups = gameMode.game.players.groupBy {
            when {
                it.getIconCount(mainIcon) >= playerIcon -> InnoActiveType.NORMAL
                gameMode.isEnemy(player, it) -> InnoActiveType.DEMAND
                else -> null
            }
        }
        this.sharedPlayers.addAll(groups[InnoActiveType.NORMAL] ?: emptyList())
        this.demandPlayers.addAll(groups[InnoActiveType.DEMAND] ?: emptyList())
//        this.sharedPlayers.addAll(gameMode.game.players.filter { groups.keyOf(it) == InnoActiveType.NORMAL })
//        this.demandPlayers.addAll(gameMode.game.players.filter { groups.keyOf(it) == InnoActiveType.DEMAND })
        // 按顺序结算每一条效果
        for (group in card.abilityGroups) {
            // 取得下一顺位的玩家,能力从该玩家开始结算
            gameMode.game.getPlayersByOrder(player).drop(1).filter { groups[group.activeType]?.contains(it) == true }.mapTo(this) { InnoCommand(it, player, group, card) }
            // 触发玩家只处理普通能力
            if (group.activeType == InnoActiveType.NORMAL) this.add(InnoCommand(player, player, group, card))
        }
    }

    /**
     * 设置玩家激活过能力
     * @param player
     */
    fun setPlayerActived(player: InnoPlayer) {
        this.getPlayerParamSet(player)[PLAYER_ACTIVED] = true
    }

    /**
     * 设置玩家被要求过执行能力
     * @param player
     */
    fun setPlayerDomanded(player: InnoPlayer) {
        this.getPlayerParamSet(player)[PLAYER_DEMANDED] = true
    }

    companion object {
        /**
         * 玩家是否使用过能力的标志
         */
        private const val PLAYER_ACTIVED = "PLAYER_ACTIVED"
        /**
         * 玩家是否被要求过使用能力的标志
         */
        private const val PLAYER_DEMANDED = "PLAYER_DEMANDED"

        private const val serialVersionUID = 1L
    }

}
