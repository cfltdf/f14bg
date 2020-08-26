package com.f14.innovation.consts

/**
 * Innovation中的卡牌颜色

 * @author F14eagle
 */
enum class InnoColor {
    /**
     * 红
     */
    RED,

    /**
     * 黄
     */
    YELLOW,

    /**
     * 绿
     */
    GREEN,

    /**
     * 蓝
     */
    BLUE,

    /**
     * 紫
     */
    PURPLE;


    companion object {

        fun getDescr(color: InnoColor): String {
            return when (color) {
                BLUE -> "蓝色"
                GREEN -> "绿色"
                PURPLE -> "紫色"
                RED -> "红色"
                YELLOW -> "黄色"
            }
        }
    }
}
