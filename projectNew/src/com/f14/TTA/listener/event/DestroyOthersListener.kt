package com.f14.TTA.listener.event

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.StringUtils
import java.util.*

/**
 * 摧毁其他玩家建筑的事件
 * @author F14eagle
 */
class DestroyOthersListener(gameMode: TTAGameMode, eventAbility: EventAbility, trigPlayer: TTAPlayer) : TTAEventListener(gameMode, eventAbility, trigPlayer) {

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 如果可以跳过选择,则玩家不必回应
        return !this.canPass(player)
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有监听中的玩家创建参数
        for (player in this.listeningPlayers) {
            val param = ChooseParam(player)
            this.setParam(player.position, param)
        }

    }

    /**
     * 检查玩家是否可以跳过选择
     * @param player
     * @return
     */
    private fun canPass(player: TTAPlayer): Boolean {
        // 检查该玩家的选择参数中是否可以跳过
        val param = this.getParam<ChooseParam>(player.position)
        return param.isAllPlayerSelected
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        for (player in this.listeningPlayers) {
            val cp = this.getParam<ChooseParam>(player)
            for (p in cp.selectedPlayer.keys) {
                param[p.position] = cp.selectedPlayer[p]
            }
        }
        return param
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        val param = this.getParam<ChooseParam>(player.position)
        // 设置可选玩家列表
        res.public("availablePositions", StringUtils.array2String(param.availablePositions))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val param = this.getParam<ChooseParam>(player.position)
            val targetPosition = action.getAsInt("targetPosition")
            CheckUtils.check(targetPosition !in param.availablePositions, "不能选择指定的玩家!")
            val target = gameMode.game.getPlayer(targetPosition)
            CheckUtils.check(!param.canSelect(target), "不能选择指定的玩家!")
            CheckUtils.check(param.hasSelected(target), "你已经拆除过该玩家的建筑了!")
            val cardId = action.getAsString("cardId")
            val card = target.getPlayedCard(cardId)
            CheckUtils.check(!eventAbility.test(card), "不能选择这张牌!")
            CheckUtils.check(!card.needWorker || card.availableCount <= 0, "这张牌上没有工人!")
            // 设置已经选择过玩家的参数
            param.setSelectedPlayer(target, card as TechCard)
            if (this.canPass(player)) {
                this.setPlayerResponsed(player)
            }
        } else {
            // 判断玩家是否可以结束
            CheckUtils.check(!this.canPass(player), this.getMsg(player))
            this.setPlayerResponsed(player)
        }
    }


    override val actionString: String
        get() = TTACmdString.ACTION_DESTORY


    override fun getMsg(player: TTAPlayer): String {
        return "你可以摧毁其他所有玩家的城市建筑各1个,请选择!"
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_DESTORY_OTHERS

    /**
     * 选择的参数

     * @author F14eagle
     */
    internal inner class ChooseParam(player: TTAPlayer) {
        val selectedPlayer: Map<TTAPlayer, MutableList<TechCard>>
        /**
         * 取得所有可选玩家的位置列表
         * @return
         */
        val availablePositions: IntArray

        init {
            // 初始化玩家选择参数,只有需要回应的玩家,才会被添加到selectedPlayer中
            selectedPlayer = gameMode.realPlayers.filter { it !== player && !this.canPass(it) }.map { it to ArrayList<TechCard>() }.toMap()
//                    .forEach { selectedPlayer[it] = null }
            // 初始化可选玩家位置数组
            availablePositions = selectedPlayer.keys.map(TTAPlayer::position).toIntArray()
//            availablePositions = IntArray(this.selectedPlayer.size)
//            for ((i, p) in this.selectedPlayer.keys.withIndex()) {
//                availablePositions[i] = p.position
//            }
        }

        /**
         * 检查玩家是否可以跳过选择

         * @param player
         * *
         * @return
         */
        fun canPass(player: TTAPlayer): Boolean {
            // 如果玩家没有该事件指定的摧毁对象,则可以跳过
            if (player.resigned) {
                return true
            }
            return player.buildings.cards.none { eventAbility.test(it) && it.availableCount > 0 }
        }

        /**
         * 判断是否可以选择该玩家
         * @param player
         * @return
         */
        fun canSelect(player: TTAPlayer): Boolean = this.selectedPlayer.containsKey(player)

        /**
         * 判断玩家是否已经选择完成
         * @param player
         * @return
         */
        fun hasSelected(player: TTAPlayer): Boolean = this.selectedPlayer.getValue(player).isNotEmpty()

        /**
         * 判断是否所有的玩家都选过了
         * @return
         */
        val isAllPlayerSelected: Boolean
            get() = this.selectedPlayer.values.all { it.isNotEmpty() }

        /**
         * 设置玩家已经过选择
         * @param player
         */
        fun setSelectedPlayer(player: TTAPlayer, card: TechCard) {
            this.selectedPlayer[player]?.add(card)
        }
    }

}
