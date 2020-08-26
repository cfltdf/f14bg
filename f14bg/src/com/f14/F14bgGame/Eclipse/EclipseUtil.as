package com.f14.F14bgGame.Eclipse
{
	import com.f14.F14bgGame.Eclipse.manager.EclipseActionManager;
	import com.f14.F14bgGame.Eclipse.manager.EclipseAnimManager;
	import com.f14.F14bgGame.Eclipse.manager.EclipseEffectManager;
	import com.f14.F14bgGame.Eclipse.manager.EclipseImageManager;
	import com.f14.F14bgGame.Eclipse.manager.EclipseResourceManager;
	import com.f14.F14bgGame.Eclipse.manager.EclipseStateManager;
	import com.f14.F14bgGame.Eclipse.player.EclipsePlayer;
	import com.f14.F14bgGame.Eclipse.ui.EclipseMainBoard;
	import com.f14.F14bgGame.bg.manager.AlertManager;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.manager.GameManager;
	import com.f14.F14bgGame.bg.manager.TooltipManager;
	import com.f14.f14bg.manager.SoundManager;
	
	public class EclipseUtil
	{
		public static var module:Eclipse;
		public static var effectManager:EclipseEffectManager;
		public static var imageManager:EclipseImageManager;
		public static var resourceManager:EclipseResourceManager;
		public static var soundManager:SoundManager;
		public static var tooltipManager:TooltipManager;
		public static var gameManager:GameManager;
		public static var actionManager:EclipseActionManager;
		public static var stateManager:EclipseStateManager;
		public static var alertManager:AlertManager;
		public static var animManager:EclipseAnimManager;
		
		/**
		 * 初始化
		 */
		public static function init():void{
			effectManager = new EclipseEffectManager();
			soundManager = new SoundManager();
			
			tooltipManager = new TooltipManager();
			stateManager = new EclipseStateManager();
			actionManager = new EclipseActionManager();
			
			gameManager = new GameManager();
			alertManager = new AlertManager();
			animManager = new EclipseAnimManager();
			
			DefaultManagerUtil.effectManager = effectManager;
			DefaultManagerUtil.gameManager = gameManager;
			DefaultManagerUtil.stateManager = stateManager;
			DefaultManagerUtil.tooltipManager = tooltipManager;
			DefaultManagerUtil.alertManager = alertManager;
			DefaultManagerUtil.animManager = animManager;
		}
		
		public static function loadResources():void{
			resourceManager = new EclipseResourceManager();
			resourceManager.loadResourceString();
			
			imageManager = new EclipseImageManager();
			DefaultManagerUtil.imageManager = imageManager;
			imageManager.loadImages();
		}
		
		public static function initGameModule(module:Eclipse):void{
			EclipseUtil.module = module;
			DefaultManagerUtil.module = module;
		}
		
		/**
		 * 取得主游戏面板
		 */
		public static function get mainBoard():EclipseMainBoard{
			return module.eclipseMainBoard;
		}
		
		/**
		 * 取得本地玩家
		 */
		public static function getLocalPlayer():EclipsePlayer{
			return gameManager.localPlayer as EclipsePlayer;
		}
		
		/**
		 * 取得指定位置的玩家
		 */
		public static function getPlayer(position:int):EclipsePlayer{
			return gameManager.players[position];
		}
		
		/**
		 * 取得指定Id的玩家
		 */
		public static function getPlayerById(userId:String):EclipsePlayer{
			return gameManager.getPlayerById(userId) as EclipsePlayer;
		}
		
	}
}