package com.f14.framework.common

interface Const {
    companion object {
        /**
         * 项目字符串
         */
        const val APPLICATION_FLAG = "f14fervlet_lostf"
        /**
         * session中存放用户信息的key
         */
        const val SESSION_ATTRIBUTE_USER = "_F14_Framework_UserInfo"
        /**
         * session中存放角色信息的key
         */
        const val SESSION_ATTRIBUTE_CHAR = "_F14_Framework_CharInfo"
        /**
         * request中存放输出参数的key
         */
        const val REQUEST_ATTRIBUTE_OUTPARAMETERS = "_F14_Framework_OutParameters"
        /**
         * request中存放json参数的key
         */
        const val REQUEST_ATTRIBUTE_JSONPARAM = "_F14_Frameworf_JsonParam"
        /**
         * json中存放参数的key
         */
        const val JSON_ATTRIBUTE_PARAM = "_F14_Json_pageParam"
        /**
         * json中存放分页信息的key
         */
        const val JSON_ATTRIBUTE_PAGEINFO = "_F14_Json_pageInfo"
        /**
         * json中存放查询结果的key
         */
        const val JSON_ATTRIBUTE_RESULT = "_F14_Json_pageResult"

        /**
         * request中存放Fervlet输出参数的key
         */
        const val REQUEST_ATTRIBUTE_FERVLET_PARAMS_OUT = "_F14_Fervlet_Params_Out"
        /**
         * request中存放Fervlet bean的key
         */
        const val REQUEST_ATTRIBUTE_FERVLET_BEAN = "_F14_Fervlet_Bean"

        /**
         * 分页大小
         */
        const val PAGE_SIZE = 15

        /**
         * 数据字段常量 - 是
         */
        const val IND_TRUE = "Y"
        /**
         * 数据字段常量 - 否
         */
        const val IND_FALSE = "N"

        /**
         * 用户允许的最大玩家数
         */
        const val MAX_USER_CHARACTER = 6
    }
}
