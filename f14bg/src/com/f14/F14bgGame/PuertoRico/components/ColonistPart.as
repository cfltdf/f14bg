package com.f14.F14bgGame.PuertoRico.components
{
	import com.f14.F14bgGame.PuertoRico.PRUtil;
	import com.f14.F14bgGame.PuertoRico.event.PrEvent;
	
	import flash.events.Event;

	public class ColonistPart extends PartTile
	{
		
		public function ColonistPart(scale:Number=1)
		{
			super(scale);
		}
		
		override public function set selected(selected:Boolean):void{
			this._selected = selected;
			//按照选中与否显示相应的图像
			if(this._selected){
				this.loadImage(PRUtil.imageManager.getPartImage(0));
			}else{
				this.loadImage(PRUtil.imageManager.getPartImage(1));
			}
		}
		
		/**
		 * 点击配件时触发的函数
		 */
		override public function onPartClick(evt:Event):void{
			if(this.selectable){
				var prevt:PrEvent = new PrEvent(PrEvent.COLONIST_CLICK);
				prevt.part = this;
				this.dispatchEvent(prevt);
			}
		}
		
	}
}