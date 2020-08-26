package com.f14.F14bgGame.RFTG.player
{
	import com.f14.F14bgGame.RFTG.RaceUtil;
	import com.f14.F14bgGame.RFTG.component.RaceCard;
	import com.f14.F14bgGame.RFTG.ui.ControlBoard;
	import com.f14.F14bgGame.RFTG.ui.RacePlayerBoard;
	import com.f14.F14bgGame.bg.player.Player;
	
	import mx.collections.ArrayCollection;

	public class RacePlayer extends Player
	{
		public function RacePlayer()
		{
			super();
		}
		
		[Bindable]
		public var vp:int;
		[Bindable]
		public var handSize:int;
		
		public var actionTypes:Array = new Array();
		public var hands:ArrayCollection = new ArrayCollection();
		public var builtCards:ArrayCollection = new ArrayCollection();
		
		protected var _controlBoard:ControlBoard
		
		public function get racePlayerBoard():RacePlayerBoard{
			return this._playerBoard as RacePlayerBoard;
		}
		
		public function addVP(vp:int):void{
			this.vp += vp;
			racePlayerBoard.onVPChange(vp);
		}
		
		public function addHandSize(handSize:int):void{
			this.handSize += handSize;
			racePlayerBoard.onHandSizeChange(handSize);
		}
		
		public function get controlBoard():ControlBoard{
			return this._controlBoard;
		}
		
		public function set controlBoard(controlBoard:ControlBoard):void{
			this._controlBoard = controlBoard;
			controlBoard.player = this;
		}
		
		/**
		 * 添加到手牌
		 */
		public function addCards(cardIds:String):void{
			var ids:Array = cardIds.split(",");
			for(var i:int=0;i<ids.length;i++){
				var card:RaceCard = RaceUtil.cardManager.getCard(ids[i]);
				if(card!=null){
					this.hands.addItem(card);
					this.controlBoard.cardBoard.addCard(card);
				}
			}
		}
		
		/**
		 * 直接出牌
		 */
		public function directPlayCards(cardIds:String):void{
			var ids:Array = cardIds.split(",");
			for(var i:int=0;i<ids.length;i++){
				var card:RaceCard = RaceUtil.cardManager.getCard(ids[i]);
				if(card!=null){
					this.builtCards.addItem(card);
					this.racePlayerBoard.cardBoard.addCard(card);
				}
			}
		}
		
		/**
		 * 从手牌中弃牌
		 */
		public function discardCards(cardIds:String):void{
			var ids:Array = cardIds.split(",");
			var cards:Array = new Array();
			for(var i:int=0;i<ids.length;i++){
				var card:RaceCard = RaceUtil.cardManager.getCard(ids[i]);
				if(card!=null){
					var ind:int = this.hands.getItemIndex(card);
					this.hands.removeItemAt(ind);
					cards.push(card);
				}
			}
			this.controlBoard.cardBoard.removeCards(cards);
		}
		
		/**
		 * 打出牌
		 */
		public function playCards(cardIds:String):void{
			var ids:Array = cardIds.split(",");
			var cards:Array = new Array();
			for(var i:int=0;i<ids.length;i++){
				var card:RaceCard = RaceUtil.cardManager.getCard(ids[i]);
				if(card!=null){
					var ind:int = this.hands.getItemIndex(card);
					if(ind>=0){
						this.hands.removeItemAt(ind);
					}
					this.builtCards.addItem(card);
					this.racePlayerBoard.cardBoard.addCard(card);
					cards.push(card);
				}
			}
			this.controlBoard.cardBoard.removeCards(cards);
		}
		
		/**
		 * 弃掉打出牌
		 */
		public function discardPlayedCards(cardIds:String):void{
			var ids:Array = cardIds.split(",");
			var cards:Array = new Array();
			for(var i:int=0;i<ids.length;i++){
				var card:RaceCard = RaceUtil.cardManager.getCard(ids[i]);
				if(card!=null){
					var ind:int = this.builtCards.getItemIndex(card);
					this.builtCards.removeItemAt(ind);
					cards.push(card);
				}
			}
			this.racePlayerBoard.cardBoard.removeCards(cards);
		}
		
		/**
		 * 取得选中的卡牌
		 */
		public function getSelectedCard():RaceCard{
			if(this.controlBoard.cardBoard.currentCard==null){
				return null;
			}else{
				return this.controlBoard.cardBoard.currentCard.object as RaceCard;
			}
		}
		
		/**
		 * 取得所有选中的卡牌
		 */
		public function getSelectedCards():Array{
			return this.controlBoard.cardBoard.getSelection();
		}
		
		/**
		 * 取得所有选中的已打出的卡牌
		 */
		public function getSelectedPlayedCards():Array{
			return this.racePlayerBoard.cardBoard.getSelection();
		}
		
		/**
		 * 取得所有选中的货物
		 */
		public function getSelectedGoods():Array{
			return this.racePlayerBoard.cardBoard.getSelectionGood();
		}
		
		/**
		 * 取得选择的行动
		 */
		/* public function getChooseAction():String{
			return ApplicationUtil.application.dropdownAction.selectedItem.@value.toString();
		} */
		
		/**
		 * 重置游戏面板
		 */
		public function resetBoards():void{
			if(this.controlBoard){
				this.controlBoard.cardBoard.reset();
			}
			if(this.playerBoard){
				this.racePlayerBoard.cardBoard.reset();
			}
		}
		
		/**
		 * 生产货物
		 */
		public function produceGoods(cardIds:String):void{
			this.racePlayerBoard.cardBoard.produceGoods(cardIds);
		}
		
		/**
		 * 丢弃货物
		 */
		public function discardGoods(cardIds:String):void{
			this.racePlayerBoard.cardBoard.discardGoods(cardIds);
		}
		
		/**
		 * 清理玩家的游戏信息
		 */
		override public function clear():void{
			super.clear();
			this.vp = 0;
			this.handSize = 0;
			this.actionTypes = new Array();
			this.hands.removeAll();
			this.builtCards.removeAll();
			
			if(this._controlBoard!=null){
				this._controlBoard.clear();
			}
			if(this._playerBoard!=null){
				this._playerBoard.clear();
			}
		}
	}
}