package com.f14.F14bgGame.Eclipse.components
{
	import com.f14.F14bgGame.Eclipse.EclipseUtil;
	import com.f14.F14bgGame.Eclipse.manager.EclipseImageManager;
	import com.f14.F14bgGame.bg.component.ImageObject;
	
	public class EclipseShipPart extends ImageObject
	{
		public var positionIndex:int;
		
		public function EclipseShipPart(scale:Number=0.37)
		{
			super(scale);
			this.showTooltip = true;
		}
		
		override protected function initImageSize():void{
			this._orgImageWidth = EclipseImageManager.WIDTH_GEAR;
			this._orgImageHeight = EclipseImageManager.HEIGHT_GEAR;
		}
		
		/**
		 * 装载图像
		 */
		override protected function loadObjectImage():void{
			if(this.object==null){
				this.loadImage(null);
			}else{
				this.loadImage(EclipseUtil.imageManager.getGearImage(this.object.imageIndex));
			}
		}
		
	}
}