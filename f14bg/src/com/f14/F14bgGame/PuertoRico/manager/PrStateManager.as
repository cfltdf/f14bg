package com.f14.F14bgGame.PuertoRico.manager
{
	import com.f14.F14bgGame.PuertoRico.PRUtil;
	import com.f14.F14bgGame.PuertoRico.consts.GameCmdConst;
	import com.f14.F14bgGame.PuertoRico.ui.BuildingBoard;
	import com.f14.F14bgGame.PuertoRico.ui.ChooseCharacterBoard;
	import com.f14.F14bgGame.PuertoRico.ui.window.CaptainEndWindow;
	import com.f14.F14bgGame.PuertoRico.ui.window.CaptainWindow;
	import com.f14.F14bgGame.PuertoRico.ui.window.ChooseBuildingWindow;
	import com.f14.F14bgGame.PuertoRico.ui.window.CommonConfirmWindow;
	import com.f14.F14bgGame.PuertoRico.ui.window.CraftsmanWindow;
	import com.f14.F14bgGame.PuertoRico.ui.window.SettleWindow;
	import com.f14.F14bgGame.PuertoRico.ui.window.TraderWindow;
	import com.f14.F14bgGame.bg.manager.GameStateManager;
	import com.f14.core.util.ApplicationUtil;
	
	import mx.containers.Canvas;
	
	public class PrStateManager extends GameStateManager
	{
		public function PrStateManager()
		{
			super();
		}
		
		public var COLORS:Array = [0x0000ff, 0x00ff00, 0xff0000, 0xffff00, 0x0033ff];
		public var chooseCharacterBoard:ChooseCharacterBoard;
		public var buildingBoard:BuildingBoard;
		public var captainWindow:CaptainWindow;
		public var captainEndWindow:CaptainEndWindow;
		public var traderWindow:TraderWindow;
		public var craftsmanWindow:CraftsmanWindow;
		public var settleWindow:SettleWindow;
		public var commonConfirmWindows:Array = new Array();
		
		public var chooseBuildingWindow:ChooseBuildingWindow;
		
		/**
		 * 初始化游戏场景
		 */
		override public function init():void{
			super.init();
			var can:Canvas = new Canvas();
			chooseCharacterBoard = new ChooseCharacterBoard();
			can.addChild(chooseCharacterBoard);
			
			buildingBoard = new BuildingBoard();
			can.addChild(buildingBoard);
		}
		
		/**
		 * 设置行动阶段是否生效
		 */
		override public function onPhaseChange(stateCode:int, active:Boolean, param:Object):void{
			//非观战的玩家都需要隐藏所有按钮
			switch(stateCode){
				case GameCmdConst.GAME_CODE_CHOOSE_BUILDING_PHASE:
					//选择建筑阶段
					if(active){
						//显示建筑列表信息
						this.showChooseBuildingWindow(param);
					}else{
						//关闭选择建筑列表面板
						this.hideChooseBuildingWindow();
					}
					break;
			}
		}
		
		/**
		 * 设置游戏状态
		 */
		override public function onStateChange(stateCode:int, active:Boolean, param:Object):void{
			this.stateCode = stateCode;
			switch(stateCode){
				case GameCmdConst.GAME_CODE_CHOOSE_CHARACTER: //选择角色
					chooseCharacter(!active, param);
					break;
				case GameCmdConst.GAME_CODE_SETTLE: //拓荒者
					settle(!active, param);
					break;
				case GameCmdConst.GAME_CODE_BUILDER: //建筑师
					builder(!active, param);
					break;
				case GameCmdConst.GAME_CODE_MAJOR: //市长
					major(!active, param);
					break;
				case GameCmdConst.GAME_CODE_CRAFTSMAN: //工匠
					craftsman(!active, param);
					break;
				case GameCmdConst.GAME_CODE_TRADER: //商人
					trader(!active, param);
					break;
				case GameCmdConst.GAME_CODE_CAPTAIN: //船长
					captain(!active, param);
					break;
				case GameCmdConst.GAME_CODE_CAPTAIN_END: //船长阶段结束
					captainEnd(!active, param);
					break;
				case GameCmdConst.GAME_CODE_CHOOSE_BUILDING_PHASE: //选择建筑阶段
					chooseBuilding(!active, param);
					break;
			}
			if(isLocal(param) && PRUtil.gameManager.isPlayingGame()){
				//设置界面显示的阶段,旁观者不用设置
				showUI(stateCode, active);
			}
		}
		
		/**
		 * 判断是否本地用户的参数
		 */
		private function isLocal(param:Object):Boolean{
			if(param==null){
				return false;
			}
			return PRUtil.gameManager.isLocalPosition(param.position);
		}
		
		/**
		 * 选择角色阶段
		 */
		protected function chooseCharacter(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					//禁止所有的角色选择
					chooseCharacterBoard.setAllSelectable(false);
					hideCharacterBoard();
				}else{
					//开始角色选择
					chooseCharacterBoard.startSelect();
					//showCharacterBoard();
					PRUtil.soundManager.play("playerTurn");
				}
			}
		}
		
		/**
		 * 拓荒者阶段
		 */
		protected function settle(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					//禁止所有种植园选择
					//PRUtil.application.plantationBoard.setAllSelectable(false);
					hideSettleBoard();
				}else{
					//开始种植园选择
					//PRUtil.application.plantationBoard.setAllSelectable(true);
					showSettleBoard();
					PRUtil.soundManager.play("playerTurn");
				}
			}
		}
		
		/**
		 * 建筑师阶段
		 */
		protected function builder(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					PRUtil.stateManager.buildingBoard.selectable = false
					PRUtil.stateManager.buildingBoard.hideBlackTradeBoard();
					//关闭建筑面板
					PRUtil.stateManager.hideBuildingBoard();
				}else{
					PRUtil.stateManager.buildingBoard.selectable = true;
					PRUtil.stateManager.showBuidlingBoard();
					PRUtil.stateManager.buildingBoard.showBlackTradeBoard(param);
					PRUtil.soundManager.play("playerTurn");
				}
			}
		}
		
		/**
		 * 市长阶段
		 */
		protected function major(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					//本地玩家的移民设置为不可选
					PRUtil.getLocalPlayer().prPlayerBoard.playerBuildingBoard.setColonistSelectable(false);
				}else{
					//本地玩家的移民设置为可选
					PRUtil.getLocalPlayer().prPlayerBoard.playerBuildingBoard.setColonistSelectable(true);
					PRUtil.soundManager.play("playerTurn");
				}
			}
		}
		
		/**
		 * 工匠阶段
		 */
		protected function craftsman(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					//本地玩家的资源设置为不可选
					//PRUtil.getLocalPlayer().playerBoard.playerInfoBoard.resourceSelecter.setResourceSelectable(false);
					hideCraftsmanBoard();
				}else{
					//本地玩家的资源设置为可选
					//PRUtil.getLocalPlayer().playerBoard.playerInfoBoard.resourceSelecter.setResourceSelectable(true);
					showCraftsmanBoard(param);
					PRUtil.soundManager.play("playerTurn");
				}
			}
		}
		
		/**
		 * 商人阶段
		 */
		protected function trader(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					//本地玩家的资源设置为不可选
					//PRUtil.getLocalPlayer().playerBoard.playerInfoBoard.resourceSelecter.setResourceSelectable(false);
					hideTraderBoard();
				}else{
					//本地玩家的资源设置为可选
					//PRUtil.getLocalPlayer().playerBoard.playerInfoBoard.resourceSelecter.setResourceSelectable(true);
					showTraderBoard(param);
					PRUtil.soundManager.play("playerTurn");
				}
			}
		}
		
		/**
		 * 船长阶段
		 */
		protected function captain(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					//货船和货物设置为不可选
					//PRUtil.application.shipBoard.shipOption.setAllComponentsSelectable(false);
					//PRUtil.getLocalPlayer().playerBoard.playerInfoBoard.resourceSelecter.setResourceSelectable(false);
					hideCaptainBoard();
				}else{
					//货船和货物设置为可选
					//PRUtil.application.shipBoard.shipOption.setAllComponentsSelectable(true);
					//PRUtil.getLocalPlayer().playerBoard.playerInfoBoard.resourceSelecter.setResourceSelectable(true);
					showCaptainBoard(param);
					PRUtil.soundManager.play("playerTurn");
				}
			}
		}
		
		/**
		 * 船长阶段结束时保留货物
		 */
		protected function captainEnd(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					PRUtil.stateManager.hideCaptainEndBoard();
				}else{
					PRUtil.stateManager.showCaptainEndBoard();
					PRUtil.soundManager.play("playerTurn");
				}
			}
		}
		
		/**
		 * 选择建筑阶段
		 */
		protected function chooseBuilding(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					chooseBuildingWindow.selectable = false;
				}else{
					chooseBuildingWindow.selectable = true;
					PRUtil.soundManager.play("playerTurn");
				}
			}
		}
		
		/**
		 * 按照阶段显示UI情况
		 */
		public function showUI(action:int, active:Boolean):void{
			//如果生效则显示按钮,否则就隐藏按钮
			if(active){
				PRUtil.mainBoard.buttonBoard.changeButtons(action);
			}else{
				PRUtil.mainBoard.buttonBoard.hideAllButtons();
			}
		}
		
		/**
		 * 显示选择角色的面板
		 */
		public function showCharacterBoard():void{
			chooseCharacterBoard.show(false);
		}
		
		/**
		 * 隐藏选择角色的面板
		 */
		public function hideCharacterBoard():void{
			chooseCharacterBoard.cancel();
		}
		
		/**
		 * 显示或隐藏角色面板
		 */
		public function trigCharacterBoard():void{
			chooseCharacterBoard.trig(false);
		}
		
		/**
		 * 显示建筑面板
		 */
		public function showBuidlingBoard():void{
			buildingBoard.show(false);
		}
		
		/**
		 * 关闭建筑面板
		 */
		public function hideBuildingBoard():void{
			buildingBoard.cancel();
		}
		
		/**
		 * 显示或隐藏建筑面板
		 */
		public function trigBuildingBoard():void{
			buildingBoard.trig(false);
		}
		
		/**
		 * 显示船长阶段保留货物的面板
		 */
		public function showCaptainEndBoard():void{
			if(captainEndWindow==null){
				captainEndWindow = new CaptainEndWindow();
				new Canvas().addChild(captainEndWindow);
				captainEndWindow.initInputs();
			}
			captainEndWindow.reset();
			captainEndWindow.resource_selecter.player = PRUtil.getLocalPlayer();
			captainEndWindow.show(false);
		}
		
		/**
		 * 关闭船长阶段保留货物的面板
		 */
		public function hideCaptainEndBoard():void{
			if(captainEndWindow!=null){
				captainEndWindow.cancel();
			}
		}
		
		/**
		 * 显示船长阶段的面板
		 */
		public function showCaptainBoard(param:Object):void{
			if(captainWindow==null){
				captainWindow = new CaptainWindow();
				new Canvas().addChild(captainWindow);
				captainWindow.initInputs();
			}
			captainWindow.reset();
			//captainWindow.resource_selecter.player = PRUtil.getLocalPlayer();
			captainWindow.show(false);
			captainWindow.loadShipInfo();
			captainWindow.initSpecialShips(param);
		}
		
		/**
		 * 关闭船长阶段的面板
		 */
		public function hideCaptainBoard():void{
			if(captainWindow!=null){
				captainWindow.cancel();
			}
		}
		
		/**
		 * 触发通用确认窗口
		 */
		public function trigCommonConfirm(param:Object):void{
			var win:CommonConfirmWindow = commonConfirmWindows[param.stepCode];
			if(!param.ending){
				//显示窗口
				if(win==null){
					win = new CommonConfirmWindow();
					win.show(false);
					win.message = param.message;
					win.stepCode = param.stepCode;
					commonConfirmWindows[param.stepCode] = win;
				}
			}else{
				//隐藏窗口
				if(win!=null){
					win.cancel();
					commonConfirmWindows[param.stepCode] = null;
				}
			}
		}
		
		/**
		 * 显示商人阶段的面板
		 */
		public function showTraderBoard(param:Object):void{
			if(traderWindow==null){
				traderWindow = new TraderWindow();
				new Canvas().addChild(traderWindow);
				traderWindow.initInputs();
			}
			traderWindow.reset();
			traderWindow.resource_selecter.player = PRUtil.getLocalPlayer();
			traderWindow.canSelfTrade = param.canSelfTrade;
			traderWindow.show(false);
			traderWindow.loadTradeHouseInfo();
		}
		
		/**
		 * 关闭商人阶段的面板
		 */
		public function hideTraderBoard():void{
			if(traderWindow!=null){
				traderWindow.cancel();
			}
		}
		
		/**
		 * 显示工匠阶段的面板
		 */
		public function showCraftsmanBoard(param:Object):void{
			if(craftsmanWindow==null){
				craftsmanWindow = new CraftsmanWindow();
				new Canvas().addChild(craftsmanWindow);
				craftsmanWindow.initInputs();
			}
			craftsmanWindow.reset();
			craftsmanWindow.resource_choose.player = PRUtil.getLocalPlayer();
			craftsmanWindow.showMessage = param.showMessage;
			craftsmanWindow.show(false);
		}
		
		/**
		 * 关闭工匠阶段的面板
		 */
		public function hideCraftsmanBoard():void{
			if(craftsmanWindow!=null){
				craftsmanWindow.cancel();
			}
		}
		
		/**
		 * 显示拓荒者阶段的面板
		 */
		public function showSettleBoard():void{
			if(settleWindow==null){
				settleWindow = new SettleWindow();
				new Canvas().addChild(settleWindow);
				settleWindow.initInputs();
			}
			settleWindow.reset();
			settleWindow.show(false);
			settleWindow.loadPlantationInfo();
		}
		
		/**
		 * 关闭工匠阶段的面板
		 */
		public function hideSettleBoard():void{
			if(settleWindow!=null){
				settleWindow.cancel();
			}
		}
		
		/**
		 * 隐藏所有游戏中用到的面板
		 */
		override public function hideGameBoard():void{
			hideBuildingBoard();
			hideCaptainBoard();
			hideCaptainEndBoard();
			hideCharacterBoard();
			hideCraftsmanBoard();
			hideSettleBoard();
			hideTraderBoard();
			hideCommonConfirmWindow();
			hideChooseBuildingWindow();
		}
		
		/**
		 * 隐藏所有通用确认窗口
		 */
		public function hideCommonConfirmWindow():void{
			for(var stepCode:String in commonConfirmWindows){
				var win:CommonConfirmWindow = commonConfirmWindows[stepCode];
				if(win!=null){
					win.cancel();
					commonConfirmWindows[stepCode] = null;
				}
			}
		}
		
		/**
		 * 显示选择建筑窗口
		 */
		public function showChooseBuildingWindow(param:Object):void{
			if(chooseBuildingWindow==null){
				chooseBuildingWindow = new ChooseBuildingWindow();
				new Canvas().addChild(chooseBuildingWindow);
			}
			chooseBuildingWindow.loadBuildings(param);
			chooseBuildingWindow.show(false);
		}
		
		/**
		 * 关闭选择建筑窗口
		 */
		public function hideChooseBuildingWindow():void{
			if(chooseBuildingWindow!=null){
				chooseBuildingWindow.cancel();
			}
		}
	}
}