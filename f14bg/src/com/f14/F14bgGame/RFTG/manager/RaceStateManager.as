package com.f14.F14bgGame.RFTG.manager
{
	import com.f14.F14bgGame.RFTG.RaceUtil;
	import com.f14.F14bgGame.RFTG.consts.CmdConst;
	import com.f14.F14bgGame.RFTG.player.RacePlayer;
	import com.f14.F14bgGame.RFTG.ui.CardBoard;
	import com.f14.F14bgGame.RFTG.ui.ChooseActionBoard;
	import com.f14.F14bgGame.RFTG.ui.window.GambleWindow;
	import com.f14.F14bgGame.RFTG.ui.window.StartWorldWindow;
	import com.f14.F14bgGame.bg.manager.GameStateManager;
	
	import mx.containers.Canvas;
	import mx.managers.PopUpManager;
	
	public class RaceStateManager extends GameStateManager
	{
		public function RaceStateManager()
		{
		}
		
		public static var COLORS:Array = [0x0000ff, 0x00ff00, 0xff0000, 0xffff00, 0x0033ff];
		public var chooseActionBoard:ChooseActionBoard;
		public var startWorldWindow:StartWorldWindow;
		public var gambleWindow:GambleWindow;
		
		/**
		 * 初始化游戏场景
		 */
		override public function init():void{
			super.init();
			chooseActionBoard = new ChooseActionBoard();
			PopUpManager.addPopUp(chooseActionBoard, RaceUtil.module);
			chooseActionBoard.visible = false;
		}
		
		/**
		 * 设置行动阶段是否生效
		 */
		override public function onPhaseChange(stateCode:int, active:Boolean, param:Object):void{
			if(stateCode==CmdConst.GAME_CODE_CHOOSE_ACTION && active){
				//如果是选择行动阶段,表示是整个回合的开始,这时清空上回合选择的行动
				RaceUtil.gameManager.clearRoundActions();
			}
			RaceUtil.mainBoard.gameInfoBoard.setActionActive(stateCode, active);
		}
		
		/**
		 * 设置游戏状态
		 */
		override public function onStateChange(stateCode:int, active:Boolean, param:Object):void{
			this.stateCode = stateCode;
			switch(stateCode){
				case CmdConst.GAME_CODE_STARTING_WORLD: //选择起始牌组
					startingWorld(!active, param);
					break;
				case CmdConst.GAME_CODE_STARTING_DISCARD: //开始时弃牌
					startingDiscard(!active, param);
					break;
				case CmdConst.GAME_CODE_CHOOSE_ACTION: //选择行动
					chooseAction(!active, param);
					break;
				case CmdConst.GAME_CODE_EXPLORE: //探索阶段
					explore(!active, param);
					break;
				case CmdConst.GAME_CODE_DEVELOP: //开发阶段
					develop(!active, param);
					break;
				case CmdConst.GAME_CODE_SETTLE: //扩张阶段
					settle(!active, param);
					break;
				case CmdConst.GAME_CODE_CONSUME: //消费阶段
					consume(!active, param);
					break;
				case CmdConst.GAME_CODE_PRODUCE: //生产阶段
					produce(!active, param);
					break;
				case CmdConst.GAME_CODE_ROUND_DISCARD: //回合结束阶段
					roundDiscard(!active, param);
					break;
			}
			var player:RacePlayer = RaceUtil.getPlayer(param.position);
			if(player!=null){
				player.playerState = null;
			}
			if(isLocal(param) && RaceUtil.gameManager.isPlayingGame()){
				//设置界面显示的阶段
				showUI(stateCode, active);
			}
		}
		
		public function getHandBoard():CardBoard{
			return RaceUtil.getLocalPlayer().controlBoard.cardBoard;
		}
		
		public function getPlayBoard():CardBoard{
			return RaceUtil.getLocalPlayer().racePlayerBoard.cardBoard;
		}
		
		/**
		 * 判断是否本地用户的参数
		 */
		private function isLocal(param:Object):Boolean{
			if(param==null){
				return false;
			}
			return RaceUtil.gameManager.isLocalPosition(param.position);
		}
		
		/**
		 * 选择起始牌组
		 */
		private function startingWorld(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					RaceUtil.stateManager.hideStartWorldWindow();
				}else{
					RaceUtil.stateManager.showStartWorldWindow(param);
				}
			}
		}
		
		/**
		 * 开始时弃牌
		 */
		private function startingDiscard(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					RaceUtil.getLocalPlayer().resetBoards();
				}else{
					RaceUtil.getLocalPlayer().resetBoards();
					getHandBoard().multiSelect = true;
					getHandBoard().setAllSelectable(true);
				}
			}
		}
		
		/**
		 * 回合结束时弃牌
		 */
		private function roundDiscard(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				if(ending){
					RaceUtil.getLocalPlayer().resetBoards();
				}else{
					RaceUtil.getLocalPlayer().resetBoards();
					getHandBoard().multiSelect = true;
					getHandBoard().setAllSelectable(true);
				}
			}
		}
		
		/**
		 * 选择行动
		 */
		private function chooseAction(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				RaceUtil.getLocalPlayer().resetBoards();
				if(!ending){
					//允许行动选择
					RaceUtil.getLocalPlayer().racePlayerBoard.canChooseAction = true;
					//清除选择行动的图像
					RaceUtil.getLocalPlayer().racePlayerBoard.clearActionImages();
				}else{
					//完成行动选择
					RaceUtil.getLocalPlayer().racePlayerBoard.setActionComplete();
				}
			}else{
				//其他玩家结束选择时
				if(ending){
					//完成行动选择
					RaceUtil.getPlayer(param.position).racePlayerBoard.setActionComplete();
				}else{
					//清除上一次的选择
					RaceUtil.getPlayer(param.position).racePlayerBoard.clearActionImages();
				}
			}
		}
		
		/**
		 * 探索阶段
		 */
		private function explore(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				//本地玩家行动
				if(ending){
					//阶段结束
					RaceUtil.getLocalPlayer().resetBoards();
				}else{
					//阶段开始
					RaceUtil.getLocalPlayer().resetBoards();
					getHandBoard().multiSelect = true;
				}
			}
		}
		
		/**
		 * 开发阶段
		 */
		private function develop(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				//本地玩家行动
				if(ending){
					//阶段结束
					RaceUtil.getLocalPlayer().resetBoards();
				}else{
					//阶段开始
					RaceUtil.getLocalPlayer().resetBoards();
					getHandBoard().multiSelect = true;
					getHandBoard().setAllSelectable(true);
				}
			}
		}
		
		/**
		 * 扩张阶段
		 */
		private function settle(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				//本地玩家行动
				if(ending){
					//阶段结束
					RaceUtil.getLocalPlayer().resetBoards();
				}else{
					//阶段开始
					RaceUtil.getLocalPlayer().resetBoards();
					getHandBoard().multiSelect = true;
					getHandBoard().setAllSelectable(true);
				}
			}
		}
		
		/**
		 * 消费阶段
		 */
		private function consume(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				//本地玩家行动
				if(ending){
					//阶段结束
					RaceUtil.getLocalPlayer().resetBoards();
				}else{
					//阶段开始
					RaceUtil.getLocalPlayer().resetBoards();
					getHandBoard().multiSelect = true;
					getHandBoard().setAllSelectable(true);
					getPlayBoard().multiSelect = true;
					getPlayBoard().setAllGoodSelectable(true);
				}
			}
		}
		
		/**
		 * 生产阶段
		 */
		private function produce(ending:Boolean, param:Object):void{
			if(isLocal(param)){
				//本地玩家行动
				if(ending){
					//阶段结束
					RaceUtil.getLocalPlayer().resetBoards();
				}else{
					//阶段开始
					RaceUtil.getLocalPlayer().resetBoards();
					getHandBoard().multiSelect = true;
					getHandBoard().setAllSelectable(true);
					getPlayBoard().multiSelect = true;
					getPlayBoard().setAllSelectable(true);
				}
			}
		}
		
		/**
		 * 按照阶段显示UI情况
		 */
		public function showUI(action:int, active:Boolean):void{
			//如果生效则显示按钮,否则就隐藏按钮
			if(active){
				RaceUtil.mainBoard.buttonBoard.changeButtons(action);
			}else{
				RaceUtil.mainBoard.buttonBoard.hideAllButtons();
			}
		}
		
		/**
		 * 隐藏所有游戏中用到的面板
		 */
		override public function hideGameBoard():void{
			hideStartWorldWindow();
			hideGambleWindow();
		}
		
		/**
		 * 显示起始牌组选择窗口
		 */
		public function showStartWorldWindow(param:Object):void{
			if(startWorldWindow==null){
				startWorldWindow = new StartWorldWindow();
				new Canvas().addChild(startWorldWindow);
			}
			startWorldWindow.loadParam(param);
			startWorldWindow.selectable = true;
			startWorldWindow.show(false);
		}
		
		/**
		 * 关闭起始牌组选择窗口
		 */
		public function hideStartWorldWindow():void{
			if(startWorldWindow!=null){
				startWorldWindow.cancel();
			}
		}
		
		/**
		 * 显示赌博窗口
		 */
		public function showGambleWindow(param:Object):void{
			if(gambleWindow==null){
				gambleWindow = new GambleWindow();
				new Canvas().addChild(gambleWindow);
			}
			gambleWindow.loadParam(param);
			gambleWindow.show(false);
		}
		
		/**
		 * 关闭赌博窗口
		 */
		public function hideGambleWindow():void{
			if(gambleWindow!=null){
				gambleWindow.cancel();
			}
		}
	}
}