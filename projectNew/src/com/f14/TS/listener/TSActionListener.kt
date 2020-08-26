package com.f14.TS.listener

import com.f14.TS.TSConfig
import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.TSReport
import com.f14.bg.listener.ActionListener
import com.f14.bg.listener.ListenerType

/**
 * TS的监听器基类

 * @author F14eagle
 */
abstract class TSActionListener(protected var gameMode: TSGameMode, listenerType: ListenerType = ListenerType.NORMAL) : ActionListener<TSPlayer, TSConfig, TSReport>(gameMode, listenerType)
