package com.f14.F14bgGame.PuertoRico.components
{
	import com.f14.F14bgGame.PuertoRico.PRUtil;
	import com.f14.F14bgGame.PuertoRico.ui.simple.DoubloonImage;
	import com.f14.F14bgGame.bg.component.ImageObject;
	import com.f14.core.util.ApplicationUtil;
	
	import mx.controls.Image;

	public class CharacterCard extends ImageObject
	{
		//[Embed(source="images/disabled.png")]
		//private static var IMG_DISABLED:Class;
		public static var IMAGE_WIDTH:int = 140;
		public static var IMAGE_HEIGHT:int = 230;
		
		public function CharacterCard(scale:Number=1)
		{
			super(scale);
			
			doubloonImage = new DoubloonImage();
			doubloonImage.x = this._orgImageWidth - doubloonImage.width - 5;
			doubloonImage.y = this._orgImageHeight - doubloonImage.height - 5;
			this.addChild(doubloonImage);
			this.doubloon = 0;
			
			disabledImage = PRUtil.imageManager.getImageInstance("disabled.png");
			disabledImage.width = IMAGE_WIDTH;
			disabledImage.height = IMAGE_HEIGHT;
			this.addChild(disabledImage);
			this.canUse = true;
		}
		
		protected var _canUse:Boolean;
		protected var _doubloon:int;
		protected var doubloonImage:DoubloonImage;
		protected var disabledImage:Image;
		
		override protected function initImageSize():void{
			this._orgImageWidth = IMAGE_WIDTH;
			this._orgImageHeight = IMAGE_HEIGHT;
		}
		
		public function get doubloon():int{
			return this._doubloon;
		}
		
		public function set doubloon(doubloon:int):void{
			this._doubloon = doubloon;
			this.doubloonImage.doubloon = doubloon;
			if(doubloon==0){
				this.doubloonImage.visible = false;
			}else{
				this.doubloonImage.visible = true;
			}
		}
		
		public function get canUse():Boolean{
			return this._canUse;
		}
		
		public function set canUse(canUse:Boolean):void{
			this._canUse = canUse;
			if(canUse){
				this.disabledImage.visible = false;
			}else{
				this.disabledImage.visible = true;
			}
		}
		
		/**
		 * 装载板块的图像
		 */
		override protected function loadObjectImage():void{
			if(this.object==null){
				this.loadImage(null);
			}else{
				this.loadImage(PRUtil.imageManager.getCardImage(object.imageIndex));
			}
		}
		
	}
}