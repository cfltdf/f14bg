package com.f14.F14bgGame.RFTG.manager
{
	import com.f14.F14bgGame.RFTG.component.RaceCard;
	
	/**
	 * RFTG - 卡牌管理器
	 */
	public class RaceCardManager
	{
		public function RaceCardManager()
		{
		}
		
		private var cardCache:Array = new Array();
		private var goalCache:Array = new Array();
		
		/**
		 * 设置卡牌
		 */
		public function put(card:RaceCard):void{
			cardCache[card.id] = card;
		}
		
		/**
		 * 按照id取得卡牌
		 */
		public function getCard(id:String):RaceCard{
			return cardCache[id] as RaceCard;
		}
		
		/**
		 * 将对象转换成RaceCard并设置到管理器
		 */
		public function putObject(obj:Object):RaceCard{
			var card:RaceCard = convertToCard(obj);
			put(card);
			return card;
		}
		
		/**
		 * 将对象转换成RaceCard对象
		 */
		public function convertToCard(obj:Object):RaceCard{
			var card:RaceCard = new RaceCard();
			card.id = obj.id;
			card.cardNo = obj.cardNo;
			card.name = obj.name;
			card.enName = obj.enName;
			card.descr = obj.descr;
			card.imageIndex = obj.imageIndex;
			card.cost = obj.cost;
			card.vp = obj.vp;
			card.qty = obj.qty;
			card.startHand = obj.startHand;
			card.startWorld = obj.startWorld;
			card.military = obj.military;
			card.type = obj.type;
			card.goodType = obj.goodType;
			card.worldType = obj.worldType;
			card.productionType = obj.productionType;
			return card;
		}
		
		/**
		 * 按照id取得目标
		 */
		public function getGoal(id:String):Object{
			return goalCache[id];
		}
		
		/**
		 * 保存目标对象
		 */
		public function putGoal(obj:Object):void{
			goalCache[obj.id] = obj;
		}
	}
}