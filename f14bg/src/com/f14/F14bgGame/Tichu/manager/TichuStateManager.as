package com.f14.F14bgGame.Tichu.manager
{
	import com.f14.F14bgGame.Tichu.TichuUtil;
	import com.f14.F14bgGame.Tichu.consts.TichuGameCmd;
	import com.f14.F14bgGame.Tichu.ui.window.ConfirmExchangeWindow;
	import com.f14.F14bgGame.Tichu.ui.window.ExchangeWindow;
	import com.f14.F14bgGame.Tichu.ui.window.GiveScoreWindow;
	import com.f14.F14bgGame.Tichu.ui.window.RoundResultWindow;
	import com.f14.F14bgGame.Tichu.ui.window.SelectNumberWindow;
	import com.f14.F14bgGame.Tichu.ui.window.WishWindow;
	import com.f14.F14bgGame.bg.consts.InputState;
	import com.f14.F14bgGame.bg.manager.GameStateManager;
	import com.f14.F14bgGame.bg.ui.window.ConfirmWindow;

	public class TichuStateManager extends GameStateManager
	{
		public function TichuStateManager()
		{
			super();
		}
		
		public var exchangeWindow:ExchangeWindow;
		public var giveScoreWindow:GiveScoreWindow;
		public var selectNumberWindow:SelectNumberWindow;
		public var wishWindow:WishWindow;
		public var roundResultWindow:RoundResultWindow;
		public var confirmExchangeWindow:ConfirmExchangeWindow;
		
		/**
		 * 初始化
		 */
		override public function init():void{
			super.init();
			
			exchangeWindow = new ExchangeWindow();
			giveScoreWindow = new GiveScoreWindow();
			selectNumberWindow = new SelectNumberWindow();
			wishWindow = new WishWindow();
			roundResultWindow = new RoundResultWindow();
			confirmExchangeWindow = new ConfirmExchangeWindow();
			
			confirmWindows.push(exchangeWindow);
			confirmWindows.push(giveScoreWindow);
			confirmWindows.push(selectNumberWindow);
			confirmWindows.push(wishWindow);
			confirmWindows.push(roundResultWindow);
			confirmWindows.push(confirmExchangeWindow);
			
			for each(var win:ConfirmWindow in confirmWindows){
				this.cav.addChild(win);
				win.initComponents();
			}
		}
		
		/**
		 * 设置玩家当前的界面状态变化时触发的方法
		 */
		override public function onStateChange(stateCode:int, active:Boolean, param:Object):void{
			super.onStateChange(stateCode, active, param);
			if(TichuUtil.gameManager.isLocalParam(param)){
				switch(stateCode){
					case TichuGameCmd.GAME_CODE_REGROUP_PHASE: //换牌阶段
						if(active){
							this.showExchangeWindow();
						}else{
							this.hideExchangeWindow();
						}
						break;
					case TichuGameCmd.GAME_CODE_ROUND_RESULT: //回合结算
						//开始/结束时设置窗口的输入状态
						this.roundResultWindow.inputable = active;
						break;
					case TichuGameCmd.GAME_CODE_CONFIRM_EXCHANGE: //确认换牌阶段
						if(active){
							this.showConfirmExchangeWindow();
						}else{
							this.hideConfirmExchangeWindow();
						}
						break;
					case TichuGameCmd.GAME_CODE_ROUND_PHASE: //回合行动
						if(!active){
							//回合结束时,检查将按钮状态改成炸弹
							TichuUtil.getLocalPlayer().playerBoard.inputState = "BOMB";
						}
						break;
				}
			}
		}
		
		/**
		 * 设置当前的阶段变化时触发的方法
		 */
		override public function onPhaseChange(stateCode:int, active:Boolean, param:Object):void{
			super.onPhaseChange(stateCode, active, param);
			switch(stateCode){
				case TichuGameCmd.GAME_CODE_ROUND_RESULT: //回合结算
					if(active){
						this.showRoundResultWindow();
					}else{
						this.hideRoundResultWindow();
					}
					break;
				case TichuGameCmd.GAME_CODE_ROUND_PHASE: //回合阶段
					if(active && TichuUtil.gameManager.isPlayingGame()){
						//所有玩家在回合阶段中将显示,炸弹按钮
						TichuUtil.getLocalPlayer().playerBoard.inputState = "BOMB";
					}
					break;
			}
		}
		
		/**
		 * 中断型监听器阶段开始/结束监听时触发的方法
		 */
		override public function onInterruptPhase(stateCode:int, active:Boolean, param:Object):void{
			//通用的界面处理
			if(active){
				//中断监听器开始时将禁用所有的输入状态
				TichuUtil.module.disableAllInput();
			}else{
				if(TichuUtil.gameManager.isLocalPosition(param.trigPlayerPosition)){
					//显示默认的按键面板
					TichuUtil.getLocalPlayer().playerBoard.inputState = InputState.DEFAULT;
				}else{
					//禁用所有的输入
					TichuUtil.mainBoard.disableAllInput();
				}
			}
		}
		
		/**
		 * 中断型监听器中玩家状态开始/结束监听时触发的方法
		 */
		override public function onInterruptState(stateCode:int, active:Boolean, param:Object):void{
			switch(stateCode){
				case TichuGameCmd.GAME_CODE_GIVE_SCORE: //给对方分数
					this.commonInterruptWindow(this.giveScoreWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TichuGameCmd.GAME_CODE_WISH_POINT: //许愿
					this.commonInterruptWindow(this.wishWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TichuGameCmd.GAME_CODE_BOMB_PHASE: //炸弹阶段
					this.setInterruptUI(active, param);
					break;
			}
		}
		
		/**
		 * 设置中断监听器触发时的界面
		 */
		protected function setInterruptUI(active:Boolean, param:Object):void{
			if(TichuUtil.gameManager.isLocalParam(param)){
				if(active){
					//如果存在行动参数,则设置玩家的输入状态
					if(param.actionString){
						TichuUtil.getLocalPlayer().playerBoard.inputState = param.actionString;
					}
				}else{
					//禁用所有输入
					TichuUtil.mainBoard.disableAllInput();
				}
			}
		}
		
		
		/**
		 * 显示换牌窗口
		 */
		public function showExchangeWindow():void{
			exchangeWindow.loadPlayerInfo();
			exchangeWindow.show(false);
		}
		
		/**
		 * 隐藏换牌窗口
		 */
		public function hideExchangeWindow():void{
			exchangeWindow.cancel();
		}
		
		/**
		 * 显示选择数字的窗口
		 */
		public function showSelectNumberWindow(cardIds:String):void{
			selectNumberWindow.cardIds = cardIds;
			selectNumberWindow.show();
		}
		
		/**
		 * 隐藏换牌窗口
		 */
		public function hideSelectNumberWindow():void{
			selectNumberWindow.cancel();
		}
		
		/**
		 * 显示回合确认的窗口
		 */
		public function showRoundResultWindow():void{
			this.roundResultWindow.clear();
			this.roundResultWindow.show(false);
		}
		
		/**
		 * 隐藏回合确认窗口
		 */
		public function hideRoundResultWindow():void{
			this.roundResultWindow.cancel();
		}
		
		/**
		 * 显示确认换牌的窗口
		 */
		public function showConfirmExchangeWindow():void{
			this.confirmExchangeWindow.clear();
			this.confirmExchangeWindow.show(false);
		}
		
		/**
		 * 隐藏确认换牌窗口
		 */
		public function hideConfirmExchangeWindow():void{
			this.confirmExchangeWindow.cancel();
		}
		
	}
}