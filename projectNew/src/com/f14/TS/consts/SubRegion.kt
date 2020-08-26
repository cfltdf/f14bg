package com.f14.TS.consts


/**
 * 子区域

 * @author F14eagle
 */
enum class SubRegion {
    /**
     * 东欧
     */
    EAST_EUROPE,
    /**
     * 西欧
     */
    WEST_EUROPE,
    /**
     * 东南亚
     */
    SOUTHEAST_ASIA;


    companion object {

        fun getChineseDescr(region: SubRegion): String {
            return when (region) {
                EAST_EUROPE -> "东欧"
                WEST_EUROPE -> "西欧"
                SOUTHEAST_ASIA -> "东南亚"
            }
        }
    }
}
