package com.f14.F14bgGame.RFTG.manager
{
	import com.f14.F14bgGame.RFTG.RaceUtil;
	import com.f14.F14bgGame.RFTG.component.RaceCard;
	import com.f14.F14bgGame.RFTG.ui.simple.ImageCard;
	import com.f14.f14bg.manager.ResourceManager;

	public class RaceResourceManager extends ResourceManager
	{
		public function RaceResourceManager()
		{
			super();
		}
		
		/**
		 * 装载资源
		 */
		override public function load(param:Object):void{
			var i:int = 0;
			for(i=0;i<param.cards.length;i++){
				RaceUtil.cardManager.putObject(param.cards[i]);
			}
			if(param.goals){
				//装载目标
				for(i=0;i<param.goals.length;i++){
					RaceUtil.cardManager.putGoal(param.goals[i]);
				}
			}
		}
		
		/**
		 * 创建指定id的货物牌
		 */
		public function createGoodCard(cardId:String):ImageCard{
			var good:ImageCard = new ImageCard(RaceImageManager.DEFAULT_SCALE);
			var card:RaceCard = new RaceCard();
			card.id = cardId;
			card.imageIndex = RaceImageManager.IMAGE_INDEX_BACK;
			good.object = card;
			good.showTooltip = false;
			return good;
		}
		
	}
}