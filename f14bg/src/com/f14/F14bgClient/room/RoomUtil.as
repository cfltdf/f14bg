package com.f14.F14bgClient.room
{
	import com.f14.F14bgClient.FlashHandler.CommonHandler;
	import com.f14.F14bgClient.room.manager.RoomActionManager;
	import com.f14.F14bgClient.room.manager.RoomSoundManager;
	import com.f14.F14bgClient.room.manager.RoomStateManager;
	import com.f14.F14bgGame.bg.ui.F14GameModule;
	
	public class RoomUtil
	{
		public static var application:F14room;
		public static var gameModule:F14GameModule;
		public static var stateManager:RoomStateManager;
		public static var actionManager:RoomActionManager;
		public static var soundManager:RoomSoundManager;
		
		public static var commonHandler:CommonHandler;
		
		public static function init():void{
			stateManager = new RoomStateManager();
			actionManager = new RoomActionManager();
			soundManager = new RoomSoundManager();
			
			commonHandler = new CommonHandler();
		}
		
	}
}