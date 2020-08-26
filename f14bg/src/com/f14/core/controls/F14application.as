package com.f14.core.controls
{
	import cn.smartinvoke.executor.Executor;
	
	import com.f14.core.util.ApplicationUtil;
	import com.f14.net.smartinvoke.ClientCmdHandler;
	import com.f14.net.smartinvoke.QueryCommandHandler;
	import com.f14.net.smartinvoke.UpdateCommandHandler;
	
	import mx.core.Application;
	import mx.events.ResizeEvent;
	import mx.modules.ModuleLoader;

	public class F14application extends Application
	{
		public var clientCmdHandler:ClientCmdHandler;
		public var updateCommandHandler:UpdateCommandHandler;
		public var queryCommandHandler:QueryCommandHandler;
		protected var _loader:ModuleLoader;
		
		public function F14application()
		{
			super();
			this.init();
		}
		
		public function init():void{
			Executor.init();
			ApplicationUtil.init();
			ApplicationUtil.application = this;
			
			this._loader = new ModuleLoader();
			this._loader.width = this.width;
			this._loader.height = this.height;
			this.addChild(this.loader);
			this.addEventListener(ResizeEvent.RESIZE, onResize);
		}
		
		public function get loader():ModuleLoader{
			return this._loader;
		}
		
		/**
		 * 同步module的大小
		 */
		protected function onResize(evt:ResizeEvent):void{
			this.loader.width = this.width;
			this.loader.height = this.height;
		}
		
	}
}