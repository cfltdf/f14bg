package com.f14.TS.component

import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TSDurationSession
import com.f14.TS.consts.TSDurationType

class DurationResult {
    var durationType: TSDurationType = TSDurationType.GLOBAL
    var target: SuperPower = SuperPower.NONE
    var durationSession: TSDurationSession = TSDurationSession.INSTANT
}
