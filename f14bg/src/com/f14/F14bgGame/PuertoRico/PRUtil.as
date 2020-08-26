package com.f14.F14bgGame.PuertoRico
{
	import com.f14.F14bgGame.PuertoRico.manager.PrActionManager;
	import com.f14.F14bgGame.PuertoRico.manager.PrEffectManager;
	import com.f14.F14bgGame.PuertoRico.manager.PrGameManager;
	import com.f14.F14bgGame.PuertoRico.manager.PrImageManager;
	import com.f14.F14bgGame.PuertoRico.manager.PrResourceManager;
	import com.f14.F14bgGame.PuertoRico.manager.PrSoundManager;
	import com.f14.F14bgGame.PuertoRico.manager.PrStateManager;
	import com.f14.F14bgGame.PuertoRico.player.PrPlayer;
	import com.f14.F14bgGame.PuertoRico.ui.PrMainBoard;
	import com.f14.F14bgGame.bg.manager.AlertManager;
	import com.f14.F14bgGame.bg.manager.AnimManager;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.manager.TooltipManager;
	
	public class PRUtil
	{
		public static var module:PuertoRico;
		public static var effectManager:PrEffectManager;
		public static var imageManager:PrImageManager;
		public static var resourceManager:PrResourceManager;
		public static var soundManager:PrSoundManager;
		public static var tooltipManager:TooltipManager;
		public static var gameManager:PrGameManager;
		public static var actionManager:PrActionManager;
		public static var stateManager:PrStateManager;
		public static var alertManager:AlertManager;
		public static var animManager:AnimManager;
		
		/**
		 * 初始化
		 */
		public static function init():void{
			effectManager = new PrEffectManager();
			soundManager = new PrSoundManager();
			animManager = new AnimManager();
			
			tooltipManager = new TooltipManager();
			stateManager = new PrStateManager();
			actionManager = new PrActionManager();
			
			gameManager = new PrGameManager();
			resourceManager = new PrResourceManager();
			alertManager = new AlertManager();
			
			DefaultManagerUtil.effectManager = effectManager;
			DefaultManagerUtil.gameManager = gameManager;
			DefaultManagerUtil.stateManager = stateManager;
			DefaultManagerUtil.tooltipManager = tooltipManager;
			DefaultManagerUtil.alertManager = alertManager;
			DefaultManagerUtil.animManager = animManager;
		}
		
		public static function initImageManager():void{
			imageManager = new PrImageManager();
			DefaultManagerUtil.imageManager = imageManager;
			imageManager.loadImages();
		}
		
		public static function initGameModule(module:PuertoRico):void{
			PRUtil.module = module;
			DefaultManagerUtil.module = module;
		}
		
		/**
		 * 取得主游戏面板
		 */
		public static function get mainBoard():PrMainBoard{
			return module.prMainBoard;
		}
		
		/**
		 * 取得本地玩家
		 */
		public static function getLocalPlayer():PrPlayer{
			return gameManager.localPlayer as PrPlayer;
		}
		
		/**
		 * 取得指定位置的玩家
		 */
		public static function getPlayer(position:int):PrPlayer{
			return gameManager.players[position];
		}
		
		/**
		 * 取得指定Id的玩家
		 */
		public static function getPlayerById(userId:String):PrPlayer{
			return gameManager.getPlayerById(userId) as PrPlayer;
		}

	}
}