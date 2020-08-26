package com.f14.F14bgGame.RFTG.consts
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
		 * 代码类型 - 系统指令 - 装载所有卡牌信息
		 */
		public static var SYSTEM_CODE_INIT_CARD:int = 0x3901;
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
		 * 代码类型 - 游戏指令 - 进入游戏
		 */
		public static var GAME_CODE_JOIN:int = 0x2000;
		/**
		 * 代码类型 - 游戏指令 - 开始游戏
		 */
		public static var GAME_CODE_START:int = 0x2001;
		/**
		 * 代码类型 - 游戏指令 - 选择行动
		 */
		public static var GAME_CODE_CHOOSE_ACTION:int = 0x2002;
		/**
		 * 代码类型 - 游戏指令 - 显示所有玩家选择的行动
		 */
		public static var GAME_CODE_SHOW_ACTION:int = 0x2003;
		/**
		 * 代码类型 - 游戏指令 - 游戏开始时玩家弃牌
		 */
		public static var GAME_CODE_STARTING_DISCARD:int = 0x2004;
		/**
		 * 代码类型 - 游戏指令 - 回合结束时弃牌
		 */
		public static var GAME_CODE_ROUND_DISCARD:int = 0x2005;
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
		 * 代码类型 - 游戏指令 - 探索阶段指令
		 */
		public static var GAME_CODE_EXPLORE:int = 0x2010;
		/**
		 * 代码类型 - 游戏指令 - 开发阶段指令
		 */
		public static var GAME_CODE_DEVELOP:int = 0x2011;
		/**
		 * 代码类型 - 游戏指令 - 扩张阶段指令
		 */
		public static var GAME_CODE_SETTLE:int = 0x2012;
		/**
		 * 代码类型 - 游戏指令 - 消费阶段指令
		 */
		public static var GAME_CODE_CONSUME:int = 0x2013;
		/**
		 * 代码类型 - 游戏指令 - 生产阶段指令
		 */
		public static var GAME_CODE_PRODUCE:int = 0x2014;
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
		 * 代码类型 - 游戏指令 - 刷新所有当前信息
		 */
		public static var GAME_CODE_REFRESH_ALL:int = 0x2100;
		/**
		 * 代码类型 - 游戏指令 - 摸牌
		 */
		public static var GAME_CODE_DRAW_CARD:int = 0x2101;
		/**
		 * 代码类型 - 游戏指令 - 弃牌
		 */
		public static var GAME_CODE_DISCARD:int = 0x2102;
		/**
		 * 代码类型 - 游戏指令 - 从手牌中打牌
		 */
		public static var GAME_CODE_PLAY_CARD:int = 0x2103;
		/**
		 * 代码类型 - 游戏指令 - 直接打出牌
		 */
		public static var GAME_CODE_DIRECT_PLAY_CARD:int = 0x2104;
		/**
		 * 代码类型 - 游戏指令 - 弃掉货物
		 */
		public static var GAME_CODE_DISCARD_GOOD:int = 0x2105;
		/**
		 * 代码类型 - 游戏指令 - 卡牌生效
		 */
		public static var GAME_CODE_CARD_EFFECT:int = 0x2106;
		/**
		 * 代码类型 - 游戏指令 - 弃掉已打出的卡牌
		 */
		public static var GAME_CODE_DISCARD_PLAYED_CARD:int = 0x2107;
		/**
		 * 代码类型 - 游戏指令 - 使用卡牌
		 */
		public static var GAME_CODE_USE_CARD:int = 0x2108;
		/**
		 * 代码类型 - 游戏指令 - 生产货物
		 */
		public static var GAME_CODE_PRODUCE_GOOD:int = 0x2109;
		/**
		 * 代码类型 - 游戏指令 - 可以主动使用的卡牌列表
		 */
		public static var GAME_CODE_ACTIVE_CARD_LIST:int = 0x210A;
		/**
		 * 代码类型 - 游戏指令 - 玩家得到VP
		 */
		public static var GAME_CODE_GET_VP:int = 0x210B;
		/**
		 * 代码类型 - 游戏指令 - 刷新牌堆数量
		 */
		public static var GAME_CODE_REFRESH_DECK:int = 0x210C;
		/**
		 * 代码类型 - 游戏指令 - 公共资源得到目标
		 */
		public static var GAME_CODE_SUPPLY_GET_GOAL:int = 0x210D;
		/**
		 * 代码类型 - 游戏指令 - 公共资源失去目标
		 */
		public static var GAME_CODE_SUPPLY_LOST_GOAL:int = 0x210E;
		/**
		 * 代码类型 - 游戏指令 - 玩家得到目标
		 */
		public static var GAME_CODE_PLAYER_GET_GOAL:int = 0x210F;
		/**
		 * 代码类型 - 游戏指令 - 玩家失去目标
		 */
		public static var GAME_CODE_PLAYER_LOST_GOAL:int = 0x2110;
		/**
		 * 代码类型 - 游戏指令 - 刷新公共区的目标
		 */
		public static var GAME_CODE_SUPPLY_REFRESH_GOAL:int = 0x2111;
		/**
		 * 代码类型 - 游戏指令 - 刷新玩家的目标
		 */
		public static var GAME_CODE_PLAYER_REFRESH_GOAL:int = 0x2112;
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
		 * 代码类型 - 游戏指令 - 选择起始星球
		 */
		public static var GAME_CODE_STARTING_WORLD:int = 0x2207;
		/**
		 * 代码类型 - 游戏指令 - 赌博
		 */
		public static var GAME_CODE_GAMBLE:int = 0x2208;
	}
}