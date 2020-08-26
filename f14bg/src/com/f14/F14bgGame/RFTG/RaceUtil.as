package com.f14.F14bgGame.RFTG
{
	import com.f14.F14bgGame.RFTG.manager.RaceActionManager;
	import com.f14.F14bgGame.RFTG.manager.RaceCardManager;
	import com.f14.F14bgGame.RFTG.manager.RaceEffectManager;
	import com.f14.F14bgGame.RFTG.manager.RaceGameManager;
	import com.f14.F14bgGame.RFTG.manager.RaceImageManager;
	import com.f14.F14bgGame.RFTG.manager.RaceResourceManager;
	import com.f14.F14bgGame.RFTG.manager.RaceSoundManager;
	import com.f14.F14bgGame.RFTG.manager.RaceStateManager;
	import com.f14.F14bgGame.RFTG.player.RacePlayer;
	import com.f14.F14bgGame.RFTG.ui.RaceMainBoard;
	import com.f14.F14bgGame.bg.manager.AlertManager;
	import com.f14.F14bgGame.bg.manager.AnimManager;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.manager.TooltipManager;
	
	public class RaceUtil
	{
		public static var module:RFTG;
		public static var effectManager:RaceEffectManager;
		public static var imageManager:RaceImageManager;
		public static var resourceManager:RaceResourceManager;
		public static var soundManager:RaceSoundManager;
		public static var tooltipManager:TooltipManager;
		public static var gameManager:RaceGameManager;
		public static var actionManager:RaceActionManager;
		public static var stateManager:RaceStateManager;
		public static var cardManager:RaceCardManager;
		public static var alertManager:AlertManager;
		public static var animManager:AnimManager;
		
		/**
		 * 初始化
		 */
		public static function init():void{
			effectManager = new RaceEffectManager();
			soundManager = new RaceSoundManager();
			animManager = new AnimManager();
			
			tooltipManager = new TooltipManager();
			stateManager = new RaceStateManager();
			actionManager = new RaceActionManager();
			
			gameManager = new RaceGameManager();
			resourceManager = new RaceResourceManager();
			cardManager = new RaceCardManager();
			alertManager = new AlertManager();
			
			DefaultManagerUtil.effectManager = effectManager;
			DefaultManagerUtil.gameManager = gameManager;
			DefaultManagerUtil.stateManager = stateManager;
			DefaultManagerUtil.tooltipManager = tooltipManager;
			DefaultManagerUtil.alertManager = alertManager;
			DefaultManagerUtil.animManager = animManager;
		}
		
		public static function initImageManager():void{
			imageManager = new RaceImageManager();
			DefaultManagerUtil.imageManager = imageManager;
			imageManager.loadImages();
		}
		
		public static function initGameModule(module:RFTG):void{
			RaceUtil.module = module;
			DefaultManagerUtil.module = module;
		}
		
		/**
		 * 取得主游戏面板
		 */
		public static function get mainBoard():RaceMainBoard{
			return module.raceMainBoard;
		}
		
		/**
		 * 取得本地玩家
		 */
		public static function getLocalPlayer():RacePlayer{
			return gameManager.localPlayer as RacePlayer;
		}
		
		/**
		 * 取得指定位置的玩家
		 */
		public static function getPlayer(position:int):RacePlayer{
			return gameManager.players[position];
		}
		
		/**
		 * 取得指定Id的玩家
		 */
		public static function getPlayerById(userId:String):RacePlayer{
			return gameManager.getPlayerById(userId) as RacePlayer;
		}

	}
}