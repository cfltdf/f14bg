package com.f14.F14bgGame.bg.ui
{
	import com.f14.F14bgClient.room.RoomUtil;
	import com.f14.F14bgClient.room.ui.part.SetupBoard;
	import com.f14.F14bgGame.bg.handler.InGameHandler;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.player.Player;
	import com.f14.core.controls.module.F14module;
	import com.f14.core.util.ApplicationUtil;
	
	import mx.containers.Canvas;
	import mx.styles.StyleManager;
	
	public class F14GameModule extends F14module
	{
		public function F14GameModule()
		{
			super();
		}
		
		protected var _gameCommandHandler:InGameHandler;
		protected var _mainBoard:MainBoard;
		protected var _imageCanvas:Canvas;
		protected var _mask:Canvas;
		
		override public function init():void{
			super.init();
			//创建图片的缓存容器
			this._imageCanvas = new Canvas();
			this._imageCanvas.width = 0;
			this._imageCanvas.height = 0;
			this._imageCanvas.visible = false;
			this.addChild(this._imageCanvas);
			this._gameCommandHandler = this.createGameCommandHandler();
			//设置游戏设置窗口
			RoomUtil.stateManager.setupBoard = this.createSetupBoard();
			//创建版本信息按钮
			RoomUtil.stateManager.createAboutWindow();
			this.loadStyle();
		}
		
		protected function createGameCommandHandler():InGameHandler{
			return null;
		}
		
		protected function createSetupBoard():SetupBoard{
			return null;
		}
		
		public function set mainBoard(mainBoard:MainBoard):void{
			this._mainBoard = mainBoard;
		}
		
		public function get mainBoard():MainBoard{
			return this._mainBoard;
		}
		
		public function get imageCanvas():Canvas{
			return this._imageCanvas;
		}
		
		public function clear():void{
			this.mainBoard.clear();
		}
		
		public function createPlayer():Player{
			return new Player();
		}
		
		/**
		 * 创建指定玩家的面板
		 */
		public function createPlayerBoard(player:Player):void{
			this.mainBoard.createPlayerBoard(player);
		}
		
		/**
		 * 刷新主游戏界面的大小
		 */
		public function refreshMainBoardSize():void{
			this.mainBoard.refreshMainBoardSize();
		}
		
		/**
		 * 设置界面输入的状态
		 */
		public function setInputState(code:int):void{
			this.mainBoard.setInputState(code);
		}
		
		/**
		 * 禁止所有的输入
		 */
		public function disableAllInput():void{
			this.mainBoard.disableAllInput();
		}
		
		/**
		 * 取得指令处理器
		 */
		public function get gameCommandHandler():InGameHandler{
			return this._gameCommandHandler;
		}
		
		/**
		 * 装载样式文件
		 */
		protected function loadStyle():void{
			try{
				StyleManager.loadStyleDeclarations(ApplicationUtil.basePath + "style.swf");
			}catch(e:*){
			}
		}
		
		/**
		 * 图片资源装载完成时的回调函数
		 */
		public function onImageLoadComplete():void{
			this.removeLoadingMask();
		}
		
		/**
		 * 创建读取图片资源时的遮盖层
		 */
		protected function createLoadingMask():void{
			this._mask = new Canvas();
			this._mask.width = this.width;
			this._mask.height = this.height;
			this._mask.setStyle("backgroundColor", "#000000");
			this._mask.setStyle("backgroundAlpha", 0.4);
			this.addChild(this._mask);
		}
		
		/**
		 * 移除读取图片资源时的遮盖层
		 */
		protected function removeLoadingMask():void{
			this.removeChild(this._mask);
		}
		
	}
}