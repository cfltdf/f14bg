package com.f14.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

object CommonUtil {
    val df = SimpleDateFormat("HH:mm:ss")
    val rateFormat = DecimalFormat("##0.00")

    /**
     * 用args中的值替换msg中的值{index}(index为下标)

     * @param msg
     * *
     * @param args
     * *
     * @return
     */
    fun getMsg(msg: String, vararg args: Any?): String {
        var msg = msg
        for (i in args.indices) {
            if (args[i] == null) {
                msg = msg.replace("\\{$i\\}".toRegex(), "-NULL-")
            } else {
                msg = msg.replace("\\{$i\\}".toRegex(), args[i].toString())
            }
        }
        return msg
    }

    /**
     * 取得当前时间字符串

     * @return
     */
    val currentTime: String
        get() = df.format(Date())

    /**
     * 格式化比率到小数点后面2位

     * @param rate
     * *
     * @return
     */
    fun formatRate(rate: Double?): Double {
        return java.lang.Double.valueOf(rateFormat.format(rate))
    }

    // public static <T> T getEnumValue(Class<T> enumType, String str){
    // if(StringUtils.isEmpty(str)){
    // return null;
    // }else{
    // return Enum.valueOf(enumType, str);
    // }
    // }
}
