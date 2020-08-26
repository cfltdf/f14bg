package com.f14.F14bgClient.login
{
	import com.f14.F14bgClient.login.manager.LoginActionManager;
	import com.f14.F14bgClient.login.manager.LoginStateManager;
	
	/**
	 * 登录模块用的辅助类
	 */
	public class LoginUtil
	{
		public static var module:LoginModule;
		public static var actionManager:LoginActionManager;
		public static var stateManager:LoginStateManager;
		
		public static function init():void{
			actionManager = new LoginActionManager();
			stateManager = new LoginStateManager();
		}
		
		/**
		 * 装载客户端主程序的版本信息
		 */
		public static function loadVersion():void{
			var version:Object;
		}

	}
}