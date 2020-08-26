package com.f14.F14bgClient.room.manager
{
	import com.f14.F14bgClient.room.RoomUtil;
	import com.f14.F14bgClient.room.ui.part.ReportBoard;
	import com.f14.F14bgClient.room.ui.part.SetupBoard;
	import com.f14.F14bgClient.room.ui.part.TimeBoard;
	import com.f14.F14bgClient.room.ui.window.ChatWindow;
	import com.f14.F14bgClient.room.ui.window.ConfigWindow;
	import com.f14.F14bgClient.room.ui.window.ReportWindow;
	import com.f14.F14bgClient.room.ui.window.ScoreWindow;
	import com.f14.core.util.ApplicationUtil;
	import com.f14.f14bg.manager.StateManager;
	import com.f14.f14bg.ui.window.AboutWindow;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	/**
	 * 界面管理器
	 */
	public class RoomStateManager extends StateManager
	{
		public function RoomStateManager()
		{
			super();
		}
		
		/**
		 * 游戏设置窗口
		 */
		public var configWindow:ConfigWindow;
		/**
		 * 得分窗口
		 */
		public var scoreWindow:ScoreWindow;
		/**
		 * 战报窗口
		 */
		public var reportWindow:ReportWindow;
		/**
		 * 聊天窗口
		 */
		public var chatWindow:ChatWindow;
		/**
		 * 关于窗口
		 */
		public var aboutWindow:AboutWindow;
		
		/**
		 * 初始化
		 */
		override public function init():void{
			super.init();
			
			this.configWindow = new ConfigWindow();
			this.cav.addChild(this.configWindow);
			this.aboutWindow = new AboutWindow();
			this.cav.addChild(this.aboutWindow);
			this.initControlBar();
		}
		
		/**
		 * 初始化控制条相关控件
		 */
		public function initControlBar():void{
			//创建聊天窗口
			this.chatWindow = new ChatWindow();
			this.chatWindow.width = 200;
			this.chatWindow.height = RoomUtil.application.height - 30;
			RoomUtil.application.f14mdi.addWindow(this.chatWindow);
			//RoomUtil.application.f14mdi.minimizWindow(this.chatWindow);
			//设置聊天窗口的默认位置
			this.chatWindow.x = ApplicationUtil.application.width - this.chatWindow.width;
			
			//创建得分面板
			this.scoreWindow = new ScoreWindow();
			RoomUtil.application.f14mdi.addWindow(this.scoreWindow);
			RoomUtil.application.f14mdi.minimizWindow(this.scoreWindow);
			this.scoreWindow.initBoards();
			
			//创建战报面板
			this.reportWindow = new ReportWindow();
			RoomUtil.application.f14mdi.addWindow(this.reportWindow);
			RoomUtil.application.f14mdi.minimizWindow(this.reportWindow);
			this.reportWindow.initReportBoard(this.createReportBoard());
		}
		
		/**
		 * 创建新的战报面板
		 */
		protected function createReportBoard():ReportBoard{
			return new ReportBoard();
		}
		
		/**
		 * 重置所有窗口的信息
		 */
		public function reset():void{
			this.configWindow.reset();
		}
		
		/**
		 * 清除所有窗口内容
		 */
		public function clear():void{
			//清除公共窗口的内容
			this.scoreWindow.clear();
			this.reportWindow.clear();
		}
		
		/**
		 * 隐藏所有游戏中用到的面板
		 */
		public function hideGameBoard():void{
			this.hideConfigWindow();
			this.closeScoreWindow();
		}
		
		/**
		 * 显示游戏设置窗口
		 */
		public function showConfigWindow():void{
			configWindow.show(false);
		}
		
		/**
		 * 隐藏游戏设置窗口
		 */
		public function hideConfigWindow():void{
			configWindow.cancel();
		}
		
		/**
		 * 开关设置功能窗口
		 */
		public function trigConfigWindow():void{
			configWindow.trig(false);
		}
		
		/**
		 * 设置游戏的设置面板
		 */
		public function set setupBoard(setupBoard:SetupBoard):void{
			this.configWindow.initSetupBoard(setupBoard);
		}
		
		/**
		 * 显示得分面板
		 */
		public function showScoreWindow(param:Object):void{
			/* if(scoreBoard==null){
				scoreBoard = new ScoreBoard();
				this.canvas_float.windowManager.add(scoreBoard);
				scoreBoard.addEventListener(MDIWindowEvent.CLOSE, closeScoreBoard);
				scoreBoard.initBoards();
			} */
			RoomUtil.application.f14mdi.showWindow(this.scoreWindow);
			scoreWindow.clear();
			scoreWindow.showStage("score");
			scoreWindow.setScoreList(param);
			//scoreBoard.visible = true;
			//显示得分面板时会隐藏设置窗口
			//this.hideConfigWindow();
		}
		
		/**
		 * 关闭得分面板
		 */
		public function closeScoreWindow(evt:Event=null):void{
			/* if(scoreBoard!=null){
				scoreBoard.visible = false;
				this.canvas_float.windowManager.remove(scoreBoard);
				scoreBoard = null;
			} */
			RoomUtil.application.f14mdi.minimizWindow(this.scoreWindow);
			//关闭得分面板时将显示设置窗口
			//this.showConfigWindow();
		}
		
		/**
		 * 取得时钟部件
		 */
		public function get timeBoard():TimeBoard{
			return RoomUtil.application.f14mdi.controlBar.timeBoard;
		}
		
		/**
		 * 显示版本信息窗口
		 */
		public function showAboutWindow():void{
			this.aboutWindow.show();
		}
		
		/**
		 * 隐藏版本信息窗口
		 */
		public function hideAboutWindow():void{
			this.aboutWindow.cancel();
		}
		
		/**
		 * 显示/隐藏版本信息窗口
		 */
		public function trigAboutWindow(evt:MouseEvent=null):void{
			//入参在click时会用到
			this.aboutWindow.trig();
		}
		
		/**
		 * 创建版本信息窗口
		 */
		public function createAboutWindow():void{
			//创建版本信息的最小化版块
			RoomUtil.application.f14mdi.createMiniTile("版本号: " + ApplicationUtil.version.version, trigAboutWindow);
			this.aboutWindow.version = ApplicationUtil.version;
		}
		
	}
}