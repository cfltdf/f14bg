package com.f14.F14bgGame.bg.manager
{
	import flash.events.EventDispatcher;
	import flash.events.MouseEvent;
	import flash.filters.ColorMatrixFilter;
	
	import mx.core.UIComponent;
	import mx.effects.Glow;
	import mx.effects.IEffectInstance;
	
	
	/**
	 * 效果管理器
	 */
	public class EffectManager
	{
		/**
		 * 黑白滤镜
		 */
		protected var grayFilter:Array = [new ColorMatrixFilter([0.5,0.5,0.5,0,0,  0.5,0.5,0.5,0,0,  0.5,0.5,0.5,0,0,  0,0,0,1,0])];
		
		public function EffectManager()
		{
			this.init();
		}
		
		/**
		 * 高亮显示效果
		 */
		protected var _glowEffect:Glow = new Glow();
		/**
		 * 聚焦显示效果
		 */
		protected var _selectEffect:Glow = new Glow();
		/**
		 * 激活显示效果
		 */
		protected var _activeEffect:Glow = new Glow();
		
		/**
		 * 等待输入时的效果
		 */
		protected var _waitForInputEffect:Glow = new Glow();
		/**
		 * 完成输入时的效果
		 */
		protected var _responsedEffect:Glow = new Glow();
		
		/**
		 * 初始化所有效果
		 */
		protected function init():void{
			_glowEffect.color = 0xffff00;
			_glowEffect.duration = 10000000;
			_glowEffect.blurXFrom = 20;
			_glowEffect.blurXTo = 20;
			_glowEffect.blurYFrom = 20;
			_glowEffect.blurYTo = 20;
			
			_selectEffect.color = 0xff00ff;
			_selectEffect.duration = 10000000;
			_selectEffect.blurXFrom = 20;
			_selectEffect.blurXTo = 20;
			_selectEffect.blurYFrom = 20;
			_selectEffect.blurYTo = 20;
			
			_activeEffect.color = 0xff0000;
			_activeEffect.duration = 10000000;
			_activeEffect.blurXFrom = 20;
			_activeEffect.blurXTo = 20;
			_activeEffect.blurYFrom = 20;
			_activeEffect.blurYTo = 20;
			
			_waitForInputEffect.color = 0xff0000;
			_waitForInputEffect.duration = 10000000;
			_waitForInputEffect.blurXFrom = 10;
			_waitForInputEffect.blurXTo = 10;
			_waitForInputEffect.blurYFrom = 10;
			_waitForInputEffect.blurYTo = 10;
			_responsedEffect.color = 0x00ff00;
			_responsedEffect.duration = 10000000;
			_responsedEffect.blurXFrom = 10;
			_responsedEffect.blurXTo = 10;
			_responsedEffect.blurYFrom = 10;
			_responsedEffect.blurYTo = 10;
		}
		
		/**
		 * 取得高亮显示的效果
		 */
		public function get glowEffect():Glow{
			return this._glowEffect;
		}
		
		/**
		 * 取得聚焦显示的效果
		 */
		public function get selectEffect():Glow{
			return this._selectEffect;
		}
		
		/**
		 * 取得激活显示的效果
		 */
		public function get activeEffect():Glow{
			return this._activeEffect;
		}
		
		/**
		 * 取得等待输入的效果
		 */
		public function get waitForInputEffect():Glow{
			return this._waitForInputEffect;
		}
		
		/**
		 * 取得输入完成的效果
		 */
		public function get responsedEffect():Glow{
			return this._responsedEffect;
		}
		
		/**
		 * 为目标创建等待输入的效果实例
		 */
		public function createInputingEffectInstance(target:Object):IEffectInstance{
			return this._waitForInputEffect.createInstance(target);
		}
		
		/**
		 * 为目标创建完成输入的效果实例
		 */
		public function createResponsedEffectInstance(target:Object):IEffectInstance{
			return this._responsedEffect.createInstance(target);
		}
		
		/**
		 * 为对象添加鼠标移上去后的高亮效果,该对象必须用selectable的属性
		 */
		public function addHighlight(obj:EventDispatcher):void{
			obj.addEventListener(MouseEvent.MOUSE_OVER, startHighlight);
			obj.addEventListener(MouseEvent.MOUSE_OUT, endHighlight);
		}
		
		/**
		 * 鼠标移到对象上时高亮显示
		 */
		protected function startHighlight(evt:MouseEvent):void{
			var obj:Object = evt.currentTarget;
			if(obj.selectable){
				//如果可选,则高亮显示该对象
				this.glowEffect.end();
				this.glowEffect.target = obj;
				this.glowEffect.play();
			}
		}
		
		/**
		 * 鼠标移出对象时取消高亮
		 */
		protected function endHighlight(evt:MouseEvent):void{
			//取消图像的高亮效果
			this.glowEffect.end();
		}
		
		/**
		 * 设置黑白滤镜
		 */
		public function setGrayFilter(o:UIComponent):void{
			o.filters = this.grayFilter;
		}
		
	}
}