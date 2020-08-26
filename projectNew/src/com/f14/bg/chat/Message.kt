package com.f14.bg.chat

class Message(val msg: String, val name: String = "", val loginName: String = "") {

    fun send(sender: IChatable) {
        sender.sendMessage(this)
    }

    fun send(senders: Collection<IChatable>) = synchronized(senders) {
        senders.forEach(this::send)
    }

    fun <T> send(sender: IChatableWith<T>, arg: T) {
        sender.sendMessage(arg, this)
    }

    fun <T> send(arg: T): (IChatableWith<T>) -> Unit = { sender ->
        this.send(sender, arg)
    }

    fun <T> send(senders: Collection<IChatableWith<T>>, arg: T) = synchronized(senders) {
        senders.forEach(this.send(arg))
    }
}
