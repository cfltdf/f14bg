package com.f14.F14bgGame.Innovation.consts
{
	public class InnoGameCmd
	{
		/**
		 * 代码类型 - 游戏指令 - 刷新摸牌堆
		 */
		public static var GAME_CODE_DRAW_DECK_INFO:int = 0x4000;
		
		
		/**
		 * 代码类型 - 游戏指令 - 玩家得到手牌
		 */
		public static var GAME_CODE_ADD_HANDS:int = 0x4400;
		/**
		 * 代码类型 - 游戏指令 - 刷新玩家的得分
		 */
		public static var GAME_CODE_SCORE_INFO:int = 0x4401;
		/**
		 * 代码类型 - 游戏指令 - 开始时选择起始卡牌
		 */
		public static var GAME_CODE_SETUP_CARD:int = 0x4402;
		/**
		 * 代码类型 - 游戏指令 - 玩家失去手牌
		 */
		public static var GAME_CODE_REMOVE_HANDS:int = 0x4403;
		/**
		 * 代码类型 - 游戏指令 - 玩家合并卡牌
		 */
		public static var GAME_CODE_CARD_STACK:int = 0x4404;
		/**
		 * 代码类型 - 游戏指令 - 玩家回合
		 */
		public static var GAME_CODE_ROUND:int = 0x4405;
		/**
		 * 代码类型 - 游戏指令 - 刷新玩家的行动点数
		 */
		public static var GAME_CODE_REFRESH_AP:int = 0x4406;
		/**
		 * 代码类型 - 游戏指令 - 刷新成就牌堆的信息
		 */
		public static var GAME_CODE_ACHIEVE_INFO:int = 0x4407;
		/**
		 * 代码类型 - 游戏指令 - 玩家得到成就牌
		 */
		public static var GAME_CODE_ADD_ACHIEVE:int = 0x4408;
		/**
		 * 代码类型 - 游戏指令 - 摸牌的行动阶段
		 */
		public static var GAME_CODE_DRAW_CARD_ACTION:int = 0x4409;
		/**
		 * 代码类型 - 游戏指令 - 选择手牌的阶段
		 */
		public static var GAME_CODE_CHOOSE_CARD:int = 0x440A;
		/**
		 * 代码类型 - 游戏指令 - 展开牌堆的阶段
		 */
		public static var GAME_CODE_SPLAY_CARD:int = 0x440B;
		/**
		 * 代码类型 - 游戏指令 - 选择牌堆的阶段
		 */
		public static var GAME_CODE_CHOOSE_STACK:int = 0x440C;
		/**
		 * 代码类型 - 游戏指令 - 刷新玩家的符号信息
		 */
		public static var GAME_CODE_ICON_INFO:int = 0x440D;
		/**
		 * 代码类型 - 游戏指令 - 选择手牌的阶段(多选)
		 */
		public static var GAME_CODE_CHOOSE_CARDS:int = 0x440E;
		/**
		 * 代码类型 - 游戏指令 - 通用询问窗口
		 */
		public static var GAME_CODE_COMMON_CONFIRM:int = 0x440F;
		/**
		 * 代码类型 - 游戏指令 - 选择玩家
		 */
		public static var GAME_CODE_CHOOSE_PLAYER:int = 0x4410;
		/**
		 * 代码类型 - 游戏指令 - 选择特定的牌
		 */
		public static var GAME_CODE_CHOOSE_SPECIFIC_CARD:int = 0x4411;
		/**
		 * 代码类型 - 游戏指令 - 移除特定的牌
		 */
		public static var GAME_CODE_REMOVE_SPECIFIC_CARD:int = 0x4412;
		/**
		 * 代码类型 - 游戏指令 - 选择分数牌的阶段
		 */
		public static var GAME_CODE_CHOOSE_SCORE_CARD:int = 0x4413;
		/**
		 * 代码类型 - 游戏指令 - 选择分数牌的阶段(多选)
		 */
		public static var GAME_CODE_CHOOSE_SCORE_CARDS:int = 0x4414;
		/**
		 * 代码类型 - 游戏指令 - 移除选择中的分数牌
		 */
		public static var GAME_CODE_REMOVE_CHOOSE_SCORE_CARD:int = 0x4415;
		/**
		 * 代码类型 - 游戏指令 - 玩家得到计分牌
		 */
		public static var GAME_CODE_ADD_SCORES:int = 0x4416;
		/**
		 * 代码类型 - 游戏指令 - 玩家移除计分牌
		 */
		public static var GAME_CODE_REMOVE_SCORES:int = 0x4417;
		/**
		 * 代码类型 - 游戏指令 - 版图移除成就牌
		 */
		public static var GAME_CODE_REMOVE_ACHIEVE_CARD:int = 0x4418;
		/**
		 * 代码类型 - 游戏指令 - 版图移除特殊成就牌
		 */
		public static var GAME_CODE_REMOVE_SPECIAL_ACHIEVE_CARD:int = 0x4419;

		/**
		 * 代码类型 - 游戏指令 - #069-出版 监听器
		 */
		public static var GAME_CODE_069:int = 0x4450;
		/**
		 * 代码类型 - 游戏指令 - #074-进化 监听器
		 */
		public static var GAME_CODE_074:int = 0x4451;
		/**
		 * 代码类型 - 游戏指令 - #076-大众传媒 监听器
		 */
		public static var GAME_CODE_076:int = 0x4452;
		/**
		 * 代码类型 - 游戏指令 - #083-经验主义 监听器
		 */
		public static var GAME_CODE_083:int = 0x4453;
		/**
		 * 代码类型 - 游戏指令 - #026-光学 监听器
		 */
		public static var GAME_CODE_026:int = 0x4454;
		/**
		 * 代码类型 - 游戏指令 - #081-火箭技术 监听器
		 */
		public static var GAME_CODE_081:int = 0x4455;
	}
}