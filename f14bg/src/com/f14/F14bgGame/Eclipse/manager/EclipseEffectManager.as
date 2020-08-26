package com.f14.F14bgGame.Eclipse.manager
{
	import com.f14.F14bgGame.bg.manager.EffectManager;
	
	import flash.filters.ColorMatrixFilter;
	

	public class EclipseEffectManager extends EffectManager
	{
		public function EclipseEffectManager()
		{
			super();
			
			this.grayFilter = [new ColorMatrixFilter([0.5,0.5,0.5,0,0,  0.5,0.5,0.5,0,0,  0.5,0.5,0.5,0,0,  0,0,0,0.3,0])];
		}
		
	}
}