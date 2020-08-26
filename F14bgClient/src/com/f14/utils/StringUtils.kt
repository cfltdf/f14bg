package com.f14.utils

import java.util.ArrayList

object StringUtils {

    /**
     * 判断字符串是否为空或null

     * @param str
     * *
     * @return
     */
    fun isEmpty(str: String?): Boolean {
        if (str == null || str.length == 0) {
            return true
        } else {
            return false
        }
    }

    /**
     * 将list转换成string

     * @param coll
     * *
     * @return
     */
    fun list2String(coll: Collection<*>): String {
        var res = ""
        if (!coll.isEmpty()) {
            for (o in coll) {
                res += o.toString() + ","
            }
            res = res.substring(0, res.length - 1)
        }
        return res
    }

    /**
     * 将string转换成list

     * @param string
     * *
     * @return
     */
    fun string2List(string: String): List<String> {
        val res = ArrayList<String>()
        if (!isEmpty(string)) {
            val strs = string.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (s in strs) {
                res.add(s)
            }
        }
        return res
    }

    /**
     * 将array转换成string

     * @param array
     * *
     * @return
     */
    fun array2String(array: Array<Any>?): String {
        var res = ""
        if (array != null && array.size > 0) {
            for (o in array) {
                res += o.toString() + ","
            }
            res = res.substring(0, res.length - 1)
        }
        return res
    }

    /**
     * 将array转换成string

     * @param array
     * *
     * @return
     */
    fun array2String(array: IntArray?): String {
        var res = ""
        if (array != null && array.size > 0) {
            for (o in array) {
                res += o.toString() + ","
            }
            res = res.substring(0, res.length - 1)
        }
        return res
    }

    /**
     * 返回str在strs中的序列,如果没找到则返回-1

     * @param strs
     * *
     * @param str
     * *
     * @return
     */
    fun indexOfArray(strs: Array<String>, str: String): Int {
        for (i in strs.indices) {
            if (strs[i] == str) {
                return i
            }
        }
        return -1
    }

    /**
     * 返回in在ints中的序列,如果没找到则返回-1

     * @param ints
     * *
     * @param in
     * *
     * @return
     */
    fun indexOfArray(ints: IntArray, `in`: Int): Int {
        for (i in ints.indices) {
            if (ints[i] == `in`) {
                return i
            }
        }
        return -1
    }

    /**
     * string转换成int数组

     * @param str
     * *
     * @return
     */
    fun string2int(str: String): IntArray {
        if (isEmpty(str)) {
            return IntArray(0)
        }
        val ss = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val res = IntArray(ss.size)
        for (i in ss.indices) {
            res[i] = Integer.valueOf(ss[i])!!
        }
        return res
    }
}
