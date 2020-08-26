package com.f14.F14bgGame.PuertoRico.components
{
	import com.f14.F14bgGame.PuertoRico.PRUtil;

	public class BigBuildingTile extends BuildingTile
	{
		public static var IMAGE_WIDTH:int = 84;
		public static var IMAGE_HEIGHT:int = 98;
		
		public function BigBuildingTile(scale:Number=1)
		{
			super(scale);
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
				this.loadImage(PRUtil.imageManager.getBigBuildingImage(object.imageIndex));
			}
		}
		
	}
}