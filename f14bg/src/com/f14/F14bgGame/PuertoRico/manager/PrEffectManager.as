package com.f14.F14bgGame.PuertoRico.manager
{
	import com.f14.F14bgGame.bg.manager.EffectManager;
	
	import mx.effects.Glow;

	public class PrEffectManager extends EffectManager
	{
		public function PrEffectManager()
		{
			super();
		}
		
		protected var _normalEffect:Glow;
		
		/**
		 * 初始化所有效果
		 */
		override protected function init():void{
			super.init();
			/*_selectEffect = new Glow();
			_selectEffect.color = 0xffff00;
			_selectEffect.duration = 10000000;
			_selectEffect.strength = 10;
			//_selectEffect.blurXFrom = 20;
			//_selectEffect.blurXTo = 1;
			//_selectEffect.blurYFrom = 20;
			//_selectEffect.blurYTo = 1;
			//_selectEffect.alphaFrom = 0;
			//_selectEffect.alphaTo = 1;*/
			
			_activeEffect = new Glow();
			_activeEffect.color = 0xff0000;
			_activeEffect.duration = 10000000;
			
			_waitForInputEffect = new Glow();
			_waitForInputEffect.color = 0xff0000;
			_waitForInputEffect.duration = 10000000;
			_responsedEffect = new Glow();
			_responsedEffect.color = 0x00ff00;
			_responsedEffect.duration = 10000000;
			
			_normalEffect = new Glow();
			_normalEffect.color = 0xffff00;
			_normalEffect.duration = 10000000;
		}
		
		public function get normalEffect():Glow{
			return this._normalEffect;
		}
		
	}
}