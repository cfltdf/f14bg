package com.f14.F14bgGame.PuertoRico.manager
{
	import com.f14.f14bg.manager.ResourceManager;
	
	/**
	 * 资源管理器
	 */
	public class PrResourceManager extends ResourceManager
	{
		public function PrResourceManager(){
			super();
		}
		
		/**
		 * 装载资源
		 */
		override public function load(param:Object):void{
			var obj:Object;
			for each(obj in param.characterCards){
				putObject(obj);
			}
			for each(obj in param.plantations){
				putObject(obj);
			}
			for each(obj in param.quarries){
				putObject(obj);
			}
			for each(obj in param.buildings){
				putObject(obj);
			}
			for each(obj in param.forest){
				putObject(obj);
			}
		}
		
	}
}