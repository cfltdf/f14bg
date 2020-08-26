package com.f14.F14bgGame.bg.manager
{
	import com.f14.F14bgGame.bg.ui.F14GameModule;
	import com.f14.f14bg.manager.ImageManager;
	
	public class DefaultManagerUtil
	{
		public static var module:F14GameModule;
		public static var imageManager:ImageManager;
		public static var effectManager:EffectManager;
		public static var tooltipManager:TooltipManager;
		public static var gameManager:GameManager;
		public static var stateManager:GameStateManager;
		public static var alertManager:AlertManager;
		public static var animManager:AnimManager;
		
		/*public static function init():void{
			effectManager = new EffectManager();
			tooltipManager = new TooltipManager();
			gameManager = new GameManager();
			stateManager = new GameStateManager();
		}*/

	}
}