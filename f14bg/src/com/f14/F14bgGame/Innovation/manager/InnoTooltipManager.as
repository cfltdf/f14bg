package com.f14.F14bgGame.Innovation.manager
{
	import com.f14.F14bgGame.Innovation.ui.simple.InnoCardList;
	import com.f14.F14bgGame.bg.manager.TooltipManager;
	import com.f14.core.util.ApplicationUtil;
	
	import flash.geom.Point;
	
	import mx.core.Application;
	import mx.managers.PopUpManager;

	public class InnoTooltipManager extends TooltipManager
	{
		public function InnoTooltipManager()
		{
			super();
		}
		
		public var cardList:InnoCardList;
		
		override public function init():void{
			super.init();
			this.cardList = new InnoCardList();
			PopUpManager.addPopUp(cardList, ApplicationUtil.application);
			cardList.init();
			cardList.visible = false;
		}
		
		/**
		 * 显示卡牌列表
		 */
		public function showCardList(point:Point, cardIds:String):void{
			var p:Point = ApplicationUtil.application.globalToLocal(point);
			this.cardList.showCardList(cardIds);
			
			if((p.x + this.cardList.width + 5) > ApplicationUtil.application.width){
				p.x = ApplicationUtil.application.width - this.cardList.width - 5;
			}
			if((p.y + this.cardList.height + 5) > Application.application.height){
				p.y = ApplicationUtil.application.height - this.cardList.height - 5;
			}
			PopUpManager.bringToFront(this.cardList);
			this.cardList.x = p.x;
			this.cardList.y = p.y;
			this.cardList.visible = true;
		}
		
	}
}