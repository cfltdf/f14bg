package com.f14.innovation.param


object InnoParamFactory {

    fun createInitParam(): InnoInitParam {
        return InnoInitParam()
    }


    fun createInitParam(num: Int, level: Int): InnoInitParam {
        val res = InnoInitParam()
        res.num = num
        res.level = level
        return res
    }
}
