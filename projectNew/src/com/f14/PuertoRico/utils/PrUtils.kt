package com.f14.PuertoRico.utils

import com.f14.PuertoRico.component.PrPartPool
import com.f14.PuertoRico.consts.GoodType
import com.f14.bg.exception.BoardGameException
import net.sf.json.JSONObject


object PrUtils {

    /**
     * 取得货物类型,如果不存在则抛出异常
     * @param goodType
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getGoodType(goodType: String): GoodType {
        try {
            return GoodType.valueOf(goodType)
        } catch (e: Exception) {
            throw BoardGameException("未知的货物类型!")
        }

    }

    /**
     * 按照partString解析成PrPartPool对象
     * @param partString
     * @return
     */
    @Throws(BoardGameException::class)
    fun getPartInfo(partString: String): PrPartPool {
        try {
            val obj = JSONObject.fromObject(partString)
            val part = PrPartPool()
            for (o in obj.keys) {
                val key = o as String
                val num = obj.getInt(key)
                val goodType = GoodType.valueOf(key)
                part.putPart(goodType, num)
            }
            return part
        } catch (e: Exception) {
            throw BoardGameException("转换PartPool对象时出错!")
        }

    }
}
