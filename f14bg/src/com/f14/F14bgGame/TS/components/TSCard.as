package com.f14.F14bgGame.TS.components
{
	import com.f14.F14bgGame.TS.TSUtil;
	import com.f14.F14bgGame.TS.manager.TSImageManager;
	import com.f14.F14bgGame.bg.component.ImageObject;
	
	import mx.controls.Label;
	
	public class TSCard extends ImageObject
	{
		public function TSCard(scale:Number=0.2)
		{
			super(scale);
			this.showTooltip = true;
		}
		
		override protected function initImageSize():void{
			this._orgImageWidth = TSImageManager.WIDTH_CARD;
			this._orgImageHeight = TSImageManager.HEIGHT_CARD;
		}
		
		/**
		 * 装载图像
		 */
		override protected function loadObjectImage():void{
			if(this.object==null){
				this.loadImage(null);
			}else{
				this.loadImage(TSUtil.imageManager.getCardImage(this.object.imageIndex));
			}
		}
		
	}
}