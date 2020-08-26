package com.f14.f14bg.event
{
	import flash.events.Event;
	
	public class BgEvent extends Event
	{
		/**
		 * 事件 - 玩家状态变换
		 */
		public static var PLAYER_STATE_CHANGE:String = "PLAYER_STATE_CHANGE";
		
		/**
		 * 事件 - 玩家游戏进行的状态变换
		 */
		public static var PLAYING_STATE_CHANGE:String = "PLAYING_STATE_CHANGE";
		
		public function BgEvent(type:String){
			super(type);
		}
		
		public var param:Object = new Object();

	}
}