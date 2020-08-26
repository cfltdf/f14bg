package com.f14.innovation.consts


/**
 * 展开方向

 * @author F14eagle
 */
enum class InnoSplayDirection {
    /**
     * 无
     */
    NULL,
    /**
     * 左
     */
    LEFT,
    /**
     * 右
     */
    RIGHT,
    /**
     * 上
     */
    UP;


    companion object {

        fun getDescr(dir: InnoSplayDirection): String {
            return when (dir) {
                LEFT -> "左"
                RIGHT -> "右"
                UP -> "上"
                else -> ""
            }
        }
    }

}
