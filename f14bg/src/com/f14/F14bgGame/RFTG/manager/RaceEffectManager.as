package com.f14.F14bgGame.RFTG.manager
{
	import com.f14.F14bgGame.bg.manager.EffectManager;
	
	import mx.effects.Zoom;

	public class RaceEffectManager extends EffectManager
	{
		public function RaceEffectManager()
		{
			super();
		}
		
		protected var _zoomEffect:Zoom = new Zoom();
		
		/**
		 * 初始化所有效果
		 */
		override protected function init():void{
			super.init();
		}
		
		public function get zoomEffect():Zoom{
			return this._zoomEffect;
		}
	}
}