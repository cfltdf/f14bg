package com.f14.f14bg.event
{
	import flash.events.Event;
	
	public class StateChangeEvent extends Event
	{
		/**
		 * 事件 - 玩家状态变换
		 */
		public static var EVENT:String = "STATE_CHANGE_EVENT";
		
		public function StateChangeEvent(){
			super(EVENT);
		}
		
		public var param:Object = new Object();

	}
}