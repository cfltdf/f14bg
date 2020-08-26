package com.f14.framework.exception

import java.io.Serializable

class BusinessException : Exception {
    private val msg: String
    var errorCode: String? = null
    var parms: Array<Serializable>? = null

    constructor(msg: String) : super(msg) {
        this.msg = msg
    }

    constructor(code: String, msg: String) : super(msg) {
        errorCode = code
        this.msg = msg
    }

    constructor(code: String, msg: String, parms: Array<Serializable>) : super(msg) {
        errorCode = code
        this.parms = parms
        this.msg = msg
    }

    override fun toString(): String {
        return this.msg
    }

    companion object {

        private const val serialVersionUID = 1L
    }
}
