package com.f14.F14bgGame.TS
{
	import com.f14.F14bgGame.TS.manager.TSActionManager;
	import com.f14.F14bgGame.TS.manager.TSEffectManager;
	import com.f14.F14bgGame.TS.manager.TSImageManager;
	import com.f14.F14bgGame.TS.manager.TSResourceManager;
	import com.f14.F14bgGame.TS.manager.TSStateManager;
	import com.f14.F14bgGame.TS.player.TSPlayer;
	import com.f14.F14bgGame.TS.ui.TSMainBoard;
	import com.f14.F14bgGame.bg.manager.AlertManager;
	import com.f14.F14bgGame.bg.manager.AnimManager;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.manager.GameManager;
	import com.f14.F14bgGame.bg.manager.TooltipManager;
	import com.f14.f14bg.manager.SoundManager;
	
	public class TSUtil
	{
		public static var module:TS;
		public static var effectManager:TSEffectManager;
		public static var imageManager:TSImageManager;
		public static var resourceManager:TSResourceManager;
		public static var soundManager:SoundManager;
		public static var tooltipManager:TooltipManager;
		public static var gameManager:GameManager;
		public static var actionManager:TSActionManager;
		public static var stateManager:TSStateManager;
		public static var alertManager:AlertManager;
		public static var animManager:AnimManager;
		
		/**
		 * 初始化
		 */
		public static function init():void{
			effectManager = new TSEffectManager();
			soundManager = new SoundManager();
			animManager = new AnimManager();
			
			tooltipManager = new TooltipManager();
			stateManager = new TSStateManager();
			actionManager = new TSActionManager();
			
			gameManager = new GameManager();
			alertManager = new AlertManager();
			
			DefaultManagerUtil.effectManager = effectManager;
			DefaultManagerUtil.gameManager = gameManager;
			DefaultManagerUtil.stateManager = stateManager;
			DefaultManagerUtil.tooltipManager = tooltipManager;
			DefaultManagerUtil.alertManager = alertManager;
			DefaultManagerUtil.animManager = animManager;
		}
		
		public static function loadResources():void{
			resourceManager = new TSResourceManager();
			resourceManager.loadResourceString();
			
			imageManager = new TSImageManager();
			DefaultManagerUtil.imageManager = imageManager;
			imageManager.loadImages();
		}
		
		public static function initGameModule(module:TS):void{
			TSUtil.module = module;
			DefaultManagerUtil.module = module;
		}
		
		/**
		 * 取得主游戏面板
		 */
		public static function get mainBoard():TSMainBoard{
			return module.tsMainBoard;
		}
		
		/**
		 * 取得本地玩家
		 */
		public static function getLocalPlayer():TSPlayer{
			return gameManager.localPlayer as TSPlayer;
		}
		
		/**
		 * 取得指定位置的玩家
		 */
		public static function getPlayer(position:int):TSPlayer{
			return gameManager.players[position];
		}
		
		/**
		 * 取得指定Id的玩家
		 */
		public static function getPlayerById(userId:String):TSPlayer{
			return gameManager.getPlayerById(userId) as TSPlayer;
		}
		
	}
}