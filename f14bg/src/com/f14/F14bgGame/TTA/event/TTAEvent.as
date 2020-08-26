package com.f14.F14bgGame.TTA.event
{
	import com.f14.F14bgGame.TTA.components.TTACard;
	
	import flash.events.Event;

	/**
	 * TTA的事件
	 */
	public class TTAEvent extends Event
	{
		public function TTAEvent(type:String)
		{
			super(type, false, false);
		}
		
		/**
		 * 拍卖时调整出价的事件
		 */
		public static var AUCTION_ADJUST:String = "AUCTION_ADJUST";
		
		/**
		 * 使用卡牌的事件
		 */
		public static var ACTIVE_CARD:String = "ACTIVE_CARD";
		
		/**
		 * 使用卡牌的事件
		 */
		public static var CLICK_PLAYER_INFO:String = "CLICK_PLAYER_INFO";
		
		public var card:TTACard;
	}
}