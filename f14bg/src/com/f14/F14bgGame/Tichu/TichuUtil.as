package com.f14.F14bgGame.Tichu
{
	import com.f14.F14bgGame.Tichu.manager.TichuActionManager;
	import com.f14.F14bgGame.Tichu.manager.TichuEffectManager;
	import com.f14.F14bgGame.Tichu.manager.TichuImageManager;
	import com.f14.F14bgGame.Tichu.manager.TichuResourceManager;
	import com.f14.F14bgGame.Tichu.manager.TichuStateManager;
	import com.f14.F14bgGame.Tichu.player.TichuPlayer;
	import com.f14.F14bgGame.Tichu.ui.TichuMainBoard;
	import com.f14.F14bgGame.bg.manager.AlertManager;
	import com.f14.F14bgGame.bg.manager.AnimManager;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.manager.GameManager;
	import com.f14.F14bgGame.bg.manager.TooltipManager;
	import com.f14.f14bg.manager.SoundManager;
	
	public class TichuUtil
	{
		public static var module:Tichu;
		public static var effectManager:TichuEffectManager;
		public static var imageManager:TichuImageManager;
		public static var resourceManager:TichuResourceManager;
		public static var soundManager:SoundManager;
		public static var tooltipManager:TooltipManager;
		public static var gameManager:GameManager;
		public static var actionManager:TichuActionManager;
		public static var stateManager:TichuStateManager;
		public static var alertManager:AlertManager;
		public static var animManager:AnimManager;
		
		/**
		 * 初始化
		 */
		public static function init():void{
			effectManager = new TichuEffectManager();
			soundManager = new SoundManager();
			animManager = new AnimManager();
			
			tooltipManager = new TooltipManager();
			stateManager = new TichuStateManager();
			actionManager = new TichuActionManager();
			
			gameManager = new GameManager();
			resourceManager = new TichuResourceManager();
			alertManager = new AlertManager();
			
			DefaultManagerUtil.effectManager = effectManager;
			DefaultManagerUtil.gameManager = gameManager;
			DefaultManagerUtil.stateManager = stateManager;
			DefaultManagerUtil.tooltipManager = tooltipManager;
			DefaultManagerUtil.alertManager = alertManager;
			DefaultManagerUtil.animManager = animManager;
		}
		
		public static function initImageManager():void{
			imageManager = new TichuImageManager();
			DefaultManagerUtil.imageManager = imageManager;
			imageManager.loadImages();
		}
		
		public static function initGameModule(module:Tichu):void{
			TichuUtil.module = module;
			DefaultManagerUtil.module = module;
		}
		
		/**
		 * 取得主游戏面板
		 */
		public static function get mainBoard():TichuMainBoard{
			return module.tichuMainBoard;
		}
		
		/**
		 * 取得本地玩家
		 */
		public static function getLocalPlayer():TichuPlayer{
			return gameManager.localPlayer as TichuPlayer;
		}
		
		/**
		 * 取得指定位置的玩家
		 */
		public static function getPlayer(position:int):TichuPlayer{
			return gameManager.players[position];
		}
		
		/**
		 * 取得指定Id的玩家
		 */
		public static function getPlayerById(userId:String):TichuPlayer{
			return gameManager.getPlayerById(userId) as TichuPlayer;
		}
		
	}
}