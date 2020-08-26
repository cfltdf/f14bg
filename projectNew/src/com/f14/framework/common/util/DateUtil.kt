/*
 * Created on 2004-12-19
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.f14.framework.common.util


import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    private val cal = Calendar.getInstance()

    /**
     * 获取"yyyy-MM-dd HH:mm:ss"格式的系统当前时间

     *
     * @return
     */
    val nowDateByFormat1: String
        get() {
            val format1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = Date()

            return format1.format(date)
        }

    /**
     * 获取"yyyyMMddHHmmss"格式的系统当前时间
     *
     * @return
     */
    val nowDateByFormat2: String
        get() {
            val format1 = SimpleDateFormat("yyyyMMddHHmmss")
            val date = Date()
            return format1.format(date)
        }

    /**
     * 获取"yyyy-MM-dd"格式的系统当前时间
     *
     * @return
     */
    val nowDateByFormat3: String
        get() {
            val format1 = SimpleDateFormat("yyyy-MM-dd")
            val date = Date()
            return format1.format(date)
        }

    /**
     * 转化 日期 TO String 类型, 格式"yyyy-MM-dd"。
     *
     * @return
     */
    fun parseDate(date: Date): String {
        val format1 = SimpleDateFormat("yyyy-MM-dd")
        return format1.format(date)
    }

    /**
     * 比较日期大小
     *
     * @param date1
     * @param date2
     * @return
     */
    fun compareDate(date1: IntArray, date2: IntArray): Boolean {
        var countEqual = 0
        for (i in date1.indices) {
            when {
                date1[i] < date2[i] -> return true
                date1[i] == date2[i] -> countEqual++
                else -> return false
            }
        }
        return countEqual == date1.size
    }

    /**
     * 一个公共的处理方法，将字符串的日期转换成日期对象 日期格式由参数format指定
     */

    fun parseDate(stringDate: String?, format: String): Date? {
        if (stringDate == null || stringDate.trim { it <= ' ' } == "") {
            return null
        }
        val sdf = SimpleDateFormat(format)
        try {
            return sdf.parse(stringDate)
        } catch (ex: ParseException) {
            // logger.info("parseExcpetion: " + ex.getMessage());
            ex.printStackTrace()
        }

        return null
    }

    /**
     * 一个公共的处理方法，将字符串的日期转换成日期对象 日期格式输入为yyyy-MM-dd，输出为yyyy-MM-dd 00:00:00的时间对象
     */

    fun parseDateFirstTime(stringDate: String?): Date? {
        var stringDate = stringDate
        if (stringDate == null || stringDate.trim { it <= ' ' } == "") {
            return null
        }
        stringDate += " 00:00:00"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            return sdf.parse(stringDate)
        } catch (ex: ParseException) {
            // logger.info("parseExcpetion: " + ex.getMessage());
            ex.printStackTrace()
        }

        return null
    }

    /**
     * 一个公共的处理方法，将字符串的日期转换成日期对象 日期格式输入为yyyy-MM-dd，输出为yyyy-MM-dd 23:59:59的时间对象
     */

    fun parseDateEndTime(stringDate: String?): Date? {
        var stringDate = stringDate
        if (stringDate == null || stringDate.trim { it <= ' ' } == "") {
            return null
        }
        stringDate += " 23:59:59"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            return sdf.parse(stringDate)
        } catch (ex: ParseException) {
            // logger.info("parseExcpetion: " + ex.getMessage());
            ex.printStackTrace()
        }

        return null
    }
    /**
     * @return 当前系统日期
     *
     * public static Date getSysDate() { Date sysDate = null;
     * java.text.SimpleDateFormat a = new java.text.SimpleDateFormat(
     * "yyyy-MM-dd HH:mm:ss"); java.text.SimpleDateFormat b = new
     * java.text.SimpleDateFormat( "yyyy-MM-dd HH:mm:ss"); try { sysDate
     * = b.parse(a.format(new Date())); } catch
     * (java.text.ParseException ex) { ex.getMessage(); } return
     * sysDate; }
     */

    /**
     * 一个公共的处理方法，将日期转换成字符串 字符串格式由参数format指定
     */

    fun parseDate(date: Date?, format: String): String? {
        if (date == null) {
            return null
        }
        val sdf = SimpleDateFormat(format)
        return sdf.format(date)
    }

    /**
     * 取得日期后几天的日期,offset为天数偏移量
     *
     * @param date
     * @param offset
     * @return
     */
    fun getLaterDate(date: Date, offset: Int): Date {
        DateUtil.cal.time = date
        DateUtil.cal.add(Calendar.DATE, offset)
        return DateUtil.cal.time
    }
}
