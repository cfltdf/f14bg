package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChooseHandListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #061-民主制度 监听器

 * @author F14eagle
 */
class InnoCustom061Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    private var num = 0

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        this.calculateNumValue()
    }

    /**
     * 取得需要归还多少牌才能执行效果的参数
     */
    private fun calculateNumValue() {
        // 取得需要归还多少牌才能执行效果的参数
        val params = this.commandList.getPlayerParamSet(this.mainPlayer)
        this.num = params.getInteger("INT_NUM")
    }


    override fun getMsg(player: InnoPlayer): String {
        return "你需要归还至少" + (this.num + 1) + "张牌,就能抓1张[8]计分!"
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 归还选择的牌
        cards.map { gameMode.game.playerRemoveHandCard(player, it) }.forEach { gameMode.game.playerReturnCard(player, it) }
        if (cards.size > this.num) {
            gameMode.game.playerDrawAndScoreCard(player, 8, 1)
            val params = this.commandList.getPlayerParamSet(this.mainPlayer)
            params[INT_NUM] = cards.size
        }
    }

    companion object {
        private const val INT_NUM = "INT_NUM"
    }

}
