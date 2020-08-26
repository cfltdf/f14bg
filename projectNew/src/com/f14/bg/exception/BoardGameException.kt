package com.f14.bg.exception

/**
 * 桌游的异常信息

 * @author F14eagle
 */
class BoardGameException(msg: String?) : Exception(msg) {
    companion object {
        private const val serialVersionUID = -7308251586513736435L
    }
}
