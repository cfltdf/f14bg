package com.f14.F14bgGame.TTA.components
{
	import com.f14.F14bgGame.bg.component.ImageObject;
	import com.f14.F14bgGame.TTA.TTAUtil;
	import com.f14.F14bgGame.TTA.manager.TTAImageManager;
	
	import mx.controls.Label;
	
	public class TTACardBack extends ImageObject
	{
		public function TTACardBack(scale:Number=0.2)
		{
			super(scale);
			this.showTooltip = true;
			this.initLabel();
		}
		
		protected var _num:int;
		protected var numLabel:Label;
		
		/**
		 * 初始化显示数量标签
		 */
		protected function initLabel():void{
			this.numLabel = new Label();
			this.numLabel.width = 36;
			this.numLabel.height = 20;
			this.numLabel.y = 35;
			this.numLabel.setStyle("fontSize", "14");
			this.numLabel.setStyle("fontWeight", "bold");
			this.numLabel.setStyle("textAlign", "center");
			this.addChild(this.numLabel);
			this.numLabel.visible = false;
		}
		
		override protected function initImageSize():void{
			this._orgImageWidth = TTAImageManager.WIDTH_CARD;
			this._orgImageHeight = TTAImageManager.HEIGHT_CARD;
			//this.width = this._orgImageWidth * this.scale;
			//this.height = this._orgImageHeight * this.scale;
		}
		
		/**
		 * 装载图像
		 */
		override protected function loadObjectImage():void{
			if(this.object==null){
				this.loadImage(null);
				this.numLabel.visible = false;
			}else{
				this.loadImage(TTAUtil.imageManager.getBackImage(this.object.level, this.object.actionType));
				this.numLabel.visible = true;
			}
		}
		
		public function set num(num:int):void{
			this._num = num;
			if(this.numLabel!=null){
				this.numLabel.text = num + "";
			}
		}
		
		public function get num():int{
			return this._num;
		}
	}
}