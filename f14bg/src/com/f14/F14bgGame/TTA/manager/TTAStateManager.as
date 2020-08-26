package com.f14.F14bgGame.TTA.manager
{
	import com.f14.F14bgGame.TTA.TTAUtil;
	import com.f14.F14bgGame.TTA.consts.TTAGameCmd;
	import com.f14.F14bgGame.TTA.player.TTAPlayer;
	import com.f14.F14bgGame.TTA.ui.window.BonusCardWindow;
	import com.f14.F14bgGame.TTA.ui.window.ChoosePlayerWindow;
	import com.f14.F14bgGame.TTA.ui.window.DiscardWindow;
	import com.f14.F14bgGame.TTA.ui.window.PactWindow;
	import com.f14.F14bgGame.TTA.ui.window.ResourceWindow;
	import com.f14.F14bgGame.TTA.ui.window.TTAAlertWindow;
	import com.f14.F14bgGame.TTA.ui.window.TTAConfirmWindow;
	import com.f14.F14bgGame.TTA.ui.window.TTAPlayerWindow;
	import com.f14.F14bgGame.TTA.ui.window.TerritoryWindow;
	import com.f14.F14bgGame.bg.consts.InputState;
	import com.f14.F14bgGame.bg.manager.GameStateManager;
	import com.f14.F14bgGame.bg.ui.window.AlertWindow;
	import com.f14.F14bgGame.bg.ui.window.ConfirmWindow;
	
	import mx.containers.Canvas;

	/**
	 * TTA的界面管理器
	 */
	public class TTAStateManager extends GameStateManager
	{
		public function TTAStateManager()
		{
			super();
		}
		
		protected var _playerWindows:Array = new Array();
		
		public var bonusCardWindow:BonusCardWindow;
		public var discardWindow:DiscardWindow;
		public var resourceWindow:ResourceWindow;
		public var confirmWindow:TTAConfirmWindow;
		public var territoryWindow:TerritoryWindow;
		public var choosePlayerWindow:ChoosePlayerWindow;
		public var pactWindow:PactWindow;
		
		/**
		 * 初始化
		 */
		override public function init():void{
			super.init();
			
			bonusCardWindow = new BonusCardWindow();
			discardWindow = new DiscardWindow();
			resourceWindow = new ResourceWindow();
			confirmWindow = new TTAConfirmWindow();
			territoryWindow = new TerritoryWindow();
			choosePlayerWindow = new ChoosePlayerWindow();
			pactWindow = new PactWindow();
			
			confirmWindows.push(bonusCardWindow);
			confirmWindows.push(discardWindow);
			confirmWindows.push(resourceWindow);
			confirmWindows.push(confirmWindow);
			confirmWindows.push(territoryWindow);
			confirmWindows.push(choosePlayerWindow);
			confirmWindows.push(pactWindow);
			
			for each(var win:ConfirmWindow in confirmWindows){
				this.cav.addChild(win);
			}
		}
		
		/**
		 * 清除所有窗口内容
		 */
		override public function clear():void{
			super.clear();
			this.clearPlayerWindows();
		}
		
		/**
		 * 创建TTA的提示窗口
		 */
		override protected function createAlertWindow():AlertWindow{
			return new TTAAlertWindow();
		}
		
		/**
		 * 隐藏所有游戏中用到的面板
		 */
		override public function hideGameBoard():void{
			super.hideGameBoard();
			
			//隐藏所有玩家的游戏面板窗口
			for each(var win:TTAPlayerWindow in this._playerWindows){
				if(win!=null){
					win.cancel();
				}
			}
		}
		
		/**
		 * 设置玩家当前的界面状态变化时触发的方法
		 */
		override public function onStateChange(stateCode:int, active:Boolean, param:Object):void{
			if(active && TTAUtil.gameManager.isLocalParam(param)){
				//如果是本地玩家开始回合,则将界面显示为本地玩家
				TTAUtil.mainBoard.setShowState("PLAYER");
				//显示阶段对应的按键面板
				TTAUtil.getLocalPlayer().step = param.currentStep;
				//玩家回合开始时,发出提示音
				TTAUtil.soundManager.play("playerTurn");
			}else{
				//否则显示详细概况的界面
				TTAUtil.mainBoard.setShowState("SUMMARY");
			}
			super.onStateChange(stateCode, active, param);
		}
		
		/**
		 * 中断型监听器阶段开始/结束监听时触发的方法
		 */
		override public function onInterruptPhase(stateCode:int, active:Boolean, param:Object):void{
			//通用的界面处理
			if(active){
				//中断监听器开始时将禁用所有的输入状态
				TTAUtil.module.disableAllInput();
			}else{
				if(TTAUtil.gameManager.isLocalPosition(param.trigPlayerPosition)){
					//如果本地玩家是触发事件的玩家,则显示本地玩家界面
					TTAUtil.mainBoard.setShowState("PLAYER");
					//显示默认的按键面板
					TTAUtil.mainBoard.localPlayerBoard.inputState = InputState.DEFAULT;
				}else{
					//否则显示详细概况的界面
					TTAUtil.mainBoard.setShowState("SUMMARY");
					//禁用所有的输入
					TTAUtil.mainBoard.disableAllInput();
				}
			}
			//特殊的界面处理
			switch(stateCode){
				case TTAGameCmd.GAME_CODE_AUCTION_TERRITORY: //玩家拍卖殖民地
				case TTAGameCmd.GAME_CODE_WAR: //侵略/战争
					this.onTerritoryPhase(active, param);
					break;
				case TTAGameCmd.GAME_CODE_PACT: //条约
					this.onPactPhase(active, param);
					break;
			}
		}
		
		/**
		 * 中断型监听器中玩家状态开始/结束监听时触发的方法
		 */
		override public function onInterruptState(stateCode:int, active:Boolean, param:Object):void{
			switch(stateCode){
				case TTAGameCmd.GAME_CODE_DISCARD_MILITARY: //弃军事牌
					this.commonInterruptWindow(this.discardWindow, active, param);
					break;
				case TTAGameCmd.GAME_CODE_CHOOSE_RESOURCE: //选择资源
					this.commonInterruptWindow(this.resourceWindow, active, param);
					break;
				case TTAGameCmd.GAME_CODE_EVENT_BUILD: //选择是否建造
					this.commonInterruptWindow(this.confirmWindow, active, param);
					break;
				case TTAGameCmd.GAME_CODE_EVENT_LOSE_POP: //失去人口
				case TTAGameCmd.GAME_CODE_EVENT_DESTORY: //选择摧毁建筑/部队
				case TTAGameCmd.GAME_CODE_EVENT_COLONY: //选择殖民地
				case TTAGameCmd.GAME_CODE_EVENT_FLIP_WONDER: //选择奇迹
				case TTAGameCmd.GAME_CODE_EVENT_TAKE_CARD: //拿牌
					//TTAUtil.mainBoard.setShowState("PLAYER");
					this.commonInterruptWindow(this.confirmWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TTAGameCmd.GAME_CODE_AUCTION_TERRITORY: //拍卖殖民地
				case TTAGameCmd.GAME_CODE_WAR: //侵略/战争
					this.onTerritoryState(active, param);
					break;
				case TTAGameCmd.GAME_CODE_ACTIVABLE_CARD: //使用卡牌能力
					this.commonInterruptWindow(this.confirmWindow, active, param);
					this.setInterruptUI(active, param);
					//将窗口显示的位置偏上放置
					this.confirmWindow.y = 25;
					break;
				case TTAGameCmd.GAME_CODE_CHOOSE_PLAYER: //选择玩家
					this.commonInterruptWindow(this.choosePlayerWindow, active, param);
					break;
				case TTAGameCmd.GAME_CODE_EVENT_DESTORY_OTHERS: //拆除其他玩家的建筑
					//TTAUtil.mainBoard.setShowState("SUMMARY");
					this.commonInterruptWindow(this.confirmWindow, active, param);
					this.onDestroyOtherState(active, param);
					//将窗口显示的位置偏上放置
					this.confirmWindow.y = 25;
					break;
				case TTAGameCmd.GAME_CODE_PACT: //条约
					this.onPactState(active, param);
					break;
			}
		}
		
		/**
		 * 显示通用的请求界面
		 */
		public function showRequestWindow(param:Object):void{
			TTAUtil.mainBoard.requestWindow.loadParam(param);
			TTAUtil.mainBoard.requestWindow.visible = true;
			TTAUtil.getLocalPlayer().playerBoard.inputState = param.cmdString;
		}
		
		/**
		 * 隐藏通用的请求界面
		 */
		public function hideRequestWindow():void{
			TTAUtil.mainBoard.requestWindow.clear();
			TTAUtil.mainBoard.requestWindow.visible = false;
			TTAUtil.getLocalPlayer().playerBoard.inputState = InputState.DEFAULT;
		}
		
		/**
		 * 取得玩家窗口,如果不存在,则创建
		 */
		public function getPlayerWindow(position:int):TTAPlayerWindow{
			var win:TTAPlayerWindow = this._playerWindows[position];
			if(win==null){
				win = new TTAPlayerWindow();
				this._playerWindows[position] = win;
				new Canvas().addChild(win);
				win.playerBoard.init();
				//win.playerBoard.playerInfo = TTAUtil.mainBoard.getPlayerInfo(position);
			}
			return win;
		}
		
		/**
		 * 显示玩家面板的窗口
		 */
		public function showPlayerWindow(position:int):void{
			var win:TTAPlayerWindow = this.getPlayerWindow(position);
			win.show(false);
		}
		
		/**
		 * 显示/隐藏玩家面板的窗口
		 */
		public function trigPlayerWindow(position:int):void{
			var win:TTAPlayerWindow = this.getPlayerWindow(position);
			win.trig(false);
		}
		
		/**
		 * 隐藏玩家面板的窗口
		 */
		public function hidePlayerWindow(position:int):void{
			var win:TTAPlayerWindow = this.getPlayerWindow(position);
			win.cancel();
		}
		
		/**
		 * 清除所有玩家面板窗口的内容
		 */
		public function clearPlayerWindows():void{
			for each(var win:TTAPlayerWindow in this._playerWindows){
				if(win!=null){
					win.playerBoard.clear();
				}
			}
		}
		
		/**
		 * 设置中断监听器触发时的界面
		 */
		protected function setInterruptUI(active:Boolean, param:Object):void{
			if(TTAUtil.gameManager.isLocalParam(param)){
				if(active){
					TTAUtil.mainBoard.setShowState("PLAYER");
					//如果存在行动参数,则设置玩家的输入状态
					if(param.actionString){
						TTAUtil.getLocalPlayer().playerBoard.inputState = param.actionString;
					}
				}else{
					if(TTAUtil.gameManager.isLocalPosition(param.trigPlayerPosition)){
						//如果本地玩家是触发事件的玩家,则显示本地玩家界面
						TTAUtil.mainBoard.setShowState("PLAYER");
					}else{
						//否则显示详细概况的界面
						TTAUtil.mainBoard.setShowState("SUMMARY");
					}
					//禁用所有输入
					TTAUtil.mainBoard.disableAllInput();
				}
			}
		}
		
		/**
		 * 拍卖殖民地阶段的界面调整
		 */
		protected function onTerritoryPhase(active:Boolean, param:Object):void{
			if(active){
				//显示殖民地拍卖的窗口
				//this.territoryWindow.loadParam(param);
				//this.territoryWindow.show(false);
			}else{
				//关闭窗口
				this.territoryWindow.cancel();
			}
		}
		
		/**
		 * 拍卖殖民地阶段玩家回合设定
		 */
		protected function onTerritoryState(active:Boolean, param:Object):void{
			//只处理本地玩家的情况
			if(TTAUtil.gameManager.isLocalParam(param)){
				this.territoryWindow.inputable = active;
				if(active){
					//设置输入状态
					//this.territoryWindow.loadParam(param);
					//this.territoryWindow.show(false);
				}else{
					//关闭窗口
					//this.territoryWindow.cancel();
				}
			}
		}
		
		/**
		 * 拆除其他玩家建筑的阶段
		 */
		protected function onDestroyOtherState(active:Boolean, param:Object):void{
			//只处理本地玩家的情况
			if(TTAUtil.gameManager.isLocalParam(param)){
				if(active){
					//切换到详细概况的界面
					TTAUtil.mainBoard.setShowState("SUMMARY");
					//设定可以选择的玩家的操作情况
					var positions:Array = param.availablePositions.split(",");
					for each(var position:int in positions){
						var player:TTAPlayer = TTAUtil.getPlayer(position);
						player.playerTableBoard.selectable = true;
					}
				}else{
					if(TTAUtil.gameManager.isLocalPosition(param.trigPlayerPosition)){
						//如果本地玩家是触发事件的玩家,则显示本地玩家界面
						TTAUtil.mainBoard.setShowState("PLAYER");
					}else{
						//否则显示详细概况的界面
						TTAUtil.mainBoard.setShowState("SUMMARY");
					}
					//禁用所有输入
					TTAUtil.mainBoard.disableAllInput();
					this.disableAllPlayerTableInput();
				}
			}
		}
		
		/**
		 * 禁用所有玩家详细概述板块的输入
		 */
		public function disableAllPlayerTableInput():void{
			for each(var player:TTAPlayer in TTAUtil.gameManager.players){
				player.playerTableBoard.selectable = false;
			}
		}
		
		/**
		 * 签订条约阶段的界面调整
		 */
		protected function onPactPhase(active:Boolean, param:Object):void{
			if(active){
				//显示窗口,并禁用窗口的所有输入
				this.pactWindow.loadParam(param);
				this.pactWindow.disableAllInput();
				this.pactWindow.show(false);
				this.currentConfirmWindow = this.pactWindow;
			}else{
				//关闭窗口
				this.pactWindow.cancel();
				this.currentConfirmWindow = null;
			}
		}
		
		/**
		 * 签订条约阶段玩家回合设定
		 */
		protected function onPactState(active:Boolean, param:Object):void{
			//只处理本地玩家的情况
			if(TTAUtil.gameManager.isLocalParam(param)){
				if(active){
					//设置输入状态
					this.pactWindow.msg = param.msg;
					this.pactWindow.setInputState(param.step);
				}else{
					//行动结束时禁用所有输入
					this.pactWindow.disableAllInput();
				}
			}
		}
		
	}
}