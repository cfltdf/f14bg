package com.f14.F14bgGame.Tichu.components
{
	import com.f14.F14bgGame.Tichu.TichuUtil;
	import com.f14.F14bgGame.Tichu.manager.TichuImageManager;
	import com.f14.F14bgGame.bg.component.ImageObject;

	public class TichuCard extends ImageObject
	{
		public function TichuCard(scale:Number=1)
		{
			super(scale);
			this.showTooltip = false;
			this.glow = false;
		}
		
		override protected function initImageSize():void{
			this._orgImageWidth = TichuImageManager.WIDTH_CARD;
			this._orgImageHeight = TichuImageManager.HEIGHT_CARD;
		}
		
		/**
		 * 装载图像
		 */
		override protected function loadObjectImage():void{
			if(this.object==null){
				this.loadImage(null);
			}else{
				this.loadImage(TichuUtil.imageManager.getCardImage(this.object.imageIndex));
			}
		}
		
	}
}