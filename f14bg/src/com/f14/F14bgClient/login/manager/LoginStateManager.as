package com.f14.F14bgClient.login.manager
{
	import com.f14.F14bgClient.login.ui.window.RegistWindow;
	import com.f14.F14bgClient.login.ui.window.SetupWindow;
	import com.f14.f14bg.manager.StateManager;
	
	import mx.containers.Canvas;
	
	public class LoginStateManager extends StateManager
	{
		public var registWindow:RegistWindow;
		public var setupWindow:SetupWindow;
		
		public function LoginStateManager(){
			super();
		}
		
		/**
		 * 显示用户注册窗口
		 */
		public function showRegistWindow():void{
			if(registWindow==null){
				registWindow = new RegistWindow();
				new Canvas().addChild(registWindow);
			}
			registWindow.clear();
			registWindow.show();
		}
		
		/**
		 * 关闭用户注册窗口
		 */
		public function hideRegistWindow():void{
			if(registWindow!=null){
				registWindow.cancel();
			}
		}
		
		/**
		 * 显示设置窗口
		 */
		public function showSetupWindow():void{
			if(setupWindow==null){
				setupWindow = new SetupWindow();
				new Canvas().addChild(setupWindow);
			}
			setupWindow.show();
			setupWindow.loadSetup();
		}
		
		/**
		 * 关闭设置窗口
		 */
		public function hideSetupWindow():void{
			if(setupWindow!=null){
				setupWindow.cancel();
			}
		}
	}
}