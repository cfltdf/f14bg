package com.f14.F14bgGame.TTA.consts
{
	public class TTAGameCmd
	{
		/**
		 * 代码类型 - 游戏指令 - 发送游戏基本信息
		 */
		public static var GAME_CODE_BASE_INFO:int = 0x4000;
		/**
		 * 代码类型 - 游戏指令 - 文明牌序列
		 */
		public static var GAME_CODE_CARD_ROW:int = 0x4001;
		/**
		 * 代码类型 - 游戏指令 - 玩家的文明添加卡牌
		 */
		public static var GAME_CODE_ADD_CARD:int = 0x4002;
		/**
		 * 代码类型 - 游戏指令 - 玩家文明的基本属性
		 */
		public static var GAME_CODE_CIVILIZATION_INFO:int = 0x4003;
		/**
		 * 代码类型 - 游戏指令 - 玩家卡牌标志物调整
		 */
		public static var GAME_CODE_CARD_TOKEN:int = 0x4004;
		/**
		 * 代码类型 - 游戏指令 - 玩家得到手牌
		 */
		public static var GAME_CODE_ADD_HAND:int = 0x4005;
		/**
		 * 代码类型 - 游戏指令 - 玩家失去手牌
		 */
		public static var GAME_CODE_REMOVE_HAND:int = 0x4006;
		/**
		 * 代码类型 - 游戏指令 - 卡牌序列失去牌
		 */
		public static var GAME_CODE_REMOVE_CARDROW:int = 0x4007;
		/**
		 * 代码类型 - 游戏指令 - 刷新玩家面板的标志物
		 */
		public static var GAME_CODE_BOARD_TOKEN:int = 0x4008;
		/**
		 * 代码类型 - 游戏指令 - 玩家拿取奇迹牌
		 */
		public static var GAME_CODE_GET_WONDER:int = 0x4009;
		/**
		 * 代码类型 - 游戏指令 - 玩家行动的请求
		 */
		public static var GAME_CODE_ACTION_REQUEST:int = 0x400A;
		/**
		 * 代码类型 - 游戏指令 - 玩家执行建造
		 */
		public static var GAME_CODE_BUILD:int = 0x400B;
		/**
		 * 代码类型 - 游戏指令 - 玩家失去已打出的牌
		 */
		public static var GAME_CODE_REMOVE_CARD:int = 0x400C;
		/**
		 * 代码类型 - 游戏指令 - 玩家奇迹建造完成
		 */
		public static var GAME_CODE_WONDER_COMPLETE:int = 0x400D;
		/**
		 * 代码类型 - 游戏指令 - 玩家执行摧毁建筑
		 */
		public static var GAME_CODE_DESTORY:int = 0x400E;
		/**
		 * 代码类型 - 游戏指令 - 玩家改变政府
		 */
		public static var GAME_CODE_CHANGE_GOVERMENT:int = 0x400F;
		/**
		 * 代码类型 - 游戏指令 - 玩家请求行动结束,关闭窗口
		 */
		public static var GAME_CODE_REQUEST_END:int = 0x4010;
		/**
		 * 代码类型 - 游戏指令 - 玩家执行升级
		 */
		public static var GAME_CODE_UPGRADE:int = 0x4011;
		/**
		 * 代码类型 - 游戏指令 - 刷新奖励牌堆
		 */
		public static var GAME_CODE_BONUS_CARD:int = 0x4012;
		/**
		 * 代码类型 - 游戏指令 - 刷新可使用的卡牌
		 */
		public static var GAME_CODE_ACTIVABLE_CARD:int = 0x4013;
		/**
		 * 代码类型 - 游戏指令 - 选择玩家
		 */
		public static var GAME_CODE_CHOOSE_PLAYER:int = 0x4014;
		/**
		 * 代码类型 - 游戏指令 - 添加持续效果的卡牌
		 */
		public static var GAME_CODE_OVERTIME_CARD:int = 0x4015;
		
		
		/**
		 * 代码类型 - 游戏指令 - 第一回合选牌
		 */
		public static var GAME_CODE_FIRST_ROUND:int = 0x4400;
		/**
		 * 代码类型 - 游戏指令 - 玩家回合
		 */
		public static var GAME_CODE_ROUND:int = 0x4401;
		/**
		 * 代码类型 - 游戏指令 - 玩家政治行动
		 */
		public static var GAME_CODE_POLITICAL_ACTION:int = 0x4402;
		/**
		 * 代码类型 - 游戏指令 - 玩家弃军事牌
		 */
		public static var GAME_CODE_DISCARD_MILITARY:int = 0x4403;
		/**
		 * 代码类型 - 游戏指令 - 玩家变换回合中的阶段
		 */
		public static var GAME_CODE_CHANGE_STEP:int = 0x4404;
		/**
		 * 代码类型 - 游戏指令 - 玩家选择资源
		 */
		public static var GAME_CODE_CHOOSE_RESOURCE:int = 0x4405;
		/**
		 * 代码类型 - 游戏指令 - 玩家选择建造的事件
		 */
		public static var GAME_CODE_EVENT_BUILD:int = 0x4406;
		/**
		 * 代码类型 - 游戏指令 - 玩家失去人口的事件
		 */
		public static var GAME_CODE_EVENT_LOSE_POP:int = 0x4407;
		/**
		 * 代码类型 - 游戏指令 - 玩家选择摧毁的事件
		 */
		public static var GAME_CODE_EVENT_DESTORY:int = 0x4408;
		/**
		 * 代码类型 - 游戏指令 - 玩家选择殖民地的事件
		 */
		public static var GAME_CODE_EVENT_COLONY:int = 0x4409;
		/**
		 * 代码类型 - 游戏指令 - 玩家选择拿牌的事件
		 */
		public static var GAME_CODE_EVENT_TAKE_CARD:int = 0x440A;
		/**
		 * 代码类型 - 游戏指令 - 玩家选择翻转奇迹的事件
		 */
		public static var GAME_CODE_EVENT_FLIP_WONDER:int = 0x440B;
		/**
		 * 代码类型 - 游戏指令 - 玩家摧毁其他玩家建筑的事件
		 */
		public static var GAME_CODE_EVENT_DESTORY_OTHERS:int = 0x440C;
		
		/**
		 * 代码类型 - 游戏指令 - 玩家拍卖殖民地
		 */
		public static var GAME_CODE_AUCTION_TERRITORY:int = 0x440D;
		/**
		 * 代码类型 - 游戏指令 - 玩家侵略/战争时选择部队
		 */
		public static var GAME_CODE_WAR:int = 0x440E;
		/**
		 * 代码类型 - 游戏指令 - 玩家签订条约
		 */
		public static var GAME_CODE_PACT:int = 0x440F;

	}
}