package com.f14.F14bgGame.Innovation.manager
{
	import com.f14.F14bgGame.Innovation.InnoUtil;
	import com.f14.F14bgGame.Innovation.consts.InnoGameCmd;
	import com.f14.F14bgGame.Innovation.ui.window.InnoCheckScoreWindow;
	import com.f14.F14bgGame.Innovation.ui.window.InnoChooseHandWindow;
	import com.f14.F14bgGame.Innovation.ui.window.InnoChoosePlayerWindow;
	import com.f14.F14bgGame.Innovation.ui.window.InnoChooseScoreWindow;
	import com.f14.F14bgGame.Innovation.ui.window.InnoChooseSpecificCardWindow;
	import com.f14.F14bgGame.Innovation.ui.window.InnoConfirmWindow;
	import com.f14.F14bgGame.Innovation.ui.window.InnoCustom026Window;
	import com.f14.F14bgGame.Innovation.ui.window.InnoCustom069Window;
	import com.f14.F14bgGame.Innovation.ui.window.InnoCustom074Window;
	import com.f14.F14bgGame.Innovation.ui.window.InnoCustom076Window;
	import com.f14.F14bgGame.Innovation.ui.window.InnoCustom081Window;
	import com.f14.F14bgGame.Innovation.ui.window.InnoCustom083Window;
	import com.f14.F14bgGame.bg.consts.InputState;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.manager.GameStateManager;
	import com.f14.F14bgGame.bg.ui.window.ConfirmWindow;
	
	import flash.geom.Point;

	public class InnoStateManager extends GameStateManager
	{
		public function InnoStateManager()
		{
			super();
		}
		
		protected var commonWindowPosition:Point;
		
		public var innoConfirmWindow:InnoConfirmWindow;
		public var innoChooseHandWindow:InnoChooseHandWindow;
		public var chooseSpecificCardWindow:InnoChooseSpecificCardWindow;
		public var chooseScoreWindow:InnoChooseScoreWindow;
		public var choosePlayerWindow:InnoChoosePlayerWindow;
		public var custom026Window:InnoCustom026Window;
		public var custom069Window:InnoCustom069Window;
		public var custom074Window:InnoCustom074Window;
		public var custom083Window:InnoCustom083Window;
		public var custom076Window:InnoCustom076Window;
		public var custom081Window:InnoCustom081Window;
		public var checkScoreWindow:InnoCheckScoreWindow;
		
		
		/**
		 * 初始化
		 */
		override public function init():void{
			super.init();
			
			this.innoConfirmWindow = new InnoConfirmWindow();
			this.innoChooseHandWindow = new InnoChooseHandWindow();
			this.chooseSpecificCardWindow = new InnoChooseSpecificCardWindow();
			this.chooseScoreWindow = new InnoChooseScoreWindow();
			this.choosePlayerWindow = new InnoChoosePlayerWindow();
			this.custom026Window = new InnoCustom026Window();
			this.custom069Window = new InnoCustom069Window();
			this.custom074Window = new InnoCustom074Window();
			this.custom083Window = new InnoCustom083Window();
			this.custom076Window = new InnoCustom076Window();
			this.custom081Window = new InnoCustom081Window();
			this.checkScoreWindow = new InnoCheckScoreWindow();
			
			confirmWindows.push(this.innoConfirmWindow);
			confirmWindows.push(this.innoChooseHandWindow);
			confirmWindows.push(this.chooseSpecificCardWindow);
			confirmWindows.push(this.chooseScoreWindow);
			confirmWindows.push(this.choosePlayerWindow);
			confirmWindows.push(this.custom026Window);
			confirmWindows.push(this.custom069Window);
			confirmWindows.push(this.custom074Window);
			confirmWindows.push(this.custom083Window);
			confirmWindows.push(this.custom076Window);
			confirmWindows.push(this.custom081Window);
			confirmWindows.push(this.checkScoreWindow);
			
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
			commonWindowPosition = null;
		}
		
		/**
		 * 设置玩家当前的界面状态变化时触发的方法
		 */
		override public function onStateChange(stateCode:int, active:Boolean, param:Object):void{
			super.onStateChange(stateCode, active, param);
			if(DefaultManagerUtil.gameManager.isLocalParam(param)){
				if(active){
					//InnoUtil.soundManager.play("playerTurn");
				}else{
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
				InnoUtil.module.disableAllInput();
			}else{
				if(InnoUtil.gameManager.isLocalPosition(param.trigPlayerPosition)){
					//显示默认的按键面板
					InnoUtil.getLocalPlayer().playerBoard.inputState = InputState.DEFAULT;
				}else{
					//禁用所有的输入
					InnoUtil.mainBoard.disableAllInput();
				}
			}
		}
		
		/**
		 * 中断型监听器中玩家状态开始/结束监听时触发的方法
		 */
		override public function onInterruptState(stateCode:int, active:Boolean, param:Object):void{
			switch(stateCode){
				case InnoGameCmd.GAME_CODE_DRAW_CARD_ACTION: //摸牌的行动阶段
					this.commonInterruptWindow(this.simpleConfirmWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_CHOOSE_CARD:		//选择手牌的行动
					this.commonInterruptWindow(this.innoConfirmWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_CHOOSE_STACK:	//选择牌堆的行动阶段
				case InnoGameCmd.GAME_CODE_COMMON_CONFIRM:	//通用询问窗口
					this.showInterruptWindow(this.innoConfirmWindow, active, param, "TOP");
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_CHOOSE_CARDS:	//选择手牌的行动(多选)
					this.commonInterruptWindow(this.innoChooseHandWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_SPLAY_CARD: //展开牌堆的行动阶段
					this.showInterruptWindow(this.innoConfirmWindow, active, param, "TOP");
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_CHOOSE_SPECIFIC_CARD:	//选择特定的卡牌
					this.commonInterruptWindow(this.chooseSpecificCardWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_CHOOSE_SCORE_CARD:	//选择计分牌
				case InnoGameCmd.GAME_CODE_CHOOSE_SCORE_CARDS:	//选择计分牌(多选)
					this.commonInterruptWindow(this.chooseScoreWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_CHOOSE_PLAYER:	//选择玩家
					this.showInterruptWindow(this.choosePlayerWindow, active, param, "TOP");
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_026:		//#026-监听器
					this.showInterruptWindow(this.custom026Window, active, param, "TOP");
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_069:		//#069-监听器
					this.showInterruptWindow(this.custom069Window, active, param, "TOP");
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_074:		//#074-监听器
					this.showInterruptWindow(this.custom074Window, active, param, "TOP");
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_083:		//#083-监听器
					this.showInterruptWindow(this.custom083Window, active, param, "");
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_076:		//#076-监听器
					this.showInterruptWindow(this.custom076Window, active, param, "TOP");
					this.setInterruptUI(active, param);
					break;
				case InnoGameCmd.GAME_CODE_081:		//#081-监听器
					this.showInterruptWindow(this.custom081Window, active, param, "");
					this.setInterruptUI(active, param);
					break;
			}
		}
		
		/**
		 * 设置中断监听器触发时的界面
		 */
		protected function setInterruptUI(active:Boolean, param:Object):void{
			if(InnoUtil.gameManager.isLocalParam(param)){
				if(active){
					//如果存在行动参数,则设置玩家的输入状态
					if(param.actionString){
						InnoUtil.getLocalPlayer().playerBoard.inputState = param.actionString;
					}
				}else{
					//禁用所有输入
					InnoUtil.mainBoard.disableAllInput();
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
		 * 通用中断窗口
		 */
		protected function showInterruptWindow(win:ConfirmWindow, active:Boolean, param:Object, position:String):void{
			super.commonInterruptWindow(win, active, param);
			if(DefaultManagerUtil.gameManager.isLocalParam(param)){
				//只有本地玩家才处理该事件
				if(active){
					//调整窗口位置
					switch(position){
						case "TOP":
							win.y = 30;
							break;
						default:
							break;
					}
				}else{
				}
			}
		}
		
		/**
		 * 触发计分区窗口
		 */
		public function trigCheckScoreWindow():void{
			this.checkScoreWindow.trig(false);
			this.checkScoreWindow.y = 30;
		}
		
	}
}