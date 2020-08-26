package com.f14.TTA.listener

import com.f14.F14bg.network.CmdFactory
import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.param.AuctionParam
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class ChooseArmyWarListener(
        gameMode: TTAGameMode,
        private var warCard: AttackCard,
        trigPlayer: TTAPlayer = warCard.owner!!,
        private var targetPlayer: TTAPlayer = warCard.target!!
) : ChooseArmyListener(gameMode, trigPlayer) {

    init {
        this.addListeningPlayer(trigPlayer)
        this.addListeningPlayer(targetPlayer)
    }

    @Throws(BoardGameException::class)
    override fun adjustBonusCard(action: GameAction) {
        // 战争时不允许使用加值卡
        CheckUtils.check(this.warCard.cardType == CardType.WAR, "战争时不允许使用加值卡!")
        // 侵略时,只有防守方可以出加值卡
        val player = action.getPlayer<TTAPlayer>()
        CheckUtils.check(this.warCard.cardType == CardType.AGGRESSION && player !== this.targetPlayer, "侵略时只有防守方可以使用加值卡!")
        super.adjustBonusCard(action)
    }

    @Throws(BoardGameException::class)
    override fun adjustUnit(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val cardId = action.getAsString("cardId")
        if (cardId.isNotEmpty()) {
            val card = player.getPlayedCard(cardId)
            if (card.cardType != CardType.WONDER) {
                if (gameMode.game.isVersion2 && !gameMode.game.isTeamMatch /* 22可以牺牲 */) {
                    if (!player.abilityManager.hasAbilitiy(CivilAbilityType.PA_SACRIFICE_UNIT)) {
                        throw BoardGameException("新版不允许牺牲部队!")
                    }
                } else {
                    // 如果是最后一个回合,则不允许牺牲部队
                    CheckUtils.check(gameMode.finalRound, "最后一个回合中不允许牺牲部队!")
                }
            }
        }
        super.adjustUnit(action)
    }

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 战争时,新版以及老版最后一回合直接结算
        return when {
            player.position != trigPlayer.position && player.abilityManager.hasAbilitiy(CivilAbilityType.PA_SHIHUANG_TOMB) -> true
            player.abilityManager.hasAbilitiy(CivilAbilityType.PA_SACRIFICE_UNIT) -> true
            !gameMode.isVersion2 && gameMode.finalRound && this.isWar -> false
            !gameMode.isVersion2 && gameMode.finalRound && !this.isWar && player.position == trigPlayer.position -> false
            gameMode.isVersion2 && !this.isWar && player.position == trigPlayer.position -> false
            else -> true
        }
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 监听开始前,为所有玩家创建拍卖参数
        for (p in this.listeningPlayers) {
            val param = AuctionParam(p, this.isColony)
            this.setParam(p, param)
        }
        // 检查进攻方玩家调整军事点数的能力
        this.getParam<AuctionParam>(this.trigPlayer).military = trigPlayer.getAttackerMilitary(warCard, targetPlayer)
        this.getParam<AuctionParam>(this.targetPlayer).military = targetPlayer.defenceMilitary
    }

    /**
     * 玩家确认部队
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun confirm(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<AuctionParam>(player.position)
        if (gameMode.game.isVersion2) {
            CheckUtils.check(param.selectedBonusCards.size > player.getProperty(CivilizationProperty.MILITARY_ACTION), "不能使用超出你的军事行动点数的卡牌!")
        }
        // 确认部队,完成回应,等待对方选择部队
        param.inputing = false
        this.setPlayerResponsed(player)
        // 向所有玩家刷新当前部队的信息
        this.sendPlayerAuctionInfo(player, null)
    }

    /**
     * 创建玩家选择部队信息的指令
     * @param receiver
     * @return
     */
    override fun createAuctionInfoResponse(receiver: TTAPlayer?): BgResponse {
        // 发送玩家的部队及拍卖信息
        val res = CmdFactory.createGameResponse(this.validCode, -1)
        res.public("subact", "loadParam")
        // 只生成触发玩家和目标玩家的拍卖信息
        val list = ArrayList<Map<String, Any>>()
        list.add(this.createPlayerAuctionParam(trigPlayer))
        list.add(this.createPlayerAuctionParam(targetPlayer))
        res.public("playersInfo", list)
        // 设置触发器信息
        this.setListenerInfo(res)
        // 设置战争/侵略牌
        res.public("showCardId", this.warCard.id)
        return res
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        // 所有玩家都结束后,结算结果
        param[trigPlayer.position] = this.getParam<Any>(trigPlayer.position)
        param[targetPlayer.position] = this.getParam<Any>(targetPlayer.position)
        return param
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_WAR

    override val isWar: Boolean
        get() = warCard.cardType == CardType.WAR

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        this.sendAutionEnd(null)
        super.onAllPlayerResponsed()

    }

    /**
     * 玩家结束拍卖
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun pass(action: GameAction) {
        // 放弃则清空玩家所有的选择
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<AuctionParam>(player.position)
        param.clear()
        // 确认部队,完成回应,等待对方选择部队
        param.inputing = false
        this.setPlayerResponsed(player)
        // 向所有玩家刷新当前部队的信息
        this.sendPlayerAuctionInfo(player, null)
    }
}
