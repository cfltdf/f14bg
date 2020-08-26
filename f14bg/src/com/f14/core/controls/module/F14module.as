package com.f14.core.controls.module
{
	import com.f14.core.util.ApplicationUtil;
	import com.f14.core.util.Version;
	
	import mx.modules.Module;

	public class F14module extends Module
	{
		private var _version:Version;
		
		public function F14module()
		{
			super();
			this._version = this.createVersion();
			ApplicationUtil.version = this.version;
		}
		
		public function init():void{
			
		}
		
		public function get version():Version{
			return this._version;
		}
		
		/**
		 * 创建version对象
		 */
		protected function createVersion():Version{
			return new Version();
		}
		
		/**
		 * 装载版本信息
		 */
		protected function loadVersionInfo():void{
			var o:Object = ApplicationUtil.commonHandler.getVersionInfo(ApplicationUtil.gameType);
			//暂时只取版本号
			this._version.version = o.version;
			this._version.title = o.title;
		}
	}
}