package com.f14.F14bg.manager

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.consts.GameType
import com.f14.F14bg.network.CmdFactory
import com.f14.bg.action.BgResponse
import com.f14.bg.action.ISendable
import com.f14.bg.exception.BoardGameException
import org.apache.log4j.Logger


abstract class ResourceManager {
    protected var log = Logger.getLogger(javaClass)!!
    protected val resourceResponse: BgResponse by lazy(this::createResourceResponse)
    lateinit var loader: ClassLoader

    /**
     * 创建默认的资源信息对象
     * @return
     */
    protected open fun createResourceResponse() = CmdFactory.createClientResponse(CmdConst.CLIENT_INIT_RESOURCE)
            .public("gameType", gameType.toString())

    /**
     * 取得游戏类型
     * @return
     */
    abstract val gameType: GameType

    /**
     * 初始化
     * @throws Exception
     */
    @Throws(Exception::class)
    open fun init() = Unit

    /**
     * 向玩家发送资源信息
     * @param handler
     * @throws BoardGameException
     */
    fun sendResourceInfo(sender: ISendable) = this.resourceResponse.send(sender)

}
