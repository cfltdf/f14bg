package com.f14.F14bgGame.TTA.manager
{
	import com.f14.F14bgGame.TTA.components.TTACard;
	import com.f14.f14bg.manager.ResourceManager;

	public class TTAResourceManager extends ResourceManager
	{
		public function TTAResourceManager()
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
		 * 创建TTACard对象
		 */
		public function createTTACard(id:String):TTACard{
			var obj:Object = this.getObject(id);
			var card:TTACard = null;
			if(obj!=null){
				card = new TTACard();
				card.object = obj;
			}
			return card;
		}
	}
}