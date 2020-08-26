package com.f14.PuertoRico.consts

/**
 * 波多黎各的角色

 * @author F14eagle
 */
enum class Character {
    /**
     * 拓荒者
     */
    SETTLE,

    /**
     * 市长
     */
    MAYOR,

    /**
     * 建筑师
     */
    BUILDER,

    /**
     * 手工业者
     */
    CRAFTSMAN,

    /**
     * 商人
     */
    TRADER,

    /**
     * 船长
     */
    CAPTAIN,

    /**
     * 淘金者
     */
    PROSPECTOR;

    companion object {

        /**
         * 角色信息的代码
         */
        const val CODE_TYPE = "PR_CHARACTER"
    }
}
