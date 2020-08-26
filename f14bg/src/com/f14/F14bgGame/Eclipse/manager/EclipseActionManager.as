package com.f14.F14bgGame.Eclipse.manager
{
	import com.f14.F14bgGame.Eclipse.EclipseUtil;
	import com.f14.F14bgGame.bg.manager.GameActionManager;

	public class EclipseActionManager extends GameActionManager
	{
		public function EclipseActionManager()
		{
			super();
		}
		
		/**
		 * 玩家选择行动
		 */
		public function doAction(action:String):void{
			this.sendCurrentCommand(action);
		}
		
	}
}