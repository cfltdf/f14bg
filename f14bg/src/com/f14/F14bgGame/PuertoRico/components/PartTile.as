package com.f14.F14bgGame.PuertoRico.components
{
	import com.f14.F14bgGame.PuertoRico.PRUtil;
	
	import flash.events.Event;
	import flash.events.MouseEvent;

	public class PartTile extends ImageTile
	{
		public static var IMAGE_WIDTH:int = 20;
		public static var IMAGE_HEIGHT:int = 20;
		
		public function PartTile(scale:Number=1)
		{
			super(scale);
			this.addEventListener(MouseEvent.CLICK, onPartClick);
		}
		
		override protected function initImageSize():void{
			this._orgImageWidth = IMAGE_WIDTH;
			this._orgImageHeight = IMAGE_HEIGHT;
		}
		
		/**
		 * 装载板块的图像
		 */
		override protected function loadObjectImage():void{
			if(this.object==null){
				this.loadImage(null);
			}else{
				this.loadImage(PRUtil.imageManager.getPartImage(object.imageIndex));
			}
		}
		
		/**
		 * 点击配件时触发的函数
		 */
		public function onPartClick(evt:Event):void{
			/* if(this.selectable){
				this.selected = !this.selected;
			} */
		}
		
	}
}