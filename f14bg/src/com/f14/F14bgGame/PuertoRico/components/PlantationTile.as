package com.f14.F14bgGame.PuertoRico.components
{
	import com.f14.F14bgGame.PuertoRico.PRUtil;
	

	public class PlantationTile extends ImageTile
	{
		public static var IMAGE_WIDTH:int = 45;
		public static var IMAGE_HEIGHT:int = 45;
		
		public function PlantationTile(scale:Number=1)
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
				this.loadImage(PRUtil.imageManager.getPlantationImage(object.imageIndex));
			}
		}
		
	}
}