package com.f14.F14bgGame.TS.manager
{
	import com.f14.F14bgGame.TS.components.TSCard;
	import com.f14.F14bgGame.TS.ui.simple.TSCardLabel;
	import com.f14.f14bg.manager.ResourceManager;

	public class TSResourceManager extends ResourceManager
	{
		public function TSResourceManager()
		{
			super();
		}
		
		/**
		 * 装载资源
		 */
		override public function load(param:Object):void{
			for each(var obj:Object in param.cards){
				this.putObject(obj);
			}
		}
		
		/**
		 * 创建TSCard对象
		 */
		public function createTSCard(id:String):TSCard{
			var obj:Object = this.getObject(id);
			var card:TSCard = null;
			if(obj!=null){
				card = new TSCard();
				card.object = obj;
			}
			return card;
		}
		
		/**
		 * 创建TSCard对象
		 */
		public function createTSBackCard(scale:Number=0.2):TSCard{
			var object:Object = new Object();
			object.imageIndex = TSImageManager.CARD_BACK_INDEX;
			var card:TSCard = new TSCard(scale);
			card.object = object;
			return card;
		}
		
		/**
		 * 创建TSCardLabel对象
		 */
		public function createTSLabel(id:String):TSCardLabel{
			var card:TSCard = this.createTSCard(id);
			if(card!=null){
				var sc:TSCardLabel = new TSCardLabel();
				sc.object = card;
				return sc;
			}else{
				return null;
			}
		}
		
		/**
		 * 创建中国牌
		 */
		public function createChinaCard():TSCard{
			var object:Object = this.getObjectByCardNo(6);
			var card:TSCard = new TSCard();
			card.object = object;
			return card;
		}
		
	}
}