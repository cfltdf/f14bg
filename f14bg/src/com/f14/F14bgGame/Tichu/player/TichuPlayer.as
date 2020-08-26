package com.f14.F14bgGame.Tichu.player
{
	import com.f14.F14bgGame.Tichu.TichuUtil;
	import com.f14.F14bgGame.Tichu.ui.TichuPlayerBoard;
	import com.f14.F14bgGame.bg.player.Player;

	public class TichuPlayer extends Player
	{
		public function TichuPlayer()
		{
			super();
		}
		
		public var tichuType:String;
		public var rank:int;
		[Bindable]
		public var score:int;
		
		/**
		 * 取得玩家的游戏面板
		 */
		public function get tichuPlayerBoard():TichuPlayerBoard{
			return this._playerBoard as TichuPlayerBoard;
		}
		
		/**
		 * 装载玩家的手牌信息
		 */
		public function loadHands(param:Object):void{
			if(param.cardIds){
				this.tichuPlayerBoard.playerHand.loadCards(param.cardIds);
			}else{
				this.tichuPlayerBoard.playerHand.setNumber(param.num);
			}
		}
		
		/**
		 * 装载玩家的基本信息
		 */
		public function loadPlayerInfo(param:Object):void{
			this.tichuType = param.playerInfo.tichuType;
			this.rank = param.playerInfo.rank;
			this.score = param.playerInfo.score;
			this.tichuPlayerBoard.tichuPlayerInfoBoard.refreshPlayerInfo();
		}
		
		/**
		 * 装载玩家的出牌信息
		 */
		public function loadPlayerPlayedCard(param:Object):void{
			if(param.cardIds){
				this.tichuPlayerBoard.playerCard.loadCards(param.cardIds);
			}else{
				this.tichuPlayerBoard.playerCard.clear();
			}
		}
		
		/**
		 * 装载玩家的按键信息
		 */
		public function loadPlayerButton(param:Object):void{
			if(this.tichuPlayerBoard.buttonPart!=null){
				this.tichuPlayerBoard.buttonPart.loadButtonParam(param);
			}
		}
		
	}
}