package com.f14.F14bgGame.PuertoRico.components
{
	import com.f14.F14bgGame.PuertoRico.PRUtil;
	import com.f14.F14bgGame.PuertoRico.consts.Part;
	import com.f14.F14bgGame.PuertoRico.event.PrEvent;
	
	import flash.events.Event;

	public class ResourcePart extends PartTile
	{
		
		public function ResourcePart(scale:Number=1)
		{
			super(scale);
		}
		
		protected var _type:String;
		protected var _color:Boolean;
		
		public function get type():String{
			return this._type;
		}
		
		public function set type(type:String):void{
			this._type = type;
			this.refreshImage();
		}
		
		public function set color(color:Boolean):void{
			this._color = color;
			this.refreshImage();
		}
		
		/**
		 * 刷新显示的图像
		 */
		protected function refreshImage():void{
			if(this._color){
				this.loadImage(PRUtil.imageManager.getPartImage(this.getImageIndex()));
			}else{
				this.loadImage(PRUtil.imageManager.getPartImage(7));
			}
		}
		
		/**
		 * 取得资源类型对应的图像编号
		 */
		protected function getImageIndex():int{
			switch(this.type){
				case Part.CORN:
					return 2;
				case Part.INDIGO:
					return 3;
				case Part.SUGAR:
					return 4;
				case Part.TOBACCO:
					return 5;
				case Part.COFFEE:
					return 6;
				default:
					return 7;
			}
		}
		
		/**
		 * 点击配件时触发的函数
		 */
		override public function onPartClick(evt:Event):void{
			if(this.selectable){
				var prevt:PrEvent = new PrEvent(PrEvent.RESOURCE_CLICK);
				prevt.part = this;
				this.dispatchEvent(prevt);
			}
		}
		
	}
}