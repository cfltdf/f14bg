package com.f14.innovation.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoActiveType
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 选择其他玩家牌堆的监听器

 * @author F14eagle
 */
open class InnoChoosePlayerStackListener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseStackListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        if (this.initParam != null) {
            if (this.abilityGroup != null && this.abilityGroup.activeType == InnoActiveType.DEMAND) {
                // 如果是DEMAND技能,则只要存在可以选的牌,就需要回应
                if (this.getAvailableCardNum(player) == 0) {
                    return false
                }
            } else {
                // 如果需要选择牌,并且牌数量不足时,则不需要回应
                if (this.initParam.num > 0 && this.getAvailableCardNum(player) < this.initParam.num) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * 判断是否可以选择玩家
     * @param player
     * @param target
     * @return
     */
    protected open fun canChoosePlayer(player: InnoPlayer, target: InnoPlayer): Boolean {
        // 判断能不能选择自己
        if (this.initParam == null || !this.initParam.isCanChooseSelf) {
            if (player === target) {
                return false
            }
        }
        return true
    }

    /**
     * 判断是否可以结束回应
     * @param player
     * @return
     */
    override fun canEndResponse(player: InnoPlayer): Boolean {
        if (this.getAvailableCardNum(player) == 0) {
            return true
        }
        if (this.initParam != null) {
            if (this.selectedCards.size < this.initParam.num) {
                return false
            }
        }
        return true
    }

    /**
     * 对所选的牌进行校验
     * @param player
     * @param target
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun checkChooseCard(player: InnoPlayer, target: InnoPlayer, cards: List<InnoCard>) {
        if (cards.isEmpty()) {
            throw BoardGameException("请选择置顶牌!")
        }
        if (this.initParam != null) {
            if (this.initParam.maxNum > 0) {
                if (cards.size > this.initParam.maxNum) {
                    throw BoardGameException("你至多只能选择" + this.initParam.maxNum + "张置顶牌!")
                }
            }
        }
        if (this.cannotChooseCard(player, cards)) {
            throw BoardGameException("你不能选择这张牌!")
        }
    }

    /**
     * 检查是否可以选择目标玩家
     * @param player
     * @param target
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun checkChoosePlayer(player: InnoPlayer, target: InnoPlayer) {
        if (!this.canChoosePlayer(player, target)) {
            throw BoardGameException("不能选择该玩家!")
        }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val targetPosition = action.getAsInt("choosePosition")
        val target = gameMode.game.getPlayer(targetPosition)
        this.checkChoosePlayer(player, target)

        // 选择置顶牌时只指定了牌堆的颜色,需要按照牌堆的颜色取得对应的置顶牌
        val colorString = action.getAsString("colors").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择置顶牌!")
        val colorStrings = BgUtils.string2Array(colorString)
        val colors = colorStrings.map { InnoColor.valueOf(it) }.toTypedArray()
        val cards = target.getTopCards(*colors)

        this.checkChooseCard(player, target, cards)
        this.processChooseCard(player, target, cards)
        this.afterProcessChooseCard(player, cards)
        this.checkPlayerResponsed(player)
    }


    override val actionString: String
        get() = "ACTION_CHOOSE_PLAYER_STACK"

    /**
     * 取得所有可供选择牌的数量

     * @param player

     * @return
     */
    override fun getAvailableCardNum(player: InnoPlayer): Int {
        return gameMode.game.players.filter { this.canChoosePlayer(player, it) }.sumBy { p -> p.topCards.count { this.canChooseCard(p, it) } }
    }

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_CHOOSE_STACK

    /**
     * 处理玩家选择的牌
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun processChooseCard(player: InnoPlayer, target: InnoPlayer, cards: List<InnoCard>) {

    }

}
