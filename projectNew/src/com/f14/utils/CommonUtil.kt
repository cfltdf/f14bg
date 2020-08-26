package com.f14.utils


import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object CommonUtil {
    val df = SimpleDateFormat("HH:mm:ss")
    val rateFormat = DecimalFormat("##0.00")

    /**
     * 取得当前时间字符串
     *
     * @return
     */
    val currentTime: String
        get() = CommonUtil.df.format(Date())

    /**
     * 格式化比率到小数点后面2位
     *
     * @param rate
     * @return
     */
    fun formatRate(rate: Double?): Double {
        return java.lang.Double.valueOf(CommonUtil.rateFormat.format(rate))
    }

    /**
     * 用args中的值替换msg中的值{index}(index为下标)
     *
     * @param msg
     * @param args
     * @return
     */
    fun getMsg(msg: String, vararg args: Any): String {
        return args.withIndex().fold(msg) { m, i ->
            m.replace(("""\{${i.index}\}""").toRegex(), i.value.toString())
        }
    }
}
