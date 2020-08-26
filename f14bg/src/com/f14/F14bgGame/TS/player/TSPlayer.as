package com.f14.F14bgGame.TS.player
{
	import com.f14.F14bgGame.TS.ui.TSPlayerBoard;
	import com.f14.F14bgGame.bg.player.Player;

	public class TSPlayer extends Player
	{
		public function TSPlayer()
		{
			super();
		}
		
		[Bindable]
		public var militaryAction:int;
		[Bindable]
		public var spaceRace:int;
		[Bindable]
		public var handNum:int;
		
		public function get tsPlayerBoard():TSPlayerBoard{
			return this._playerBoard as TSPlayerBoard;
		}
		
		/**
		 * 玩家得到手牌
		 */
		public function addHands(param:Object):void{
			if(this.tsPlayerBoard!=null && param.cardIds){
				this.tsPlayerBoard.playerHands.addCards(param.cardIds);
			}
			this.handNum = param.handNum;
		}
		
		/**
		 * 玩家失去手牌
		 */
		public function removeHands(param:Object):void{
			if(this.tsPlayerBoard!=null && param.cardIds){
				this.tsPlayerBoard.playerHands.removeCards(param.cardIds);
			}
			this.handNum = param.handNum;
		}
		
		/**
		 * 装载玩家信息
		 */
		public function loadPlayerParam(param:Object):void{
			this.militaryAction = param.militaryAction;
			this.spaceRace = param.spaceRace;
		}
		
	}
}