package com.f14.TTA.listener

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTAGameCmd

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

abstract class TTAActiveCardListener(protected var param: RoundParam, protected var activeCard: TTACard) : TTAInterruptListener(param.gameMode, param.player) {

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_ACTIVABLE_CARD
}
