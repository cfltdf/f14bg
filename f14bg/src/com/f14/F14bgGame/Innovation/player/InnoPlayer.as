package com.f14.F14bgGame.Innovation.player
{
	import com.f14.F14bgGame.Innovation.InnoUtil;
	import com.f14.F14bgGame.Innovation.ui.InnoPlayerBoard;
	import com.f14.F14bgGame.Innovation.ui.InnoPlayerTable;
	import com.f14.F14bgGame.Innovation.ui.part.InnoCardStack;
	import com.f14.F14bgGame.bg.player.Player;

	public class InnoPlayer extends Player
	{
		public function InnoPlayer()
		{
			super();
		}
		
		protected var _playerTable:InnoPlayerTable;
		
		public function get innoPlayerBoard():InnoPlayerBoard{
			return this._playerBoard as InnoPlayerBoard;
		}
		
		public function set playerTable(playerTable:InnoPlayerTable):void{
			this._playerTable = playerTable;
			playerTable.player = this;
		}
		
		public function get playerTable():InnoPlayerTable{
			return this._playerTable;
		}
		
		/**
		 * 玩家得到手牌
		 */
		public function addHands(param:Object):void{
			if(this.innoPlayerBoard!=null && param.cardIds){
				this.innoPlayerBoard.playerHands.addCards(param.cardIds);
			}
			if(this.playerTable!=null){
				this.playerTable.totalCardNum = param.handNum;
				this.playerTable.hand_cards_info.loadParam(param.handInfo);
			}
		}
		
		/**
		 * 玩家失去手牌
		 */
		public function removeHands(param:Object):void{
			if(this.innoPlayerBoard!=null && param.cardIds){
				this.innoPlayerBoard.playerHands.removeCards(param.cardIds);
			}
			if(this.playerTable!=null){
				this.playerTable.totalCardNum = param.handNum;
				this.playerTable.hand_cards_info.loadParam(param.handInfo);
			}
		}
		
		/**
		 * 刷新玩家的打出的牌堆信息
		 */
		public function refreshCardStack(param:Object):void{
			if(this.playerTable!=null){
				for(var color:String in param.stacksInfo){
					var stackParam:Object = param.stacksInfo[color];
					var cardStack:InnoCardStack = this.playerTable.getCardStack(color);
					cardStack.loadParam(stackParam);
				}
			}
		}
		
		/**
		 * 刷新得分信息
		 */
		public function refreshScoreInfo(param:Object):void{
			if(this.playerTable!=null){
				this.playerTable.totalScoreNum = param.scoreNum;
				this.playerTable.score_cards_info.loadParam(param.scoreInfo);
			}
		}
		
		/**
		 * 刷新得分信息
		 */
		public function refreshAp(param:Object):void{
			if(this.playerTable!=null){
				this.playerTable.actionPointPart.loadParam(param);
			}
		}
		
		/**
		 * 刷新玩家的所有图标信息
		 */
		public function refreshTotalIcons(param:Object):void{
			if(this.playerTable!=null){
				this.playerTable.totalIconsPart.loadParam(param);
			}
		}
		
		/**
		 * 玩家得到成就
		 */
		public function addAchieves(param:Object):void{
			if(this.playerTable!=null){
				if(param.cardIds){
					this.playerTable.playerAchieve.addCards(param.cardIds);
				}
			}
		}
		
		/**
		 * 玩家得到计分区的牌
		 */
		public function addScores(param:Object):void{
			if(InnoUtil.gameManager.isLocalPlayer(this) && param.cardIds){
				InnoUtil.stateManager.checkScoreWindow.cardsPart.addCards(param.cardIds);
			}
			if(this.playerTable!=null){
				this.playerTable.totalScoreNum = param.scoreNum;
				this.playerTable.score_cards_info.loadParam(param.scoreInfo);
			}
		}
		
		/**
		 * 玩家失去计分区的牌
		 */
		public function removeScores(param:Object):void{
			if(InnoUtil.gameManager.isLocalPlayer(this) && param.cardIds){
				InnoUtil.stateManager.checkScoreWindow.cardsPart.removeCards(param.cardIds);
			}
			if(this.playerTable!=null){
				this.playerTable.totalScoreNum = param.scoreNum;
				this.playerTable.score_cards_info.loadParam(param.scoreInfo);
			}
		}
		
	}
}