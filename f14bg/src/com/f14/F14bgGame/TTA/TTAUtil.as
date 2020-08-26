package com.f14.F14bgGame.TTA
{
	import com.f14.F14bgGame.TTA.manager.OvertimeCardManager;
	import com.f14.F14bgGame.TTA.manager.TTAActionManager;
	import com.f14.F14bgGame.TTA.manager.TTAImageManager;
	import com.f14.F14bgGame.TTA.manager.TTAResourceManager;
	import com.f14.F14bgGame.TTA.manager.TTAStateManager;
	import com.f14.F14bgGame.TTA.player.TTAPlayer;
	import com.f14.F14bgGame.TTA.ui.TTAMainBoard;
	import com.f14.F14bgGame.bg.manager.AlertManager;
	import com.f14.F14bgGame.bg.manager.AnimManager;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.manager.EffectManager;
	import com.f14.F14bgGame.bg.manager.GameManager;
	import com.f14.F14bgGame.bg.manager.TooltipManager;
	import com.f14.f14bg.manager.SoundManager;
	
	public class TTAUtil
	{
		public static var module:TTA;
		public static var effectManager:EffectManager;
		public static var imageManager:TTAImageManager;
		public static var resourceManager:TTAResourceManager;
		public static var soundManager:SoundManager;
		public static var tooltipManager:TooltipManager;
		public static var gameManager:GameManager;
		public static var actionManager:TTAActionManager;
		public static var stateManager:TTAStateManager;
		public static var overtimeCardManager:OvertimeCardManager;
		public static var alertManager:AlertManager;
		public static var animManager:AnimManager;
		
		/**
		 * 初始化
		 */
		public static function init():void{
			effectManager = new EffectManager();
			soundManager = new SoundManager();
			animManager = new AnimManager();
			
			tooltipManager = new TooltipManager();
			stateManager = new TTAStateManager();
			actionManager = new TTAActionManager();
			
			gameManager = new GameManager();
			resourceManager = new TTAResourceManager();
			overtimeCardManager = new OvertimeCardManager();
			alertManager = new AlertManager();
			
			DefaultManagerUtil.effectManager = effectManager;
			DefaultManagerUtil.gameManager = gameManager;
			DefaultManagerUtil.stateManager = stateManager;
			DefaultManagerUtil.tooltipManager = tooltipManager;
			DefaultManagerUtil.alertManager = alertManager;
			DefaultManagerUtil.animManager = animManager;
		}
		
		public static function initImageManager():void{
			imageManager = new TTAImageManager();
			DefaultManagerUtil.imageManager = imageManager;
			imageManager.loadImages();
		}
		
		public static function initGameModule(module:TTA):void{
			TTAUtil.module = module;
			DefaultManagerUtil.module = module;
		}
		
		/**
		 * 取得主游戏面板
		 */
		public static function get mainBoard():TTAMainBoard{
			return module.ttaMainBoard;
		}
		
		/**
		 * 取得本地玩家
		 */
		public static function getLocalPlayer():TTAPlayer{
			return gameManager.localPlayer as TTAPlayer;
		}
		
		/**
		 * 取得指定位置的玩家
		 */
		public static function getPlayer(position:int):TTAPlayer{
			return gameManager.players[position];
		}
		
		/**
		 * 取得指定Id的玩家
		 */
		public static function getPlayerById(userId:String):TTAPlayer{
			return gameManager.getPlayerById(userId) as TTAPlayer;
		}
		
	}
}