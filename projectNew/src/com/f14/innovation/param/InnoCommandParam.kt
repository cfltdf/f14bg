package com.f14.innovation.param

/**
 * 在执行一个InnoCommond时所共用的参数

 * @author F14eagle
 */
class InnoCommandParam {
    var isSetActiveAgain: Boolean = false
    /**
     * 是否进行过检查器

     * @return
     */
    var isChecked: Boolean = false

    /**
     * 重置参数
     */
    fun reset() {
        this.isSetActiveAgain = false
    }
}
