package com.f14.bg.report

import com.google.gson.annotations.SerializedName

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

class MessageObject(var time: String = "", @SerializedName("message") var msgPublic: String, var isAlert: Boolean = false, @Transient var position: Int = -1, @Transient var msgPrivate: String? = null) {
    override fun toString() = listOfNotNull(this.time, this.msgPublic).joinToString(" ")

    fun toPrivate() = MessageObject(time, msgPrivate ?: msgPublic, isAlert, position)

}
