package com.f14.F14bgGame.TTA.manager
{
	import com.f14.F14bgGame.TTA.components.OvertimeCard;
	
	public class OvertimeCardManager
	{
		public function OvertimeCardManager()
		{
		}
		
		protected var cards:Array = new Array();
		
		public function clear():void{
			this.cards = new Array;
		}
		
		/**
		 * 添加卡牌到缓存中
		 */
		public function addCard(param:Object):void{
			var card:OvertimeCard = new OvertimeCard();
			card.id = param.id;
			card.owner = param.owner;
			card.a = param.a;
			card.b = param.b;
			this.cards[card.id] = card;
		}
		
		/**
		 * 添加卡牌到缓存中
		 */
		public function addCards(params:Array):void{
			for each(var obj:Object in params){
				this.addCard(obj);
			}
		}
		
		/**
		 * 取得缓存的卡牌信息
		 */
		public function getCard(cardId:String):OvertimeCard{
			return this.cards[cardId] as OvertimeCard;
		}

	}
}