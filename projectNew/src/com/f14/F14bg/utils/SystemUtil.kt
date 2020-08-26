package com.f14.F14bg.utils

import com.f14.f14bgdb.util.CodeUtil

/**
 * 系统相关类
 * @author F14eagle
 */
object SystemUtil {
    /**
     * 判断是否已调试模型运行游戏
     * @return
     */
    val isDebugMode: Boolean
        get() = RunMode.DEBUG.toString() == CodeUtil.getLabel("RUN_MODE", "MODE")

    /**
     * 运行方式
     * @author F14eagle
     */
    enum class RunMode {
        PLAY, // 游戏
        DEBUG // 调试
    }

}
