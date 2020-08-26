package com.f14.TTA.listener

import com.f14.F14bg.network.CmdFactory
import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.CivilCard
import com.f14.TTA.component.card.LeaderCard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.component.param.AuctionParam
import com.f14.TTA.consts.ActiveAbilityType
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.CivilAbilityType
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.bg.utils.CheckUtils
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

abstract class ChooseArmyListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer) : TTAOrderInterruptListener(gameMode, trigPlayer) {

    /**
     * 调整拍卖所用的加值卡
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun adjustBonusCard(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val cardId = action.getAsString("cardId")
        val c = player.getCard(cardId)
        val selected = action.getAsBoolean("selected")
        val param = this.getParam<AuctionParam>(player.position)
        when {
            c.activeAbility != null && c.activeAbility!!.abilityType == ActiveAbilityType.PA_SHIHUANG_TOMB -> Unit
            c.cardType != CardType.DEFENSE_BONUS && !(gameMode.game.isVersion2 && !this.isColony) && !player.abilityManager.hasAbilitiy(CivilAbilityType.PA_MEIRIN) && !player.abilityManager.hasAbilitiy(CivilAbilityType.PA_CARD_AS_BONUSCARD) -> throw BoardGameException("只能选择防御/殖民地加值卡!")
        }
        param.setBonusCard(c, selected)
        // 向操作的玩家刷新当前出价的总值
        this.sendPlayerAuctionValue(player, player)
    }

    /**
     * 调整拍卖所用的部队
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun adjustUnit(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val cardId = action.getAsString("cardId")
        val num = action.getAsInt("num")
        val param = this.getParam<AuctionParam>(player.position)
        if (cardId.isEmpty()) {
            // 调整出价
            CheckUtils.check(isWar, "不能在战争中选择出价!")
            CheckUtils.check(isColony && num > param.checkTotalValue(), "不能选择超出你要牺牲的部队的出价!")
            CheckUtils.check(!isTichu && num <= 0, "出价必须大于0!")
            param.setTotalValue(num)
        } else {
            // 调整部队
            CheckUtils.check(isTichu, "地主竞价中不能牺牲部队!")
            val card = player.getPlayedCard(cardId)
            when {
                card.cardType == CardType.WONDER -> {
                    val wonder = card as WonderCard
                    val a = player.abilityManager.getAbility(CivilAbilityType.PA_SHIHUANG_TOMB)!!
                    // 检查现有部队数量是否超出出价的部队数量
                    CheckUtils.check(num < 0 || num > wonder.blues || num > a.limit, "部队数量错误,不能进行调整!")
                    param.setBlueNum(a, num)
                }
                card.cardType == CardType.LEADER -> {
                    val a = player.abilityManager.getAbility(CivilAbilityType.PA_COLONIZE_RESOURCE)!!
                    CheckUtils.check(num < 0 || num > a.limit || (num + 1) * num / 2 > player.totalResource, "部队数量错误,不能进行调整!")
                    param.setBlueNum(a, num)
                }
                else -> {
                    CheckUtils.check(card.cardType != CardType.UNIT, "只能选择部队牌!")
                    // 检查现有部队数量是否超出出价的部队数量
                    CheckUtils.check(num < 0 || num > card.availableCount, "部队数量错误,不能进行调整!")
                    if (gameMode.isVersion2 && !this.isColony && player.abilityManager.hasAbilitiy(CivilAbilityType.PA_SACRIFICE_UNIT)) {
                        player.abilityManager.getAbility(CivilAbilityType.PA_SACRIFICE_UNIT)?.let { _ ->
                            val n = param.units.entries.sumBy { (k, v) -> if (card.id == k.id) num else v }
                            if (n > 1) throw BoardGameException("你最多只能牺牲1个部队")
                        }
                    }
                    param.setUnitNum(card as TechCard, num)
                }
            }
            // 向操作的玩家刷新当前出价的总值
        }
        this.sendPlayerAuctionValue(player, player)
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 监听开始前,为所有玩家创建拍卖参数
        for (player in gameMode.game.players) {
            val param = AuctionParam(player, !isWar)
            this.setParam(player.position, param)
        }
    }

    /**
     * 玩家确认
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun confirm(action: GameAction)

    /**
     * 创建玩家选择部队信息的指令
     * @param receiver
     * @return
     */
    protected open fun createAuctionInfoResponse(receiver: TTAPlayer?): BgResponse {
        // 发送玩家的部队及拍卖信息
        val res = CmdFactory.createGameResponse(this.validCode, -1)
        res.public("subact", "loadParam")
        val list = gameMode.game.players.map(this::createPlayerAuctionParam)
        res.public("playersInfo", list)
        // 设置触发器信息
        this.setListenerInfo(res)
        return res
    }

