package com.f14.F14bgGame.Tichu.consts
{
	public class TichuGameCmd
	{
		/**
		 * 代码类型 - 游戏指令 - 发送游戏基本信息
		 */
		public static var GAME_CODE_BASE_INFO:int = 0x4000;
		/**
		 * 代码类型 - 游戏指令 - 刷新玩家的基本信息
		 */
		public static var GAME_CODE_PLAYER_INFO:int = 0x4001;
		/**
		 * 代码类型 - 游戏指令 - 刷新玩家的手牌
		 */
		public static var GAME_CODE_PLAYER_HAND:int = 0x4002;
		/**
		 * 代码类型 - 游戏指令 - 刷新玩家打出的牌
		 */
		public static var GAME_CODE_PLAYER_PLAY_CARD:int = 0x4003;
		/**
		 * 代码类型 - 游戏指令 - 刷新玩家的按键信息
		 */
		public static var GAME_CODE_PLAYER_BUTTON:int = 0x4004;
		
		/**
		 * 代码类型 - 游戏指令 - 叫大地主阶段
		 */
		public static var GAME_CODE_BIG_TICHU_PHASE:int = 0x4400;
		/**
		 * 代码类型 - 游戏指令 - 换牌阶段
		 */
		public static var GAME_CODE_REGROUP_PHASE:int = 0x4401;
		/**
		 * 代码类型 - 游戏指令 - 正常回合阶段
		 */
		public static var GAME_CODE_ROUND_PHASE:int = 0x4402;
		/**
		 * 代码类型 - 游戏指令 - 选择给对方分数
		 */
		public static var GAME_CODE_GIVE_SCORE:int = 0x4403;
		/**
		 * 代码类型 - 游戏指令 - 玩家许愿
		 */
		public static var GAME_CODE_WISH_POINT:int = 0x4404;
		/**
		 * 代码类型 - 游戏指令 - 回合确认阶段
		 */
		public static var GAME_CODE_ROUND_RESULT:int = 0x4405;
		/**
		 * 代码类型 - 游戏指令 - 确认换牌阶段
		 */
		public static var GAME_CODE_CONFIRM_EXCHANGE:int = 0x4406;
		/**
		 * 代码类型 - 游戏指令 - 选择炸弹的阶段
		 */
		public static var GAME_CODE_BOMB_PHASE:int = 0x4407;

	}
}