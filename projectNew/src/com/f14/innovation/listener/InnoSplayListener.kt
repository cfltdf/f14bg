package com.f14.innovation.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam


class InnoSplayListener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseCardListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        if (this.isSpecificalAction) {
            // 如果是特定的动作,则检查玩家是否可以展开该牌堆,如果不能则不需要操作
            if (!player.canSplayStack(this.initParam!!.color!!, this.initParam.splayDirection)) {
                return false
            }
        } else {

        }
        return super.beforeListeningCheck(player)
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        if (this.isSpecificalAction) {
            // 如果已经指定的允许展开的颜色,则直接展开该颜色
            gameMode.game.playerSplayStack(player, this.initParam!!.color!!, this.initParam.splayDirection)
        } else {

        }
        this.setPlayerResponsed(player)
    }


    override // 如果在initParam中指定了展开的颜色,则不需要输入
    val actionString: String
        get() {
            return if (this.isSpecificalAction) {
                "DISABLE"
            } else {
                "STACKS"
            }
        }

    /**
     * 这里没用
     */
    override fun getAvailableCardNum(player: InnoPlayer) = 0

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_SPLAY_CARD

    /**
     * 判断是否是特定的行动(已经指定了操作目标牌堆)

     * @return
     */
    private val isSpecificalAction: Boolean
        get() = this.initParam != null && this.initParam.color != null

}
