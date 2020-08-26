package com.f14.framework.common.util


object Validator {

    fun isEmpty(str: String?): Boolean {
        return str == null || str.isEmpty()
    }

    fun isNotEmpty(str: String): Boolean {
        return !isEmpty(str)
    }

    fun isInArray(value: Any, array: Array<Any>): Boolean {
        for (e in array) {
            if (e == value) {
                return true
            }
        }
        return false
    }
}
