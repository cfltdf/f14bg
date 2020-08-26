package com.f14.F14bgGame.Innovation
{
	import com.f14.F14bgGame.Innovation.manager.InnoActionManager;
	import com.f14.F14bgGame.Innovation.manager.InnoAnimManager;
	import com.f14.F14bgGame.Innovation.manager.InnoEffectManager;
	import com.f14.F14bgGame.Innovation.manager.InnoImageManager;
	import com.f14.F14bgGame.Innovation.manager.InnoResourceManager;
	import com.f14.F14bgGame.Innovation.manager.InnoStateManager;
	import com.f14.F14bgGame.Innovation.manager.InnoTooltipManager;
	import com.f14.F14bgGame.Innovation.player.InnoPlayer;
	import com.f14.F14bgGame.Innovation.ui.InnoMainBoard;
	import com.f14.F14bgGame.bg.manager.AlertManager;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.manager.GameManager;
	import com.f14.f14bg.manager.SoundManager;
	
	public class InnoUtil
	{
		public static var module:Innovation;
		public static var effectManager:InnoEffectManager;
		public static var imageManager:InnoImageManager;
		public static var resourceManager:InnoResourceManager;
		public static var soundManager:SoundManager;
		public static var tooltipManager:InnoTooltipManager;
		public static var gameManager:GameManager;
		public static var actionManager:InnoActionManager;
		public static var stateManager:InnoStateManager;
		public static var alertManager:AlertManager;
		public static var animManager:InnoAnimManager;
		
		/**
		 * 初始化
		 */
		public static function init():void{
			effectManager = new InnoEffectManager();
			soundManager = new SoundManager();
			
			tooltipManager = new InnoTooltipManager();
			stateManager = new InnoStateManager();
			actionManager = new InnoActionManager();
			
			gameManager = new GameManager();
			alertManager = new AlertManager();
			animManager = new InnoAnimManager();
			
			DefaultManagerUtil.effectManager = effectManager;
			DefaultManagerUtil.gameManager = gameManager;
			DefaultManagerUtil.stateManager = stateManager;
			DefaultManagerUtil.tooltipManager = tooltipManager;
			DefaultManagerUtil.alertManager = alertManager;
			DefaultManagerUtil.animManager = animManager;
		}
		
		public static function loadResources():void{
			resourceManager = new InnoResourceManager();
			resourceManager.loadResourceString();
			
			imageManager = new InnoImageManager();
			DefaultManagerUtil.imageManager = imageManager;
			imageManager.loadImages();
		}
		
		public static function initGameModule(module:Innovation):void{
			InnoUtil.module = module;
			DefaultManagerUtil.module = module;
		}
		
		/**
		 * 取得主游戏面板
		 */
		public static function get mainBoard():InnoMainBoard{
			return module.innoMainBoard;
		}
		
		/**
		 * 取得本地玩家
		 */
		public static function getLocalPlayer():InnoPlayer{
			return gameManager.localPlayer as InnoPlayer;
		}
		
		/**
		 * 取得指定位置的玩家
		 */
		public static function getPlayer(position:int):InnoPlayer{
			return gameManager.players[position];
		}
		
		/**
		 * 取得指定Id的玩家
		 */
		public static function getPlayerById(userId:String):InnoPlayer{
			return gameManager.getPlayerById(userId) as InnoPlayer;
		}
		
	}
}