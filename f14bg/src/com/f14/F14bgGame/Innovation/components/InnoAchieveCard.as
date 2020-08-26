package com.f14.F14bgGame.Innovation.components
{
	import com.f14.F14bgGame.Innovation.InnoUtil;
	import com.f14.F14bgGame.Innovation.manager.InnoImageManager;
	import com.f14.F14bgGame.bg.component.ImageObject;
	
	public class InnoAchieveCard extends ImageObject
	{
		public function InnoAchieveCard(scale:Number=0.2)
		{
			super(scale);
			this.showTooltip = true;
		}
		
		override protected function initImageSize():void{
			this._orgImageWidth = InnoImageManager.WIDTH_CARD;
			this._orgImageHeight = InnoImageManager.HEIGHT_CARD;
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