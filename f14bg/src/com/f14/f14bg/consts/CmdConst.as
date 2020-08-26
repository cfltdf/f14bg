package com.f14.f14bg.consts
{
	public class CmdConst
	{
		/**
		 * 应用程序代码
		 */
		public static var APPLICATION_FLAG:int = 0x1401;
		/**
		 * 指令类型 - 系统指令
		 */
		public static var SYSTEM_CMD:int = 0x39;
		/**
		 * 指令类型 - 聊天指令
		 */
		public static var CHAT_CMD:int = 0x10;
		/**
		 * 指令类型 - 游戏指令
		 */
		public static var GAME_CMD:int = 0x20;
		/**
		 * 指令类型 - 异常指令
		 */
		public static var EXCEPTION_CMD:int = 0x00;
		
		/**
		 * 代码类型 - 系统指令 - 登录
		 */
		public static var SYSTEM_CODE_LOGIN:int = 0x3900;
		/**
		 * 代码类型 - 系统指令 - 装载所有资源信息
		 */
		public static var SYSTEM_CODE_INIT_RESOURCE:int = 0x3901;
		/**
		 * 指令类型 - 系统指令 - 连接服务器
		 */
		public static var SYSTEM_CODE_CONNECT:int = 0x3902;
		/**
		 * 指令类型 - 系统指令 - 刷新房间列表
		 */
		public static var SYSTEM_CODE_ROOM_LIST:int = 0x3903;
		/**
		 * 指令类型 - 系统指令 - 创建房间
		 */
		public static var SYSTEM_CODE_CREATE_ROOM:int = 0x3904;
		/**
		 * 指令类型 - 系统指令 - 刷新成员列表
		 */
		public static var SYSTEM_CODE_PLAYER_LIST:int = 0x3905;
		/**
		 * 指令类型 - 系统指令 - 进入大厅
		 */
		public static var SYSTEM_CODE_JOIN_HALL:int = 0x3906;
		/**
		 * 指令类型 - 系统指令 - 退出大厅
		 */
		public static var SYSTEM_CODE_LEAVE_HALL:int = 0x3907;
		/**
		 * 指令类型 - 系统指令 - 进入房间
		 */
		public static var SYSTEM_CODE_JOIN_ROOM:int = 0x3908;
		/**
		 * 指令类型 - 系统指令 - 退出房间
		 */
		public static var SYSTEM_CODE_LEAVE_ROOM:int = 0x3909;
		/**
		 * 指令类型 - 系统指令 - 本地玩家进入房间
		 */
		public static var SYSTEM_CODE_LOCAL_JOIN:int = 0x390A;
		/**
		 * 指令类型 - 系统指令 - 本地玩家退出房间
		 */
		public static var SYSTEM_CODE_LOCAL_LEAVE:int = 0x390B;
		/**
		 * 指令类型 - 系统指令 - 大厅添加房间
		 */
		public static var SYSTEM_CODE_ROOM_ADDED:int = 0x390C;
		/**
		 * 指令类型 - 系统指令 - 大厅移除房间
		 */
		public static var SYSTEM_CODE_ROOM_REMOVED:int = 0x390D;
		/**
		 * 指令类型 - 系统指令 - 大厅房间状态变化
		 */
		public static var SYSTEM_CODE_ROOM_CHANGED:int = 0x390E;
		/**
		 * 指令类型 - 系统指令 - 断线后重新连接
		 */
		public static var SYSTEM_CODE_RECONNECT:int = 0x390F;
		/**
		 * 指令类型 - 系统指令 - 断线后重新进入游戏
		 */
		public static var SYSTEM_CODE_RECONNECT_GAME:int = 0x3910;
		/**
		 * 指令类型 - 系统指令 - 用户注册
		 */
		public static var SYSTEM_CODE_USER_REGIST:int = 0x3911;
		/**
		 * 指令类型 - 系统指令 - 排行榜信息
		 */
		public static var SYSTEM_CODE_RANKING_LIST:int = 0x3912;
		/**
		 * 指令类型 - 系统指令 - 用户排名信息
		 */
		public static var SYSTEM_CODE_USER_RANK:int = 0x3913;
		/**
		 * 指令类型 - 系统指令 - 读取代码
		 */
		public static var SYSTEM_CODE_CODE_DETAIL:int = 0x3914;
		/**
		 * 指令类型 - 系统指令 - 读取本地用户信息
		 */
		public static var SYSTEM_CODE_USER_INFO:int = 0x3915
		/**
		 * 代码类型 - 游戏指令 - 玩家加入房间的检查
		 */
		public static var SYSTEM_CODE_JOIN_CHECK:int = 0x3916;
		/**
		 * 代码类型 - 游戏指令 - 用户读取房间信息的指令
		 */
		public static var SYSTEM_CODE_LOAD_ROOM_INFO:int = 0x3917;
		/**
		 * 代码类型 - 游戏指令 - 刷新用户和房间的基本信息
		 */
		public static var SYSTEM_CODE_USER_ROOM_INFO:int = 0x3918;
		/**
		 * 代码类型 - 游戏指令 - 刷新房间里的用户信息
		 */
		public static var SYSTEM_CODE_USER_LIST_INFO:int = 0x3919;
		/**
		 * 代码类型 - 游戏指令 - 刷新大厅中用户的信息
		 */
		public static var SYSTEM_CODE_HALL_REFRESH_USER:int = 0x391A;
		/**
		 * 代码类型 - 游戏指令 - 刷新房间中用户的信息
		 */
		public static var SYSTEM_CODE_ROOM_REFRESH_USER:int = 0x391B;
		/**
		 * 代码类型 - 游戏指令 - 房间中的用户加入游戏
		 */
		public static var SYSTEM_CODE_ROOM_JOIN_PLAY:int = 0x391C;
		/**
		 * 代码类型 - 游戏指令 - 房间中的用户离开游戏
		 */
		public static var SYSTEM_CODE_ROOM_LEAVE_PLAY:int = 0x391D;
		/**
		 * 代码类型 - 游戏指令 - 用户进入房间
		 */
		public static var SYSTEM_CODE_ROOM_JOIN:int = 0x391E;
		/**
		 * 代码类型 - 游戏指令 - 用户离开房间
		 */
		public static var SYSTEM_CODE_ROOM_LEAVE:int = 0x391F;
		/**
		 * 代码类型 - 游戏指令 - 用户按键状态变化
		 */
		public static var SYSTEM_CODE_ROOM_USER_BUTTON:int = 0x3920;
		/**
		 * 代码类型 - 游戏指令 - 读取游戏设置
		 */
		public static var SYSTEM_CODE_LOAD_CONFIG:int = 0x3921;
		/**
		 * 代码类型 - 游戏指令 - 用户离开房间的请求
		 */
		public static var SYSTEM_CODE_ROOM_LEAVE_REQUEST:int = 0x3922;
		/**
		 * 代码类型 - 游戏指令 - 用户准备游戏
		 */
		public static var SYSTEM_CODE_USER_READY:int = 0x3923;
		/**
		 * 代码类型 - 游戏指令 - 用户开始游戏
		 */
		public static var SYSTEM_CODE_USER_START:int = 0x3924;
		/**
		 * 代码类型 - 游戏指令 - 检查大厅公告
		 */
		public static var SYSTEM_CODE_HALL_NOTICE:int = 0x3925;
		/**
		 * 代码类型 - 游戏指令 - 发送房间邀请通知
		 */
		public static var SYSTEM_CODE_ROOM_INVITE_NOTIFY:int = 0x3926;
	
		/**
		 * 代码类型 - 游戏指令 - 进入游戏
		 */
		public static var GAME_CODE_JOIN:int = 0x2000;
		/**
		 * 代码类型 - 游戏指令 - 开始游戏
		 */
		public static var GAME_CODE_START:int = 0x2001;
		/**
		 * 代码类型 - 游戏指令 - 读取所有玩家信息
		 */
		public static var GAME_CODE_LOAD_PLAYER:int = 0x2006;
		/**
		 * 代码类型 - 游戏指令 - 设置游戏起始信息
		 */
		public static var GAME_CODE_SETUP:int = 0x2007;
		/**
		 * 代码类型 - 游戏指令 - 读取本地玩家信息
		 */
		public static var GAME_CODE_LOCAL_PLAYER:int = 0x2008;
		/**
		 * 指令类型 - 系统指令 - 断线后重新连接游戏
		 */
		public static var GAME_CODE_RECONNECT_GAME:int = 0x2009;
		/**
		 * 代码类型 - 游戏指令 - 游戏结束
		 */
		public static var GAME_CODE_END:int = 0x2015;
		/**
		 * 代码类型 - 游戏指令 - 结束时的分数统计
		 */
		public static var GAME_CODE_VP_BOARD:int = 0x2016;
		/**
		 * 代码类型 - 游戏指令 - 离开游戏
		 */
		public static var GAME_CODE_LEAVE:int = 0x2017;
		/**
		 * 代码类型 - 游戏指令 - 玩家被移除游戏
		 */
		public static var GAME_CODE_REMOVE_PLAYER:int = 0x2018;
		/**
		 * 代码类型 - 游戏指令 - 玩家准备游戏
		 */
		public static var GAME_CODE_PLAYER_READY:int = 0x2019;
		/**
		 * 代码类型 - 游戏指令 - 观战游戏
		 */
		public static var GAME_CODE_AUDIENCE:int = 0x2020;
		/**
		 * 代码类型 - 游戏指令 - 退出观战
		 */
		public static var GAME_CODE_EXIT_AUDIENCE:int = 0x2021;
		/**
		 * 代码类型 - 游戏指令 - 刷新游戏进行中的信息
		 */
		public static var GAME_CODE_PLAYING_INFO:int = 0x2022;
		/**
		 * 代码类型 - 游戏指令 - 刷新玩家状态
		 */
		public static var GAME_CODE_PLAYER_STATE:int = 0x2023;
		/**
		 * 代码类型 - 游戏指令 - 刷新玩家游戏进行状态
		 */
		public static var GAME_CODE_PLAYING_STATE:int = 0x2024;
		
		/**
		 * 代码类型 - 游戏指令 - 刷新所有当前信息
		 */
		public static var GAME_CODE_REFRESH_ALL:int = 0x2100;
		/**
		 * 代码类型 - 游戏指令 - 监听器开始监听
		 */
		public static var GAME_CODE_START_LISTEN:int = 0x2200;
		/**
		 * 代码类型 - 游戏指令 - 玩家输入回应
		 */
		public static var GAME_CODE_PLAYER_RESPONSED:int = 0x2201;
		/**
		 * 代码类型 - 游戏指令 - 阶段开始
		 */
		public static var GAME_CODE_PHASE_START:int = 0x2202;
		/**
		 * 代码类型 - 游戏指令 - 阶段结束
		 */
		public static var GAME_CODE_PHASE_END:int = 0x2203;
		/**
		 * 代码类型 - 游戏指令 - 刷新游戏时间
		 */
		public static var GAME_CODE_GAME_TIME:int = 0x2204;
		/**
		 * 代码类型 - 游戏指令 - 读取游戏设置
		 */
		public static var GAME_CODE_LOAD_CONFIG:int = 0x2205;
		/**
		 * 代码类型 - 游戏指令 - 设置游戏
		 */
		public static var GAME_CODE_SET_CONFIG:int = 0x2206;
		/**
		 * 代码类型 - 游戏指令 - 发送战报
		 */
		public static var GAME_CODE_LOAD_REPORT:int = 0x220A;
		/**
		 * 代码类型 - 游戏指令 - 发送战报信息
		 */
		public static var GAME_CODE_REPORT_MESSAGE:int = 0x220B;
		/**
		 * 代码类型 - 游戏指令 - 刷新当前提示信息
		 */
		public static var GAME_CODE_REFRESH_MSG:int = 0x220C;
		/**
		 * 代码类型 - 游戏指令 - 游戏信息提示
		 */
		public static var GAME_CODE_TIP_ALERT:int = 0x220D;
		/**
		 * 代码类型 - 游戏指令 - 游戏的简单指令
		 */
		public static var GAME_CODE_SIMPLE_CMD:int = 0x220E;
		/**
		 * 代码类型 - 游戏指令 - 游戏的动画指令
		 */
		public static var GAME_CODE_ANIM_CMD:int = 0x220F;
		
		/**
		 * 代码类型 - 聊天指令 - 聊天信息
		 */
		public static var CHAT_CODE_MESSAGE:int = 0x1000;
	}
}