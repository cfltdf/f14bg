package com.f14.F14bgGame.Innovation.components
{
	import com.f14.F14bgGame.Innovation.InnoUtil;
	import com.f14.F14bgGame.Innovation.manager.InnoImageManager;
	import com.f14.F14bgGame.bg.component.CountImageObject;
	
	public class DrawDeckCard extends CountImageObject
	{
		public function DrawDeckCard(scale:Number=0.3)
		{
			super(scale);
			this.showTooltip = true;
		}
		
		override protected function initImageSize():void{
			this._orgImageWidth = InnoImageManager.WIDTH_BACK;
			this._orgImageHeight = InnoImageManager.HEIGHT_BACK;
		}
		
		/**
		 * 装载图像
		 */
		override protected function loadObjectImage():void{
			if(this.object==null){
				this.loadImage(null);
			}else{
				this.loadImage(InnoUtil.imageManager.getBackImage(this.object.level));
			}
		}
		
	}
}