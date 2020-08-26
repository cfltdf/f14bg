package com.f14.bg.utils

import com.f14.bg.exception.BoardGameException


object CheckUtils {

    /**
     * 检查b的值,为true则抛出异常信息msg
     * @param b
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun check(b: Boolean, msg: String) {
        if (b) {
            throw BoardGameException(msg)
        }
    }


}
