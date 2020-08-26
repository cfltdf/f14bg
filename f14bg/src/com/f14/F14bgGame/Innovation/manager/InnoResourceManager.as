package com.f14.F14bgGame.Innovation.manager
{
	import com.f14.F14bgGame.Innovation.components.BackCard;
	import com.f14.F14bgGame.Innovation.components.DrawDeckCard;
	import com.f14.F14bgGame.Innovation.components.InnoCard;
	import com.f14.F14bgGame.bg.component.ImageObject;
	import com.f14.f14bg.manager.ResourceManager;

	public class InnoResourceManager extends ResourceManager
	{
		public function InnoResourceManager()
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
		 * 创建InnoCard对象
		 */
		public function createInnoCard(id:String, scale:Number=0.2):InnoCard{
			var obj:Object = this.getObject(id);
			var card:InnoCard = null;
			if(obj!=null){
				card = new InnoCard(scale);
				card.object = obj;
			}
			return card;
		}
		
		/**
		 * 创建InnoCard牌背对象
		 */
		public function createInnoBackCard(level:int,scale:Number=0.2):InnoCard{
			var obj:Object = {};
			obj.level = level;
			var card:BackCard = new BackCard();
			card.object = obj;
			return card;
		}
		
		/**
		 * 创建InnoCard牌背对象,需要设置cardId
		 */
		public function createInnoBackCardById(cardId:String,scale:Number=0.2):InnoCard{
			var obj:Object = this.getObject(cardId);
			if(obj!=null){
				var card:InnoCard = this.createInnoBackCard(obj.level, scale);
				card.object.id = obj.id;
				return card;
			}else{
				return null;
			}
		}
		
		/**
		 * 创建InnoCard摸牌堆的对象
		 */
		public function createDrawDeckCard(level:int,scale:Number=0.2):DrawDeckCard{
			var obj:Object = {};
			obj.level = level;
			var card:DrawDeckCard = new DrawDeckCard();
			card.object = obj;
			return card;
		}
		
		/**
		 * 创建InnoCard摸牌堆的对象
		 */
		public function createAchieveCard(cardId:String):ImageObject{
			var object = this.getObject(cardId);
			var card:ImageObject = null;
			if(object!=null){
				//特殊成就和普通成就分开处理
				if(object.special){
					card = new InnoCard();
					card.object = object;
				}else{
					card = new DrawDeckCard();
					card.object = object;
				}
			}
			return card;
		}
		
	}
}