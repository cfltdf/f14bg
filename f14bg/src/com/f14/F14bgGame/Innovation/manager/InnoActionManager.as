package com.f14.F14bgGame.Innovation.manager
{
	import com.f14.F14bgGame.Innovation.InnoUtil;
	import com.f14.F14bgGame.bg.manager.GameActionManager;

	public class InnoActionManager extends GameActionManager
	{
		public function InnoActionManager()
		{
			super();
		}
		
		/**
		 * 玩家设置起始卡牌
		 */
		public function setupCard(cardId:String):void{
			var param:Object = {};
			param.cardId = cardId;
			this.sendCurrentCommand("MELD_CARD", param);
		}
		
		/**
		 * 玩家合并卡牌
		 */
		public function meldCard(cardId:String):void{
			var param:Object = {};
			param.cardId = cardId;
			this.sendCurrentCommand("MELD_CARD", param);
		}
		
		/**
		 * 玩家摸卡牌
		 */
		public function drawCard(level:int):void{
			if(InnoUtil.stateManager.currentConfirmWindow!=null){
				//如果存在确认窗口,则从确认窗口发送参数
				var param:Object = {};
				param.level = level;
				param.subact = "DRAW_CARD";
				InnoUtil.stateManager.currentConfirmWindow.doConfirm(param);
			}else{
				var param:Object = {};
				param.level = level;
				this.sendCurrentCommand("DRAW_CARD", param);
			}
		}
		
		/**
		 * 玩家触发卡牌能力
		 */
		public function dogmaCard(color:String):void{
			var param:Object = {};
			param.color = color;
			this.sendCurrentCommand("DOGMA", param);
		}
		
		/**
		 * 玩家拿取成就牌
		 */
		public function drawAchieveCard(cardId:String):void{
			var param:Object = {};
			param.cardId = cardId;
			this.sendCurrentCommand("ACHIEVE", param);
		}
		
	}
}