package com.f14.innovation.listener

import com.f14.bg.listener.ActionListener
import com.f14.bg.listener.ListenerType
import com.f14.innovation.InnoConfig
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.InnoReport

/**
 * Innovation的行动监听器基类

 * @author F14eagle
 */
abstract class InnoActionListener(protected var gameMode: InnoGameMode, listenerType: ListenerType = ListenerType.NORMAL) : ActionListener<InnoPlayer, InnoConfig, InnoReport>(gameMode, listenerType)