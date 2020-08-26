package com.f14.tichu.listener

import com.f14.bg.listener.ActionListener
import com.f14.bg.listener.ListenerType
import com.f14.tichu.TichuConfig
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer
import com.f14.tichu.TichuReport

/**
 * tichu的行动监听器

 * @author F14eagle
 */
abstract class TichuActionListener(protected var gameMode: TichuGameMode, listenerType: ListenerType = ListenerType.NORMAL) : ActionListener<TichuPlayer, TichuConfig, TichuReport>(gameMode, listenerType)
