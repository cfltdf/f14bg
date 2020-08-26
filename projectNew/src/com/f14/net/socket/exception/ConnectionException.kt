package com.f14.net.socket.exception

class ConnectionException : Exception {

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super(cause)

    companion object {
        private const val serialVersionUID = 827662582585267236L
    }

}
