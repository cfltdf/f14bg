package com.f14.F14bgGame.Eclipse.manager
{
	import com.f14.F14bgGame.Eclipse.EclipseUtil;
	import com.f14.F14bgGame.Eclipse.consts.EclipseGameCmd;
	import com.f14.F14bgGame.Eclipse.ui.window.EclipseChoosePartWindow;
	import com.f14.F14bgGame.Eclipse.ui.window.EclipseChoosePopulationWindow;
	import com.f14.F14bgGame.Eclipse.ui.window.EclipseChooseUnitWindow;
	import com.f14.F14bgGame.Eclipse.ui.window.EclipsePlayerWindow;
	import com.f14.F14bgGame.Eclipse.ui.window.EclipsePublicWindow;
	import com.f14.F14bgGame.Eclipse.ui.window.EclipseRemoveInfluenceConfirmWindow;
	import com.f14.F14bgGame.Eclipse.ui.window.EclipseSelectInfluenceDiscWindow;
	import com.f14.F14bgGame.Eclipse.ui.window.EclipseTradeWindow;
	import com.f14.F14bgGame.bg.consts.InputState;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.manager.GameStateManager;
	import com.f14.F14bgGame.bg.ui.window.ConfirmWindow;
	
	import flash.geom.Point;
	
	import mx.containers.Canvas;

	public class EclipseStateManager extends GameStateManager
	{
		public function EclipseStateManager()
		{
			super();
			this.uimanager = new EclipseUIManager();			
		}
		
		protected var uimanager:EclipseUIManager;
		protected var _playerWindows:Array = new Array();
		
		protected var commonWindowPosition:Point;
		public var publicWindow:EclipsePublicWindow;
		
		public var chooseUnitWindow:EclipseChooseUnitWindow;
		public var choosePopulationWindow:EclipseChoosePopulationWindow;
		public var choosePartWindow:EclipseChoosePartWindow;
		public var removeInfluenceConfirmWindow:EclipseRemoveInfluenceConfirmWindow;
		public var selectDiscWindow:EclipseSelectInfluenceDiscWindow;
		public var tradeWindow:EclipseTradeWindow;
	
		/**
		 * 初始化
		 */
		override public function init():void{
			super.init();
			
			publicWindow = new EclipsePublicWindow();
			chooseUnitWindow = new EclipseChooseUnitWindow();
			choosePopulationWindow = new EclipseChoosePopulationWindow();
			choosePartWindow = new EclipseChoosePartWindow();
			removeInfluenceConfirmWindow = new EclipseRemoveInfluenceConfirmWindow();
			selectDiscWindow = new EclipseSelectInfluenceDiscWindow();
			tradeWindow = new EclipseTradeWindow();
			
			confirmWindows.push(publicWindow);
			confirmWindows.push(chooseUnitWindow);
			confirmWindows.push(choosePopulationWindow);
			confirmWindows.push(choosePartWindow);
			confirmWindows.push(removeInfluenceConfirmWindow);
			confirmWindows.push(selectDiscWindow);
			confirmWindows.push(tradeWindow);
			
			for each(var win:ConfirmWindow in confirmWindows){
				this.cav.addChild(win);
				win.initComponents();
			}
		}
		
		/**
		 * 清除所有窗口内容
		 */
		override public function clear():void{
			super.clear();
			this._playerWindows = new Array();
			commonWindowPosition = null;
		}
		
		/**
		 * 隐藏所有游戏中用到的面板
		 */
		override public function hideGameBoard():void{
			super.hideGameBoard();
			
			//隐藏所有玩家的游戏面板窗口
			for each(var win:EclipsePlayerWindow in this._playerWindows){
				if(win!=null){
					win.cancel();
				}
			}
		}
		
		/**
		 * 取得玩家窗口,如果不存在,则创建
		 */
		public function getPlayerWindow(position:int):EclipsePlayerWindow{
			var win:EclipsePlayerWindow = this._playerWindows[position];
			if(win==null){
				win = new EclipsePlayerWindow();
				this._playerWindows[position] = win;
				new Canvas().addChild(win);
				win.playerBoard.init();
			}
			return win;
		}
		
		/**
		 * 显示/隐藏玩家面板的窗口
		 */
		public function trigPlayerWindow(position:int):void{
			var win:EclipsePlayerWindow = this.getPlayerWindow(position);
			win.trig(false);
		}
		
		/**
		 * 显示玩家面板的窗口
		 */
		public function showPlayerWindow(position:int):void{
			var win:EclipsePlayerWindow = this.getPlayerWindow(position);
			win.show(false);
		}
		
		/**
		 * 设置玩家当前的界面状态变化时触发的方法
		 */
		override public function onStateChange(stateCode:int, active:Boolean, param:Object):void{
			//普通监听器
			this.stateCode = stateCode;
			if(DefaultManagerUtil.gameManager.isLocalParam(param)){
				if(active){
					//如果是当前玩家回合激活,则显示对应阶段的输入状态
					this.uimanager.setInputStateWithParam(stateCode, param);
				}else{
					//否则则禁用所有的输入
					EclipseUtil.mainBoard.disableAllInput();
				}
			}
		}
		
		/**
		 * 中断型监听器阶段开始/结束监听时触发的方法
		 */
		override public function onInterruptPhase(stateCode:int, active:Boolean, param:Object):void{
			//通用的界面处理
			if(active){
				//中断监听器开始时将禁用所有的输入状态
				EclipseUtil.module.disableAllInput();
			}else{
				if(EclipseUtil.gameManager.isLocalPosition(param.trigPlayerPosition)){
					//显示默认的按键面板
					EclipseUtil.getLocalPlayer().playerBoard.inputState = InputState.DEFAULT;
				}else{
					//禁用所有的输入
					EclipseUtil.mainBoard.disableAllInput();
				}
			}
		}
		
		/**
		 * 中断型监听器中玩家状态开始/结束监听时触发的方法
		 */
		override public function onInterruptState(stateCode:int, active:Boolean, param:Object):void{
			//先隐藏所有的窗口
			this.hideGameBoard();
			switch(stateCode){
				case EclipseGameCmd.GAME_CODE_ACTION_EXPLORE:	//探索阶段
				case EclipseGameCmd.GAME_CODE_ACTION_BUILD:	//建造阶段
				case EclipseGameCmd.GAME_CODE_ACTION_MOVE:	//移动阶段
				case EclipseGameCmd.GAME_CODE_ACTION_INFLUENCE:	//扩张阶段
				case EclipseGameCmd.GAME_CODE_ACTION_RESEARCH:	//科研阶段
				case EclipseGameCmd.GAME_CODE_ACTION_UPGRADE:	//升级阶段
				case EclipseGameCmd.GAME_CODE_ACTION_SHIPPART:	//选择部件阶段
				case EclipseGameCmd.GAME_CODE_ACTION_COLONY:	//殖民阶段
					this.setInterruptState(stateCode, active, param);
					break;
			}
			switch(stateCode){
				case EclipseGameCmd.GAME_CODE_ACTION_INFLUENCE:	//扩张阶段
					switch(param.subPhase){
						case 1:	//选择是否收回影响力
							this.commonInterruptWindow(this.removeInfluenceConfirmWindow, active, param);
							break;
						case 2:	//选择影响力圆片的来源
							this.commonInterruptWindow(this.selectDiscWindow, active, param);
							break;
					}
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_MOVE:	//移动阶段
					//移动阶段的状态变更时,先清除星图上的移动轨迹
					EclipseUtil.mainBoard.spaceMap.moveTrack.clear();
					switch(param.subPhase){
						case 1:	//显示选择单位的窗口
							this.commonInterruptWindow(this.chooseUnitWindow, active, param);
							break;
						case 2: //绘制移动轨迹
						case 3: //确认移动轨迹
							EclipseUtil.mainBoard.spaceMap.moveTrack.loadParam(param);
							break;
					}
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_RESEARCH:	//科研阶段
					//显示公共游戏面板
					if(EclipseUtil.gameManager.isLocalParam(param)){
						if(active){
							this.publicWindow.show(false);
						}else{
							//禁用所有输入
							EclipseUtil.mainBoard.disableAllInput();
						}
					}
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_UPGRADE:	//升级阶段
					//显示玩家面板
					if(EclipseUtil.gameManager.isLocalParam(param)){
						if(active){
							this.showPlayerWindow(param.position);
							//设置变更结果
							EclipseUtil.getLocalPlayer().loadBlueprintChangeParam(param);
						}else{
							//禁用所有输入
							EclipseUtil.mainBoard.disableAllInput();
						}
					}
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_SHIPPART:	//选择飞船部件
					if(EclipseUtil.gameManager.isLocalParam(param)){
						if(active){
							//显示玩家窗口+部件窗口
							this.showChoosePartWindow(active, param);
						}else{
							//禁用所有输入
							EclipseUtil.mainBoard.disableAllInput();
						}
					}
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_POPULATION:	//选择人口
					this.commonInterruptWindow(this.choosePopulationWindow, active, param);
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_TRADE:	//交易
					this.commonInterruptWindow(this.tradeWindow, active, param);
					break;
			}
		}
		
		/**
		 * 设置中断监听器触发时的界面(非弹出窗口)
		 */
		protected function setInterruptState(stateCode:int, active:Boolean, param:Object):void{
			if(EclipseUtil.gameManager.isLocalParam(param)){
				if(active){
					//如果存在行动参数,则设置玩家的输入状态
					this.uimanager.setInterruptStateWithParam(stateCode, param);
				}else{
					//禁用所有输入
					EclipseUtil.mainBoard.disableAllInput();
				}
			}
		}
		
		/**
		 * 通用中断窗口
		 */
		override protected function commonInterruptWindow(win:ConfirmWindow, active:Boolean, param:Object):void{
			//隐藏所有的游戏窗口
			this.hideGameBoard();
			super.commonInterruptWindow(win, active, param);
		}
		
		/**
		 * 显示选择部件的窗口
		 */
		protected function showChoosePartWindow(active:Boolean, param:Object):void{
			//隐藏所有的游戏窗口
			this.hideGameBoard();
			//显示玩家面板
			this.showPlayerWindow(param.position);
			//设置变更结果
			EclipseUtil.getLocalPlayer().loadBlueprintChangeParam(param);
			super.commonInterruptWindow(this.choosePartWindow, active, param);
		}
		
	}
}