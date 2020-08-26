package com.f14.F14bgClient.hall
{
	import com.f14.F14bgClient.hall.manager.HallActionManager;
	import com.f14.F14bgClient.hall.manager.HallStateManager;
	
	public class HallUtil
	{
		public static var module:HallModule;
		public static var actionManager:HallActionManager;
		public static var stateManager:HallStateManager;
		
		public static function init():void{
			actionManager = new HallActionManager();
			stateManager = new HallStateManager();
		}
		
	}
}