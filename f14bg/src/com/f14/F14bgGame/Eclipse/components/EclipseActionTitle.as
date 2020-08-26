package com.f14.F14bgGame.Eclipse.components
{
	import com.f14.F14bgGame.Eclipse.EclipseUtil;
	import com.f14.F14bgGame.Eclipse.consts.EclipseActionType;
	import com.f14.F14bgGame.Eclipse.manager.EclipseImageManager;
	import com.f14.F14bgGame.bg.component.ImageObject;
	import com.f14.core.util.ApplicationUtil;
	
	public class EclipseActionTitle extends ImageObject
	{
		public var actionType:String;
		
		public function EclipseActionTitle(scale:Number=1)
		{
			super(scale);
			this.showTooltip = false;
		}
		
		override protected function initImageSize():void{
			this._orgImageWidth = EclipseImageManager.WIDTH_TITLE;
			this._orgImageHeight = EclipseImageManager.HEIGHT_TITLE;
		}
		
		/**
		 * 装载图像
		 */
		override protected function loadObjectImage():void{
			var index:int = EclipseActionType.getActionIndex(this.actionType);
			this.loadImage(EclipseUtil.imageManager.getActionTitleImage(index));
		}
		
	}
}