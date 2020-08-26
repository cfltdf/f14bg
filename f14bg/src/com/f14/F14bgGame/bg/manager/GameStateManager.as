package com.f14.F14bgGame.bg.manager
{
	import com.f14.F14bgClient.room.RoomUtil;
	import com.f14.F14bgGame.bg.ui.window.AlertWindow;
	import com.f14.F14bgGame.bg.ui.window.ConfirmWindow;
	import com.f14.F14bgGame.bg.ui.window.DefaultAlertWindow;
	import com.f14.F14bgGame.bg.ui.window.SimpleConfirmWindow;
	import com.f14.f14bg.manager.StateManager;
	
	/**
	 * 界面管理器
	 */
	public class GameStateManager extends StateManager
	{
		public function GameStateManager()
		{
			super();
		}
		
		/**
		 * 当前界面代码
		 */
		public var stateCode:int;
		/**
		 * 提示信息窗口
		 */
		public var alertWindow:AlertWindow;
		/**
		 * 确认窗口数组
		 */
		protected var confirmWindows:Array = new Array();
		/**
		 * 当前执行的确认窗口
		 */
		public var currentConfirmWindow:ConfirmWindow;
		
		/**
		 * 通用的确认窗口
		 */
		public var simpleConfirmWindow:SimpleConfirmWindow;
		
		/**
		 * 初始化
		 */
		override public function init():void{
			super.init();
			
			this.alertWindow = this.createAlertWindow();
			this.cav.addChild(this.alertWindow);
			
			this.simpleConfirmWindow = new SimpleConfirmWindow();
			confirmWindows.push(this.simpleConfirmWindow);
			this.cav.addChild(this.simpleConfirmWindow);
		}
		
		/**
		 * 创建提示窗口
		 */
		protected function createAlertWindow():AlertWindow{
			return new DefaultAlertWindow();
		}
		
		/**
		 * 重置所有窗口的信息
		 */
		public function reset():void{
			
		}
		
		/**
		 * 清除所有窗口内容
		 */
		public function clear():void{
			//清除公共窗口的内容
			this.alertWindow.clear();
			
			this.currentConfirmWindow = null;
			for each(var win:ConfirmWindow in confirmWindows){
				win.clear();
			}
		}
		
		/**
		 * 隐藏所有游戏中用到的面板
		 */
		public function hideGameBoard():void{
			this.alertWindow.cancel();
			
			for each(var win:ConfirmWindow in confirmWindows){
				win.cancel();
			}
		}
		
		/**
		 * 提示信息
		 */
		public function alert(msg:String, param:Object=null):void{
			this.alertWindow.loadParam(msg, param);
			this.alertWindow.show(false);
		}
		
		/**
		 * 设置玩家当前的界面状态变化时触发的方法
		 */
		public function onStateChange(stateCode:int, active:Boolean, param:Object):void{
			//普通监听器
			this.stateCode = stateCode;
			if(DefaultManagerUtil.gameManager.isLocalParam(param)){
				if(active){
					//如果是当前玩家回合激活,则显示对应阶段的输入状态
					RoomUtil.gameModule.setInputState(stateCode);
				}else{
					//否则则禁用所有的输入
					RoomUtil.gameModule.disableAllInput();
				}
			}
		}
		
		/**
		 * 设置当前的阶段变化时触发的方法
		 */
		public function onPhaseChange(stateCode:int, active:Boolean, param:Object):void{
			//普通监听器
			this.stateCode = stateCode;
			//阶段变化时将禁用所有的输入状态
			RoomUtil.gameModule.disableAllInput();
			//ApplicationManager.application.disableAllInput();
		}
		
		/**
		 * 中断型监听器阶段开始/结束监听时触发的方法
		 */
		public function onInterruptPhase(stateCode:int, active:Boolean, param:Object):void{
			
		}
		
		/**
		 * 中断型监听器中玩家状态开始/结束监听时触发的方法
		 */
		public function onInterruptState(stateCode:int, active:Boolean, param:Object):void{

		}
		
		/**
		 * 通用中断窗口
		 */
		protected function commonInterruptWindow(win:ConfirmWindow, active:Boolean, param:Object):void{
			if(DefaultManagerUtil.gameManager.isLocalParam(param)){
				//只有本地玩家才处理该事件
				if(active){
					//显示窗口,并加载参数
					win.loadParam(param);
					win.show(false);
					this.currentConfirmWindow = win;
				}else{
					//关闭窗口
					win.cancel();
					this.currentConfirmWindow = null;
				}
			}
		}
		
	}
}