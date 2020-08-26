package com.f14.F14bgGame.Eclipse.components
{
	import com.f14.F14bgGame.Eclipse.EclipseUtil;
	import com.f14.F14bgGame.Eclipse.manager.EclipseImageManager;
	import com.f14.F14bgGame.bg.component.ImageObject;
	
	public class EclipseTechnology extends ImageObject
	{
		public function EclipseTechnology(scale:Number=0.34)
		{
			super(scale);
			this.showTooltip = true;
		}
		
		override protected function initImageSize():void{
			this._orgImageWidth = EclipseImageManager.WIDTH_SCIENCE;
			this._orgImageHeight = EclipseImageManager.HEIGHT_SCIENCE;
		}
		
		/**
		 * 装载图像
		 */
		override protected function loadObjectImage():void{
			if(this.object==null){
				this.loadImage(null);
			}else{
				this.loadImage(EclipseUtil.imageManager.getScienceImage(this.object.imageIndex));
			}
		}
		
	}
}