package com.f14.TS.consts


/**
 * 区域

 * @author F14eagle
 */
enum class Region {
    /**
     * 亚洲
     */
    ASIA,
    /**
     * 欧洲
     */
    EUROPE,
    /**
     * 中东
     */
    MIDDLE_EAST,
    /**
     * 中美洲
     */
    CENTRAL_AMERICA,
    /**
     * 南美洲
     */
    SOUTH_AMERICA,
    /**
     * 非洲
     */
    AFRICA;


    companion object {

        fun getChineseDescr(region: Region): String {
            return when (region) {
                EUROPE -> "欧洲"
                MIDDLE_EAST -> "中东"
                ASIA -> "亚洲"
                CENTRAL_AMERICA -> "中美洲"
                SOUTH_AMERICA -> "南美洲"
                AFRICA -> "非洲"
            }
        }
    }
}
