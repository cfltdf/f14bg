package com.f14.F14bgGame.RFTG.ui.simple
{
	import com.f14.F14bgGame.RFTG.RaceUtil;
	import com.f14.F14bgGame.bg.component.ImageObject;
	
	public class GoalCard extends ImageObject
	{
		public static var IMAGE_WIDTH:int = 128;
		public static var IMAGE_HEIGHT:int = 128;
		
		public function GoalCard(scale:Number=1)
		{
			super(scale);
			this.showTooltip = true;
		}
		
		override protected function initImageSize():void{
			this._orgImageWidth = IMAGE_WIDTH;
			this._orgImageHeight = IMAGE_HEIGHT;
		}
		
		/**
		 * 装载图像
		 */
		override protected function loadObjectImage():void{
			if(this.object==null){
				this.loadImage(null);
			}else{
				this.loadImage(RaceUtil.imageManager.getGoalImage(object.imageIndex));
			}
		}
		
	}
}