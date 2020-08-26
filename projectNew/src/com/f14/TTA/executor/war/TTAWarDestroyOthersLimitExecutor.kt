package com.f14.TTA.executor.war

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.listener.war.DestroyOthersLimitListener
import com.f14.TTA.listener.war.DestroyOthersWarListener


/**
 * 摧毁(等级限制)的战争(侵略)结果处理器

 * @author 吹风奈奈
 */
class TTAWarDestroyOthersLimitExecutor(param: RoundParam, card: AttackCard, winner: TTAPlayer, loser: TTAPlayer, advantage: Int) : TTAWarDestroyOthersExecutor(param, card, winner, loser, advantage) {


    override fun createListener(): DestroyOthersWarListener {
        return DestroyOthersLimitListener(gameMode, player, card, winner, loser, createWarParam())
    }

}
