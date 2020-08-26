package com.f14.F14bgGame.TTA.player
{
	import com.f14.F14bgGame.bg.consts.InputState;
	import com.f14.F14bgGame.bg.player.Player;
	import com.f14.F14bgGame.TTA.components.TTACard;
	import com.f14.F14bgGame.TTA.ui.TTAPlayerBoard;
	import com.f14.F14bgGame.TTA.ui.TTAPlayerTable;
	import com.f14.F14bgGame.TTA.ui.part.TTALabel;
	import com.f14.F14bgGame.TTA.ui.part.TTAPlayerHand;

	public class TTAPlayer extends Player
	{
		public function TTAPlayer()
		{
			super();
		}
		
		[Bindable]
		public var culture:int;
		[Bindable]
		public var science:int;
		[Bindable]
		public var military:int;
		[Bindable]
		public var happiness:int;
		[Bindable]
		public var culturePoint:int;
		[Bindable]
		public var sciencePoint:int;
		[Bindable]
		public var civilAction:int;
		[Bindable]
		public var militaryAction:int;
		[Bindable]
		public var civilHands:int;
		[Bindable]
		public var militaryHands:int;
		[Bindable]
		public var colonyBonus:int;
		
		public var playerHands:TTAPlayerHand;
		protected var _playerTableBoard:TTAPlayerTable;
		protected var _step:String;
		
		public function set playerTableBoard(playerTableBoard:TTAPlayerTable):void{
			if(this._playerTableBoard!=null){
				this._playerTableBoard.player = null;
			}
			this._playerTableBoard = playerTableBoard;
			if(playerTableBoard!=null){
				playerTableBoard.player = this;
			}
		}
		
		public function get playerTableBoard():TTAPlayerTable{
			return this._playerTableBoard;
		}
		
		public function set step(step:String):void{
			this._step = step;
			//设置步骤后,将刷新按键状态及组件输入状态
			if(this.ttaPlayerBoard!=null){
				this.ttaPlayerBoard.showStepButtons();
				this.ttaPlayerBoard.inputState = InputState.DEFAULT;
			}
		}
		
		public function get step():String{
			return this._step;
		}
		
		/**
		 * 清理玩家的游戏信息
		 */
		override public function clear():void{
			super.clear();
			this.playerTableBoard = null;
			this._step = null;
			//if(this._playerTableBoard!=null){
			//	this._playerTableBoard.player = null;
			//}
		}
		
		/**
		 * 取得玩家的游戏面板
		 */
		public function get ttaPlayerBoard():TTAPlayerBoard{
			return this._playerBoard as TTAPlayerBoard;
		}
		
		public function addCards(cardIds:String):void{
			var cards:Array = this.ttaPlayerBoard.addCards(cardIds);
			this.playerTableBoard.addCards(cards);
		}
		
		public function removeCards(cardIds:String):void{
			this.ttaPlayerBoard.removeCards(cardIds);
			this.playerTableBoard.removeCards(cardIds);
		}
		
		/**
		 * 装载文明属性
		 */
		public function loadProperty(property:Object):void{
			this.culturePoint = property.CULTURE_POINT;
			this.culture = property.CULTURE;
			this.sciencePoint = property.SCIENCE_POINT;
			this.science = property.SCIENCE;
			this.military = property.MILITARY;
			this.happiness = property.HAPPINESS;
			this.civilAction = property.CIVIL_ACTION;
			this.militaryAction = property.MILITARY_ACTION;
			this.colonyBonus = property.COLONIZING_BONUS;
			
			this.civilHands = property.CIVIL_HANDS;
			this.militaryHands = property.MILITARY_HANDS;
		}
		
		/**
		 * 装载卡牌标志物数量
		 */
		public function loadCardToken(cards:Object):void{
			for(var id:String in cards){
				var card:TTACard = this.ttaPlayerBoard.getCard(id);
				card.yellow = cards[id].YELLOW;
				card.blue = cards[id].BLUE;
				card.white = cards[id].WHITE;
				card.red = cards[id].RED;
				
				var label:TTALabel = this.playerTableBoard.getLabel(id);
				label.checkDescr();
			}
		}
		
		/**
		 * 装载玩家面板的标志物
		 */
		public function loadBoardToken(param:Object):void{
			if(param.AVAILABLE_WORKER!=null){
				this.ttaPlayerBoard.workerPool.num = param.AVAILABLE_WORKER;
				this.playerTableBoard.availableWorker = param.AVAILABLE_WORKER;
			}
			if(param.AVAILABLE_BLUE!=null){
				this.ttaPlayerBoard.resourcePool.num = param.AVAILABLE_BLUE;
				this.playerTableBoard.availableBlue = param.AVAILABLE_BLUE;
			}
			if(param.UNUSED_WORKER!=null){
				this.ttaPlayerBoard.unusedWorker.num = param.UNUSED_WORKER;
				this.playerTableBoard.unusedWorker = param.UNUSED_WORKER;
			}
			//显示幸福度相关内容
			this.ttaPlayerBoard.happinessIndicate.loadParam(param);
		}
		
		/**
		 * 玩家得到手牌
		 */
		public function addHands(param:Object):void{
			if(this.playerHands!=null){
				this.playerHands.addCards(param.civilCards);
				this.playerHands.addCards(param.militaryCards);
			}
		}
		
		/**
		 * 玩家失去手牌
		 */
		public function removeHands(param:Object):void{
			if(this.playerHands!=null){
				this.playerHands.removeCards(param.civilCards);
				this.playerHands.removeCards(param.militaryCards);
			}
		}
		
		/**
		 * 为玩家设置未建造完成的奇迹
		 */
		public function setUncompleteWonder(cardId:String):void{
			this.ttaPlayerBoard.setUncompleteWonder(cardId);
			this.playerTableBoard.uncompleteWonder = this.ttaPlayerBoard.uncompleteWonder;
		}
		
		/**
		 * 玩家完成奇迹的建造
		 */
		public function completeWonder():void{
			var card:TTACard = this.ttaPlayerBoard.completeWonder();
			this.playerTableBoard.completeWonder(card);
		}
	}
}