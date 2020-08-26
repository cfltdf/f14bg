package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.listener.initParam.OPActionInitParam

abstract class TSOpActionInterruptListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: OPActionInitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    val opActionInitParam: OPActionInitParam
        get() = super.initParam as OPActionInitParam
}
