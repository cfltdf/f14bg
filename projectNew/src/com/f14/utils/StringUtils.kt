package com.f14.utils


object StringUtils {

    /**
     * 将array转换成string
     * @param array
     * @return
     */
    fun array2String(array: IntArray?): String {
        return if (array != null && array.isNotEmpty()) {
            array.joinToString(",")
        } else {
            ""
        }
    }

    /**
     * 将array转换成string
     * @param array
     * @return
     */
    fun array2String(array: Array<Any>?): String {
        return if (array != null && array.isNotEmpty()) {
            array.joinToString(",")
        } else {
            ""
        }
    }

    /**
     * 判断字符串是否为空或null
     * @param str
     * @return
     */
    fun isEmpty(str: String?): Boolean {
        return str.isNullOrEmpty()
    }

    /**
     * 将list转换成string
     * @param coll
     * @return
     */

    fun list2String(coll: Collection<*>): String {
        return coll.joinToString(",")
    }

    /**
     * string转换成int数组
     * @param str
     * @return
     */
    fun string2int(str: String): IntArray {
        return if (isEmpty(str)) {
            IntArray(0)
        } else {
            str.split(",".toRegex()).dropLastWhile(String::isEmpty).map(Integer::valueOf).toIntArray()
        }
    }

    /**
     * 将string转换成list
     * @param string
     * @return
     */
    fun string2List(string: String): List<String> {
        return if (isEmpty(string)) {
            emptyList()
        } else {
            string.split(",".toRegex()).dropLastWhile(String::isEmpty)
        }
    }
}
