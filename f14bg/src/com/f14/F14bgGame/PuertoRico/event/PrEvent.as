package com.f14.F14bgGame.PuertoRico.event
{
	import com.f14.F14bgGame.PuertoRico.components.PartTile;
	
	import flash.events.Event;
	
	public class PrEvent extends Event
	{
		/**
		 * 点击移民事件
		 */
		public static var COLONIST_CLICK:String = "COLONIST_CLICK";
		/**
		 * 点击资源事件
		 */
		public static var RESOURCE_CLICK:String = "RESOURCE_CLICK";
		/**
		 * 点击货船事件
		 */
		public static var SHIP_CLICK:String = "SHIP_CLICK";
		
		public function PrEvent(type:String){
			super(type);
		}
		
		public var part:PartTile;

	}
}