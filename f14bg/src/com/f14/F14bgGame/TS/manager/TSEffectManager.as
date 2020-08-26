package com.f14.F14bgGame.TS.manager
{
	import com.f14.F14bgGame.bg.manager.EffectManager;
	
	import mx.effects.Glow;

	public class TSEffectManager extends EffectManager
	{
		public function TSEffectManager()
		{
			super();
		}
		
		/**
		 * 初始化所有效果
		 */
		override protected function init():void{
			super.init();
			
			_glowEffect.color = 0xffff00;
			_glowEffect.duration = 10000000;
			_glowEffect.blurXFrom = 2;
			_glowEffect.blurXTo = 2;
			_glowEffect.blurYFrom = 2;
			_glowEffect.blurYTo = 2;
			_glowEffect.strength = 255;
		}
		
	}
}