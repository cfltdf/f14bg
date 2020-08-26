package com.f14.PuertoRico.game.listener

import com.f14.PuertoRico.game.PRGameMode
import com.f14.PuertoRico.game.PRPlayer
import com.f14.PuertoRico.game.PrConfig
import com.f14.PuertoRico.game.PrReport
import com.f14.bg.listener.ActionListener

abstract class PRActionListener(protected var gameMode: PRGameMode) : ActionListener<PRPlayer, PrConfig, PrReport>(gameMode)
