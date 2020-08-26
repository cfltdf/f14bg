package com.f14.f14bg.manager
{
	import mx.containers.Canvas;
	
	public class StateManager
	{
		/**
		 * 临时canvas
		 */
		protected var cav:Canvas;
		
		public function StateManager(){
			this.init();
		}
		
		/**
		 * 初始化
		 */
		public function init():void{
			this.cav = new Canvas();
		}
		
	}
}