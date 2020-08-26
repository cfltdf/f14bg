package com.f14.F14bgGame.bg.component
{
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.core.util.ApplicationUtil;
	
	import flash.display.BitmapData;
	import flash.events.MouseEvent;
	import flash.filters.ColorMatrixFilter;
	import flash.geom.Point;
	
	import mx.core.FlexBitmap;
	import mx.core.UIComponent;
	import mx.effects.IEffectInstance;

	public class ImageObject extends UIComponent implements Selectable
	{
		public function ImageObject(scale:Number=1)
		{
			super();
			bitmap = new FlexBitmap();
			bitmap.smoothing = true;
			this.addChild(bitmap);
			initEvent();
			initEffect();
			initImageSize();
			this.scale = scale;
		}
		
		protected var _selectable:Boolean;
		protected var bitmap:FlexBitmap;
		protected var _scale:Number;
		protected var _selected:Boolean = false;
		protected var _showTooltip:Boolean = false;
		protected var _glow:Boolean = true;
		
		protected var _selectEffect:IEffectInstance;
		protected var _colorEffect:Boolean = true;
		
		protected var _orgImageWidth:int = 0;
		protected var _orgImageHeight:int = 0;
		/**
		 * 该图像对象中的实际对象
		 */
		protected var _object:Object;
		
		/**
		 * 初始化原始图像的大小
		 */
		protected function initImageSize():void{
			
		}
		
		/**
		 * 初始化事件监听
		 */
		protected function initEvent():void{
			this.addEventListener(MouseEvent.MOUSE_OVER, onMouseOver);
			this.addEventListener(MouseEvent.MOUSE_OUT, onMouseOut);
		}
		
		/**
		 * 初始化特殊效果
		 */
		protected function initEffect():void{
			_selectEffect = DefaultManagerUtil.effectManager.selectEffect.createInstance(this);
		}
		
		public function get selectable():Boolean{
			return this._selectable;
		}
		
		public function set selectable(selectable:Boolean):void{
			this._selectable = selectable;
			this.refreshGlowEffect();
		}
		
		public function get glow():Boolean{
			return this._glow;
		}
		
		public function set glow(glow:Boolean):void{
			this._glow = glow;
		}
		
		/**
		 * 取得该图像对象中的实际对象
		 */
		public function get object():Object{
			return this._object;
		}
		
		/**
		 * 设置该图像对象中的实际对象并刷新显示的图像
		 */
		public function set object(object:Object):void{
			this._object = object;
			if(object!=null){
				this.id = object.id;
			}else{
				this.id = null;
			}
			this.loadObjectImage();
		}
		
		/**
		 * 设置图像是否选中
		 */
		public function set selected(selected:Boolean):void{
			this._selected = selected;
			//如果发光则处理发光的效果
			if(this.glow){
				if(this.selected){
					DefaultManagerUtil.effectManager.glowEffect.end();
					this._selectEffect.play();
				}else{
					this._selectEffect.end();
				}
			}
		}
		
		public function get selected():Boolean{
			return this._selected;
		}
		
		public function get scale():Number{
			return _scale;
		}
		
		/**
		 * 设置图像的显示比例
		 */
		public function set scale(scale:Number):void{
			this._scale = scale;
			if(this.bitmap!=null){
				this.bitmap.width = this.orgImageWidth * scale;
				this.bitmap.height = this.orgImageHeight * scale;
			}
			this.width = this.orgImageWidth * scale;
			this.height = this.orgImageHeight * scale;
		}
		
		/**
		 * 取得图像
		 */
		public function get bitmapData():BitmapData{
			return this.bitmap.bitmapData;
		}
		
		/**
		 * 获取图像的原始宽度
		 */
		public function get orgImageWidth():int{
			return this._orgImageWidth;
		}
		
		/**
		 * 获取图像的原始高度
		 */
		public function get orgImageHeight():int{
			return this._orgImageHeight;
		}
		
		/**
		 * 判断是否显示图像的tooltip
		 */
		public function get showTooltip():Boolean{
			return this._showTooltip;
		}
		
		/**
		 * 设置是否显示图像的tooltip
		 */
		public function set showTooltip(showTooltip:Boolean):void{
			this._showTooltip = showTooltip;
		}
		
		/**
		 * 读取图像
		 */
		protected function loadImage(bitmapData:BitmapData):void{
			bitmap.bitmapData = bitmapData;
			this.scale = scale;
		}
		
		/**
		 * 鼠标移到图像上时触发的事件
		 */
		protected function onMouseOver(evt:MouseEvent):void{
			if(selectable && glow){
				//如果可选,则高亮显示该对象
				DefaultManagerUtil.effectManager.glowEffect.end();
				DefaultManagerUtil.effectManager.glowEffect.target = this;
				DefaultManagerUtil.effectManager.glowEffect.play();
			}
			if(showTooltip && this.object!=null){
				//显示tooltip
				var point:Point = new Point(this.x, this.y);
				point = this.parent.localToGlobal(point);
				DefaultManagerUtil.tooltipManager.setImage(this);
				var newpoint:Point = new Point(point.x, point.y);
				if((point.x + this.width + DefaultManagerUtil.tooltipManager.getTooltipWidth() + 5) > ApplicationUtil.application.width){
					newpoint.x = newpoint.x - DefaultManagerUtil.tooltipManager.getTooltipWidth() - 5;
				}else{
					newpoint.x = newpoint.x + this.width + 5;
				}
				if((point.y + DefaultManagerUtil.tooltipManager.getTooltipHeight()) > ApplicationUtil.application.height){
					newpoint.y = ApplicationUtil.application.height - DefaultManagerUtil.tooltipManager.getTooltipHeight() - 5;
				}else{
				}
				DefaultManagerUtil.tooltipManager.showTooltip(newpoint.x, newpoint.y);
			}
		}
		
		/**
		 * 鼠标移出图像时触发的事件
		 */
		protected function onMouseOut(evt:MouseEvent):void{
			//取消图像的高亮效果
			if(glow){
				DefaultManagerUtil.effectManager.glowEffect.end();
			}
			//隐藏tooltip
			DefaultManagerUtil.tooltipManager.hideTooltip();
		}
		
		/**
		 * 刷新当前发光效果的状态
		 */
		protected function refreshGlowEffect():void{
			if(glow){
				if(selectable){
				}else{
					//否则停止高光
					DefaultManagerUtil.effectManager.glowEffect.end();
				}
			}
		}
		
		/**
		 * 装载图像
		 */
		protected function loadObjectImage():void{
			
		}
		
		/**
		 * 设置图像是否显示颜色效果
		 */
		public function setColorEffect(colorEffect:Boolean):void{
			this._colorEffect = colorEffect;
			if(!this._colorEffect){
				this.filters = [
					new ColorMatrixFilter([
					1,0,0,0,0,
					1,0,0,0,0,
					1,0,0,0,0,
					0,0,0,1,0
					])
				];
			}else{
				this.filters = null;
			}
		}
		
	}
}