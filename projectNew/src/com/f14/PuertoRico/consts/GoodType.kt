package com.f14.PuertoRico.consts


/**
 * 货物类型

 * @author F14eagle
 */
enum class GoodType {
    /**
     * 玉米
     */
    CORN,
    /**
     * 染料
     */
    INDIGO,
    /**
     * 糖
     */
    SUGAR,
    /**
     * 烟草
     */
    TOBACCO,
    /**
     * 咖啡
     */
    COFFEE;


    /**
     * 取得货物类型对应的中文描述
     * @return
     */
    val chinese: String
        get() {
            return when (this) {
                CORN -> "玉米"
                INDIGO -> "染料"
                SUGAR -> "糖"
                TOBACCO -> "烟草"
                COFFEE -> "咖啡"
            }
        }
}
