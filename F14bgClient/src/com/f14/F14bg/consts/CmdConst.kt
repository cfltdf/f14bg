package com.f14.F14bg.consts

/**
 * 指令常量

 * @author F14eagle
 */
open class CmdConst {
    companion object {
        /**
         * 应用程序代码
         */
        val APPLICATION_FLAG = 0x1401

        /**
         * 指令类型 - 系统指令
         */
        val SYSTEM_CMD = 0x39
        /**
         * 指令类型 - 聊天指令
         */
        val CHAT_CMD = 0x10
        /**
         * 指令类型 - 游戏指令
         */
        val GAME_CMD = 0x20
        /**
         * 指令类型 - 异常指令
         */
        val EXCEPTION_CMD = 0x00
        /**
         * 指令类型 - 客户端直读指令
         */
        val CLIENT_CMD = 0x15

        /**
         * 代码类型 - 系统指令 - 登录
         */
        val SYSTEM_CODE_LOGIN = 0x3900
        /**
         * 代码类型 - 系统指令 - 初始化资源
         */
        val SYSTEM_CODE_INIT_RESOURCE = 0x3901
        /**
         * 指令类型 - 系统指令 - 连接服务器
         */
        val SYSTEM_CODE_CONNECT = 0x3902
        /**
         * 指令类型 - 系统指令 - 刷新房间列表
         */
        val SYSTEM_CODE_ROOM_LIST = 0x3903
        /**
         * 指令类型 - 系统指令 - 创建房间
         */
        val SYSTEM_CODE_CREATE_ROOM = 0x3904
        /**
         * 指令类型 - 系统指令 - 刷新成员列表
         */
        val SYSTEM_CODE_PLAYER_LIST = 0x3905
        /**
         * 指令类型 - 系统指令 - 进入大厅
         */
        val SYSTEM_CODE_JOIN_HALL = 0x3906
        /**
         * 指令类型 - 系统指令 - 退出大厅
         */
        val SYSTEM_CODE_LEAVE_HALL = 0x3907
        /**
         * 指令类型 - 系统指令 - 进入房间
         */
        val SYSTEM_CODE_JOIN_ROOM = 0x3908
        /**
         * 指令类型 - 系统指令 - 退出房间
         */
        val SYSTEM_CODE_LEAVE_ROOM = 0x3909
        /**
         * 指令类型 - 系统指令 - 本地玩家进入房间
         */
        val SYSTEM_CODE_LOCAL_JOIN = 0x390A
        /**
         * 指令类型 - 系统指令 - 本地玩家退出房间
         */
        val SYSTEM_CODE_LOCAL_LEAVE = 0x390B
        /**
         * 指令类型 - 系统指令 - 大厅添加房间
         */
        val SYSTEM_CODE_ROOM_ADDED = 0x390C
        /**
         * 指令类型 - 系统指令 - 大厅移除房间
         */
        val SYSTEM_CODE_ROOM_REMOVED = 0x390D
        /**
         * 指令类型 - 系统指令 - 大厅房间状态变化
         */
        val SYSTEM_CODE_ROOM_CHANGED = 0x390E
        /**
         * 指令类型 - 系统指令 - 断线后重新连接
         */
        val SYSTEM_CODE_RECONNECT = 0x390F
        /**
         * 指令类型 - 系统指令 - 断线后重新进入游戏
         */
        val SYSTEM_CODE_RECONNECT_GAME = 0x3910
        /**
         * 指令类型 - 系统指令 - 用户注册
         */
        val SYSTEM_CODE_USER_REGIST = 0x3911
        /**
         * 指令类型 - 系统指令 - 排行榜信息
         */
        val SYSTEM_CODE_RANKING_LIST = 0x3912
        /**
         * 指令类型 - 系统指令 - 用户排名信息
         */
        val SYSTEM_CODE_USER_RANK = 0x3913
        /**
         * 指令类型 - 系统指令 - 读取代码
         */
        val SYSTEM_CODE_CODE_DETAIL = 0x3914
        /**
         * 指令类型 - 系统指令 - 读取本地用户信息
         */
        val SYSTEM_CODE_USER_INFO = 0x3915
        /**
         * 代码类型 - 游戏指令 - 玩家加入房间的检查
         */
        val SYSTEM_CODE_JOIN_CHECK = 0x3916
        /**
         * 代码类型 - 游戏指令 - 用户读取房间信息的指令
         */
        val SYSTEM_CODE_LOAD_ROOM_INFO = 0x3917
        /**
         * 代码类型 - 游戏指令 - 刷新用户和房间的基本信息
         */
        val SYSTEM_CODE_USER_ROOM_INFO = 0x3918
        /**
         * 代码类型 - 游戏指令 - 刷新房间里的用户信息
         */
        val SYSTEM_CODE_USER_LIST_INFO = 0x3919
        /**
         * 代码类型 - 游戏指令 - 刷新大厅中用户的信息
         */
        val SYSTEM_CODE_HALL_REFRESH_USER = 0x391A
        /**
         * 代码类型 - 游戏指令 - 刷新房间中用户的信息
         */
        val SYSTEM_CODE_ROOM_REFRESH_USER = 0x391B
        /**
         * 代码类型 - 游戏指令 - 房间中的用户加入游戏
         */
        val SYSTEM_CODE_ROOM_JOIN_PLAY = 0x391C
        /**
         * 代码类型 - 游戏指令 - 房间中的用户离开游戏
         */
        val SYSTEM_CODE_ROOM_LEAVE_PLAY = 0x391D
        /**
         * 代码类型 - 游戏指令 - 用户进入房间
         */
        val SYSTEM_CODE_ROOM_JOIN = 0x391E
        /**
         * 代码类型 - 游戏指令 - 用户离开房间
         */
        val SYSTEM_CODE_ROOM_LEAVE = 0x391F
        /**
         * 代码类型 - 游戏指令 - 用户按键状态变化
         */
        val SYSTEM_CODE_ROOM_USER_BUTTON = 0x3920
        /**
         * 代码类型 - 游戏指令 - 读取游戏设置
         */
        val SYSTEM_CODE_LOAD_CONFIG = 0x3921
        /**
         * 代码类型 - 游戏指令 - 用户离开房间的请求
         */
        val SYSTEM_CODE_ROOM_LEAVE_REQUEST = 0x3922
        /**
         * 代码类型 - 游戏指令 - 用户准备游戏
         */
        val SYSTEM_CODE_USER_READY = 0x3923
        /**
         * 代码类型 - 游戏指令 - 用户开始游戏
         */
        val SYSTEM_CODE_USER_START = 0x3924
        /**
         * 代码类型 - 游戏指令 - 检查大厅公告
         */
        val SYSTEM_CODE_HALL_NOTICE = 0x3925
        /**
         * 代码类型 - 游戏指令 - 发送房间邀请通知
         */
        val SYSTEM_CODE_ROOM_INVITE_NOTIFY = 0x3926
        /**
         * 代码类型 - 游戏指令 - 保存录像指令
         */
        val SYSTEM_CODE_SAVE_REPLAY = 0x3927
        /**
         * 代码类型 - 游戏指令 - 用户换人的请求
         */
        val SYSTEM_CODE_ROOM_REPLACE_REQUEST = 0x3928
        /**
         * 代码类型 - 游戏指令 - 房间中的用户换人
         */
        val SYSTEM_CODE_ROOM_REPLACE = 0x3929
        /**
         * 代码类型 - 游戏指令 - 载入战报指令
         */
        val SYSTEM_CODE_LOAD_REPORT = 0x392A

        /**
         * 代码类型 - 游戏指令 - 进入游戏
         */
        val GAME_CODE_JOIN = 0x2000
        /**
         * 代码类型 - 游戏指令 - 开始游戏
         */
        val GAME_CODE_START = 0x2001
        /**
         * 代码类型 - 游戏指令 - 读取所有玩家信息
         */
        val GAME_CODE_LOAD_PLAYER = 0x2006
        /**
         * 代码类型 - 游戏指令 - 设置游戏起始信息
         */
        val GAME_CODE_SETUP = 0x2007
        /**
         * 代码类型 - 游戏指令 - 读取本地玩家信息
         */
        val GAME_CODE_LOCAL_PLAYER = 0x2008
        /**
         * 指令类型 - 系统指令 - 断线后重新连接游戏
         */
        val GAME_CODE_RECONNECT_GAME = 0x2009
        /**
         * 代码类型 - 游戏指令 - 游戏结束
         */
        val GAME_CODE_END = 0x2015
        /**
         * 代码类型 - 游戏指令 - 结束时的分数统计
         */
        val GAME_CODE_VP_BOARD = 0x2016
        /**
         * 代码类型 - 游戏指令 - 离开游戏
         */
        val GAME_CODE_LEAVE = 0x2017
        /**
         * 代码类型 - 游戏指令 - 玩家被移除游戏
         */
        val GAME_CODE_REMOVE_PLAYER = 0x2018
        /**
         * 代码类型 - 游戏指令 - 玩家准备游戏
         */
        val GAME_CODE_PLAYER_READY = 0x2019
        /**
         * 代码类型 - 游戏指令 - 观战游戏
         */
        val GAME_CODE_AUDIENCE = 0x2020
        /**
         * 代码类型 - 游戏指令 - 退出观战
         */
        val GAME_CODE_EXIT_AUDIENCE = 0x2021
        /**
         * 代码类型 - 游戏指令 - 刷新游戏进行中的信息
         */
        val GAME_CODE_PLAYING_INFO = 0x2022
        /**
         * 代码类型 - 游戏指令 - 刷新玩家状态
         */
        val GAME_CODE_PLAYER_STATE = 0x2023
        /**
         * 代码类型 - 游戏指令 - 刷新玩家游戏进行状态
         */
        val GAME_CODE_PLAYING_STATE = 0x2024
        /**
         * 代码类型 - 游戏指令 - 开始换人
         */
        val GAME_CODE_REPLACE_START = 0x2025
        /**
         * 代码类型 - 游戏指令 - 结束换人
         */
        val GAME_CODE_REPLACE_END = 0x2026

        /**
         * 代码类型 - 游戏指令 - 刷新所有当前信息
         */
        val GAME_CODE_REFRESH_ALL = 0x2100
        /**
         * 代码类型 - 游戏指令 - 监听器开始监听
         */
        val GAME_CODE_START_LISTEN = 0x2200
        /**
         * 代码类型 - 游戏指令 - 玩家输入回应
         */
        val GAME_CODE_PLAYER_RESPONSED = 0x2201
        /**
         * 代码类型 - 游戏指令 - 阶段开始
         */
        val GAME_CODE_PHASE_START = 0x2202
        /**
         * 代码类型 - 游戏指令 - 阶段结束
         */
        val GAME_CODE_PHASE_END = 0x2203
        /**
         * 代码类型 - 游戏指令 - 刷新游戏时间
         */
        val GAME_CODE_GAME_TIME = 0x2204
        /**
         * 代码类型 - 游戏指令 - 读取游戏设置
         */
        val GAME_CODE_LOAD_CONFIG = 0x2205
        /**
         * 代码类型 - 游戏指令 - 设置游戏
         */
        val GAME_CODE_SET_CONFIG = 0x2206
        /**
         * 代码类型 - 游戏指令 - 发送战报
         */
        val GAME_CODE_LOAD_REPORT = 0x220A
        /**
         * 代码类型 - 游戏指令 - 发送战报信息
         */
        val GAME_CODE_REPORT_MESSAGE = 0x220B
        /**
         * 代码类型 - 游戏指令 - 刷新当前提示信息
         */
        val GAME_CODE_REFRESH_MSG = 0x220C
        /**
         * 代码类型 - 游戏指令 - 游戏信息提示
         */
        val GAME_CODE_TIP_ALERT = 0x220D
        /**
         * 代码类型 - 游戏指令 - 游戏的简单指令
         */
        val GAME_CODE_SIMPLE_CMD = 0x220E
        /**
         * 代码类型 - 游戏指令 - 游戏的动画指令
         */
        val GAME_CODE_ANIM_CMD = 0x220F
        /**
         * 代码类型 - 游戏指令 - MAGIC CODE
         */
        val GAME_CODE_MAGIC_CODE = 0x2210

        /**
         * 代码类型 - 聊天指令 - 聊天信息
         */
        val CHAT_CODE_MESSAGE = 0x1000

        /**
         * 指令类型 - 客户端直读指令 - 装载系统代码
         */
        val CLIENT_LOAD_CODE = 0x1500
        /**
         * 指令类型 - 客户端直读指令 - 打开房间窗口
         */
        val CLIENT_OPEN_ROOM = 0x1501
        /**
         * 指令类型 - 客户端直读指令 - 装载资源字符串
         */
        val CLIENT_INIT_RESOURCE = 0x1502
        /**
         * 指令类型 - 客户端直读指令 - 关闭房间窗口
         */
        val CLIENT_CLOSE_ROOM = 0x1503
        /**
         * 指令类型 - 客户端直读指令 - 确认是否离开房间
         */
        val CLIENT_LEAVE_ROOM_CONFIRM = 0x1504
        /**
         * 指令类型 - 客户端直读指令 - 检查模块是否需要更新
         */
        val CLIENT_CHECK_UPDATE = 0x1505
        /**
         * 指令类型 - 客户端直读指令 - 查看用户信息
         */
        val CLIENT_USER_INFO = 0x1506
        /**
         * 指令类型 - 客户端直读指令 - 广播消息
         */
        val CLIENT_BROADCAST = 0x1507
        /**
         * 指令类型 - 客户端直读指令 - 打开大厅公告
         */
        val CLIENT_HALL_NOTICE = 0x1508
        /**
         * 指令类型 - 客户端直读指令 - 发送气泡通知
         */
        val CLIENT_BUBBLE_NOTIFY = 0x1509
        /**
         * 指令类型 - 客户端直读指令 - 保存录像
         */
        val CLIENT_SAVE_REPLAY = 0x150A
    }


}
