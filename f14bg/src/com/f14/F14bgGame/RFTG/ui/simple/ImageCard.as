package com.f14.F14bgGame.RFTG.ui.simple
{
	import com.f14.F14bgGame.RFTG.RaceUtil;
	import com.f14.F14bgGame.RFTG.consts.UIConst;
	import com.f14.F14bgGame.RFTG.manager.RaceImageManager;
	import com.f14.F14bgGame.bg.component.ImageObject;
	
	import mx.effects.IEffectInstance;

	public class ImageCard extends ImageObject
	{
		public function ImageCard(scale:Number=1)
		{
			super(scale);
			this.showTooltip = true;
		}
		
		public var activeType:String;
		protected var _active:Boolean;
		protected var _locked:Boolean;
		
		protected var _activeEffect:IEffectInstance;
		
		override protected function initImageSize():void{
			this._orgImageWidth = RaceImageManager.WIDTH;
			this._orgImageHeight = RaceImageManager.HEIGHT;
		}
		
		public function set active(active:Boolean):void{
			this._active = active;
			if(this.active){
				this._activeEffect.play();
			}else{
				this._activeEffect.end();
			}
		}
		
		public function get active():Boolean{
			return this._active;
		}
		
		public function set locked(locked:Boolean):void{
			var offset:int = 0;
			if(this._locked!=locked){
				offset = UIConst.PADDING_Y - 3;
			}
			this._locked = locked;
			if(locked){
				this.selected = false;
				this.selectable = false;
				this.y -= offset;
			}else{
				this.selected = false;
				this.selectable = true;
				this.y += offset;
			}
		}
		
		public function get locked():Boolean{
			return this._locked;
		}
		
		/**
		 * 装载图像
		 */
		override protected function loadObjectImage():void{
			if(this.object==null){
				this.loadImage(null);
			}else{
				this.loadImage(RaceUtil.imageManager.getCardImage(this.object.imageIndex));
			}
		}
		
		/**
		 * 读取图像
		 */
		/*public function loadImage(index:int=-1):void{
			if(index!=-1){
				bitmap.bitmapData = ImageManager.getCardImage(index);
			}else{
				if(card==null){
					bitmap.bitmapData = null;
				}else{
					bitmap.bitmapData = ImageManager.getCardImage(card.imageIndex);
				}
			}
			this.scale = scale;
		}*/
		
		/**
		 * 初始化特殊效果
		 */
		override protected function initEffect():void{
			super.initEffect();
			_activeEffect = RaceUtil.effectManager.activeEffect.createInstance(this);
		}
		
	}
}