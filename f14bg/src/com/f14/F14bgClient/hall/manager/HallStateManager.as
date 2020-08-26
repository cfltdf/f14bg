package com.f14.F14bgClient.hall.manager
{
	import com.f14.F14bgClient.hall.ui.window.CreateRoomWindow;
	import com.f14.F14bgClient.hall.ui.window.PasswordWindow;
	import com.f14.core.util.ApplicationUtil;
	import com.f14.f14bg.manager.StateManager;
	import com.f14.f14bg.ui.window.AboutWindow;
	
	public class HallStateManager extends StateManager
	{
		public var createRoomWindow:CreateRoomWindow;
		public var passwordWindow:PasswordWindow;
		public var aboutWindow:AboutWindow;
		
		public function HallStateManager(){
			super();
		}
		
		/**
		 * 初始化
		 */
		override public function init():void{
			super.init();
			createRoomWindow = new CreateRoomWindow();
			cav.addChild(createRoomWindow);
			
			passwordWindow = new PasswordWindow();
			cav.addChild(passwordWindow);
			
			this.aboutWindow = new AboutWindow();
			this.cav.addChild(this.aboutWindow);
		}
		
		/**
		 * 显示创建房间的窗口
		 */
		public function showCreateRoomWindow():void{
			createRoomWindow.clear();
			createRoomWindow.show();
		}
		
		/**
		 * 关闭创建房间的窗口
		 */
		public function hideCreateRoomWindow():void{
			if(createRoomWindow!=null){
				createRoomWindow.cancel();
			}
		}
		
		/**
		 * 显示密码输入窗口
		 */
		public function showPasswordWindow(roomId:String):void{
			passwordWindow.clear();
			passwordWindow.roomId = roomId;
			passwordWindow.show(true);
		}
		
		/**
		 * 关闭密码输入窗口
		 */
		public function hidePasswordWindow():void{
			if(passwordWindow!=null){
				passwordWindow.cancel();
			}
		}
		
		/**
		 * 显示关于窗口
		 */
		public function showAboutWindow():void{
			this.aboutWindow.version = ApplicationUtil.version;
			this.aboutWindow.show();
		}
		
		/**
		 * 隐藏关于窗口
		 */
		public function hideAboutWindow():void{
			if(this.aboutWindow!=null){
				this.aboutWindow.cancel();
			}
		}
		
	}
}