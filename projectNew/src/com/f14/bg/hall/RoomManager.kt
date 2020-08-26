package com.f14.bg.hall

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */

object RoomManager {
    private var idseq = 1000

    /**
     * 生成房间id
     * @return
     */
    @Synchronized
    fun generateRoomId() = idseq++
}
