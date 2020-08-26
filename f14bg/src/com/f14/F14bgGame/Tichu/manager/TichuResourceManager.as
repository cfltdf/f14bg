package com.f14.F14bgGame.Tichu.manager
{
	import com.f14.F14bgGame.Tichu.components.TichuCard;
	import com.f14.f14bg.manager.ResourceManager;

	public class TichuResourceManager extends ResourceManager
	{
		public function TichuResourceManager()
		{
			super();
		}
		
		protected var tichuCards:Array = new Array();
		
		/**
		 * 装载资源
		 */
		override public function load(param:Object):void{
			for each(var obj:Object in param.cards){
				this.putObject(obj);
			}
		}
		
		/**
		 * 创建TichuCard对象
		 */
		public function createTichuCard(id:String):TichuCard{
			var obj:Object = this.getObject(id);
			var card:TichuCard = null;
			if(obj!=null){
				card = new TichuCard();
				card.object = obj;
			}
			return card;
		}
		
		/**
		 * 创建TichuCard背面的对象
		 */
		public function createTichuBackCard():TichuCard{
			var object:Object = new Object();
			object.imageIndex = TichuImageManager.CARD_BACK_INDEX;
			var card:TichuCard = new TichuCard();
			card.object = object;
			return card;
		}
		
	}
}