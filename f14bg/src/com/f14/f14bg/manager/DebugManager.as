package com.f14.f14bg.manager
{
	import com.f14.core.util.ApplicationUtil;
	import com.f14.f14bg.ui.DebugWindow;
	
	import mx.containers.Canvas;
	
	public class DebugManager
	{
		
		/**
		 * 临时canvas
		 */
		protected var cav:Canvas;
		protected var _debug:Boolean = false;
		protected var _debugWindow:DebugWindow;
		
		public function DebugManager()
		{
			this.init();
		}
		
		/**
		 * 初始化
		 */
		public function init():void{
			//设置是否调试模式的状态
			var debugString:String = ApplicationUtil.commonHandler.getConfigValue("debug");
			this._debug = debugString=="true"?true:false;
			
			if(debug){
				//只有当调试模式打开时,才会初始化调试窗口
				this.cav = new Canvas();
			
				this._debugWindow = new DebugWindow();
				this.cav.addChild(this.debugWindow);
			}
		}
		
		public function get debug():Boolean{
			return this._debug;
		}
		
		public function get debugWindow():DebugWindow{
			return this._debugWindow;
		}
		
		/**
		 * 显示调试窗口
		 */
		public function showDebugWindow():void{
			this.debugWindow.show(false);
		}
		
		/**
		 * 隐藏调试窗口
		 */
		public function hideDebugWindow():void{
			this.debugWindow.cancel();
		}
		
		/**
		 * 打印错误信息到调试窗口
		 */
		public function printError(e:Error, cmdstr:String):void{
			if(debug){
				this.debugWindow.printError(e, cmdstr);
				this.showDebugWindow();
			}
		}
		
		/**
		 * 打印调试信息到调试窗口
		 */
		public function printDebug(msg:Object, title:String=null):void{
			if(debug){
				this.debugWindow.printDebug(msg, title); 
				this.showDebugWindow();
			}
		}

	}
}