    /**
     * 创建玩家的拍卖参数
     * @param player
     * @return
     */
    protected fun createPlayerAuctionParam(player: TTAPlayer): Map<String, Any> {
        val map = HashMap<String, Any>()
        val param = this.getParam<AuctionParam>(player.position)
        map["position"] = player.position

        val unitsInfo = player.unitsInfo.toMutableList()
        if (!this.isColony && player.position != this.trigPlayer.position && player.abilityManager.hasAbilitiy(CivilAbilityType.PA_SHIHUANG_TOMB)) {
            val c = player.abilityManager.getAbilityCard(CivilAbilityType.PA_SHIHUANG_TOMB) as CivilCard
            if (c.blues > 0) {
                val o = mapOf("cardId" to c.id, "num" to c.blues)
                unitsInfo.add(o)
            }
        }
        if (this.isColony && player.abilityManager.hasAbilitiy(CivilAbilityType.PA_COLONIZE_RESOURCE)) {
            val c = player.abilityManager.getAbilityCard(CivilAbilityType.PA_COLONIZE_RESOURCE) as LeaderCard
            if (player.totalResource > 0) {
                val o = mapOf("cardId" to c.id, "num" to player.totalResource)
                unitsInfo.add(o)
            }
        }
        map["unitsInfo"] = unitsInfo
        val availableBonusCard =
        // 防御和殖民地加值卡应该设置为私有参数
                when {
                    !this.isColony && (player.position == this.trigPlayer.position || this.isWar) -> emptyList()
                    player.abilityManager.hasAbilitiy(CivilAbilityType.PA_MEIRIN) || gameMode.game.isVersion2 && !this.isColony || player.abilityManager.hasAbilitiy(CivilAbilityType.PA_CARD_AS_BONUSCARD) && this.isColony -> // 新版侵略或新版库克,可以使用全部手牌当D
                        player.militaryHands.cards
                    else -> player.bonusCards
                }

        // 如果是receiver玩家正在输入,或者玩家已经输入完成,则向发送该玩家的输入信息
        map["bonusCardIds"] = BgUtils.card2String(availableBonusCard)
        map["auctionParam"] = param.toMap()
        return map
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 设置玩家当前的拍卖信息
        val param = this.getParam<AuctionParam>(player.position)
        res.public("auctionParam", param.toMap())
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        when (action.getAsString("subact")) {
            "adjustUnit" -> // 调整部队
                this.adjustUnit(action)
            "adjustBonusCard" -> // 调整加值卡
                this.adjustBonusCard(action)
            "confirm" -> // 确认拍卖
                this.confirm(action)
            "pass" -> // 放弃拍卖
                this.pass(action)
            else -> throw BoardGameException("无效的行动代码!")
        }
    }

    /**
     * 取得玩家的拍卖值
     * @param player
     * @return
     */
    protected fun getPlayerAuctionValue(player: TTAPlayer): Int {
        val param = this.getParam<AuctionParam>(player.position)
        return param.getTotalValue()
    }

    /**
     * 是否看的是殖民点数
     * @return
     */
    protected open val isColony: Boolean
        get() = false

    protected open val isTichu: Boolean
        get() = false

    protected open val isWar: Boolean
        get() = false

    @Throws(BoardGameException::class)
    override fun onPlayerTurn(player: TTAPlayer) {
        super.onPlayerTurn(player)
        // 玩家回合开始时,将其输入状态设为true
        val param = this.getParam<AuctionParam>(player.position)
        param.inputing = true
    }

    /**
     * 玩家结束
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun pass(action: GameAction)

    protected fun sendAutionEnd(receiver: TTAPlayer?) {
        val res = CmdFactory.createGameResponse(this.validCode, -1)
        res.public("subact", "end")
        gameMode.game.sendResponse(receiver, res)
    }

    /**
     * 向receiver发送player的拍卖信息,receiver为空则向所有玩家发送
     * @param player
     */
    protected fun sendPlayerAuctionInfo(player: TTAPlayer, receiver: TTAPlayer?) {
        val param = this.getParam<AuctionParam>(player.position)
        val res = CmdFactory.createGameResponse(this.validCode, player.position)
        val auctionInfo = param.toMap().toMutableMap()
        if (this.isColony && player !== receiver) {
            auctionInfo.remove("units")
            auctionInfo["units"] = ArrayList<Map<String, Any>>()
            auctionInfo.remove("bonusCards")
            auctionInfo["bonusCards"] = ArrayList<Map<String, Any>>()
        }
        res.public("subact", "auctionParam")
        res.public("auctionParam", auctionInfo)
        gameMode.game.sendResponse(receiver, res)
    }

    /**
     * 向receiver发送player的拍卖总值,receiver为空则向所有玩家发送
     * @param player
     */
    protected fun sendPlayerAuctionValue(player: TTAPlayer, receiver: TTAPlayer?) {
        val param = this.getParam<AuctionParam>(player.position)
        val res = CmdFactory.createGameResponse(this.validCode, player.position)
        res.public("subact", "auctionValue")
        res.public("auctionValue", param.getTotalValue())
        gameMode.game.sendResponse(receiver, res)
    }

    override fun sendPlayerListeningInfo(receiver: TTAPlayer?) {
        super.sendPlayerListeningInfo(receiver)
        // 发送玩家的部队及拍卖信息
        val res = this.createAuctionInfoResponse(receiver)
        // 向receiver发送指令
        gameMode.game.sendResponse(receiver, res)
    }

}
