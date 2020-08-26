package com.f14.F14bgGame.Tichu.manager
{
	import com.f14.F14bgGame.Tichu.consts.TichuGameCmd;
	import com.f14.F14bgGame.bg.manager.GameActionManager;

	public class TichuActionManager extends GameActionManager
	{
		public function TichuActionManager()
		{
			super();
		}
		
		/**
		 * 叫小地主
		 */
		public function callSmallTichu():void{
			this.sendCurrentCommand("smallTichu");
		}
		
		/**
		 * 出牌
		 */
		public function playCard(cardIds:String, point:int=0):void{
			var param:Object = {};
			param.cardIds = cardIds;
			param.point = point;
			this.sendCurrentCommand("play", param);
		}
		
		/**
		 * 出炸弹
		 */
		public function playBomb():void{
			this.sendCurrentCommand("bomb");
		}
		
		/**
		 * 炸弹阶段出牌
		 */
		public function playBombCard(cardIds:String):void{
			var param:Object = this.createGameCommand(TichuGameCmd.GAME_CODE_BOMB_PHASE);
			param.cardIds = cardIds;
			param.subact = "play";
			this.sendCommand(param);
		}
		
	}
}