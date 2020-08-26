package com.f14.F14bgGame.TS.manager
{
	import com.f14.F14bgGame.TS.TSUtil;
	import com.f14.F14bgGame.TS.consts.TSGameCmd;
	import com.f14.F14bgGame.TS.ui.window.AddInfluenceWindow;
	import com.f14.F14bgGame.TS.ui.window.ChoiceWindow;
	import com.f14.F14bgGame.TS.ui.window.ChooseCardWindow;
	import com.f14.F14bgGame.TS.ui.window.CountryWindow;
	import com.f14.F14bgGame.TS.ui.window.CoupWindow;
	import com.f14.F14bgGame.TS.ui.window.HeadLineWindow;
	import com.f14.F14bgGame.TS.ui.window.InfluenceWindow;
	import com.f14.F14bgGame.TS.ui.window.OpActionWindow;
	import com.f14.F14bgGame.TS.ui.window.RealignmentWindow;
	import com.f14.F14bgGame.TS.ui.window.TSCustom100Window;
	import com.f14.F14bgGame.TS.ui.window.TSCustom104Window;
	import com.f14.F14bgGame.TS.ui.window.TSCustom108Window;
	import com.f14.F14bgGame.TS.ui.window.TSCustom40Window;
	import com.f14.F14bgGame.TS.ui.window.TSCustom45Window;
	import com.f14.F14bgGame.TS.ui.window.TSCustom46Window;
	import com.f14.F14bgGame.TS.ui.window.TSCustom49RoundWindow;
	import com.f14.F14bgGame.TS.ui.window.TSCustom49Window;
	import com.f14.F14bgGame.TS.ui.window.TSCustom50Window;
	import com.f14.F14bgGame.TS.ui.window.TSCustom67Window;
	import com.f14.F14bgGame.TS.ui.window.TSCustom77Window;
	import com.f14.F14bgGame.TS.ui.window.TSCustom94Window;
	import com.f14.F14bgGame.TS.ui.window.TSCustom98Window;
	import com.f14.F14bgGame.TS.ui.window.TSDeckWindow;
	import com.f14.F14bgGame.TS.ui.window.TSDiscardDeckWindow;
	import com.f14.F14bgGame.TS.ui.window.TSQuagmireWindow;
	import com.f14.F14bgGame.TS.ui.window.TSRoundDiscardWindow;
	import com.f14.F14bgGame.TS.ui.window.TSScoreCardWindow;
	import com.f14.F14bgGame.TS.ui.window.ViewHandWindow;
	import com.f14.F14bgGame.bg.consts.InputState;
	import com.f14.F14bgGame.bg.manager.GameStateManager;
	import com.f14.F14bgGame.bg.ui.window.ConfirmWindow;
	
	import flash.geom.Point;

	public class TSStateManager extends GameStateManager
	{
		public function TSStateManager()
		{
			super();
		}
		
		public var influenceWindow:InfluenceWindow;
		public var addInfluenceWindow:AddInfluenceWindow;
		public var coupWindow:CoupWindow;
		public var realignmentWindow:RealignmentWindow;
		public var countryWindow:CountryWindow;
		public var opActionWindow:OpActionWindow;
		public var chooseCardWindow:ChooseCardWindow;
		public var choiceWindow:ChoiceWindow;
		public var viewHandWindow:ViewHandWindow;
		public var deckWindow:TSDeckWindow;
		public var discardDeckWindow:TSDiscardDeckWindow;
		public var custom45Window:TSCustom45Window;
		public var custom46Window:TSCustom46Window;
		public var custom77Window:TSCustom77Window;
		public var custom67Window:TSCustom67Window;
		public var custom50Window:TSCustom50Window;
		public var custom94Window:TSCustom94Window;
		public var custom98Window:TSCustom98Window;
		public var custom40Window:TSCustom40Window;
		public var quagmireWindow:TSQuagmireWindow;
		public var custom49Window:TSCustom49Window;
		public var custom49RoundWindow:TSCustom49RoundWindow;
		public var roundDiscardWindow:TSRoundDiscardWindow;
		public var custom108Window:TSCustom108Window;
		public var scoreCardWindow:TSScoreCardWindow;
		public var custom104Window:TSCustom104Window;
		public var custom100Window:TSCustom100Window;
		
		public var healLineWindow:HeadLineWindow;
		protected var commonWindowPosition:Point;
		
		/**
		 * 初始化
		 */
		override public function init():void{
			super.init();
			
			influenceWindow = new InfluenceWindow();
			addInfluenceWindow = new AddInfluenceWindow();
			coupWindow = new CoupWindow();
			realignmentWindow = new RealignmentWindow();
			countryWindow = new CountryWindow();
			opActionWindow = new OpActionWindow();
			healLineWindow = new HeadLineWindow();
			chooseCardWindow = new ChooseCardWindow();
			choiceWindow = new ChoiceWindow();
			viewHandWindow = new ViewHandWindow();
			deckWindow = new TSDeckWindow();
			discardDeckWindow = new TSDiscardDeckWindow();
			custom45Window = new TSCustom45Window();
			custom46Window = new TSCustom46Window();
			custom77Window = new TSCustom77Window();
			custom67Window = new TSCustom67Window();
			custom50Window = new TSCustom50Window();
			custom94Window = new TSCustom94Window();
			custom98Window = new TSCustom98Window();
			custom40Window = new TSCustom40Window();
			quagmireWindow = new TSQuagmireWindow();
			custom49Window = new TSCustom49Window();
			custom49RoundWindow = new TSCustom49RoundWindow();
			roundDiscardWindow = new TSRoundDiscardWindow();
			custom108Window = new TSCustom108Window();
			scoreCardWindow = new TSScoreCardWindow();
			custom104Window = new TSCustom104Window();
			custom100Window = new TSCustom100Window();
			
			confirmWindows.push(influenceWindow);
			confirmWindows.push(addInfluenceWindow);
			confirmWindows.push(coupWindow);
			confirmWindows.push(realignmentWindow);
			confirmWindows.push(countryWindow);
			confirmWindows.push(opActionWindow);
			confirmWindows.push(healLineWindow);
			confirmWindows.push(chooseCardWindow);
			confirmWindows.push(choiceWindow);
			confirmWindows.push(viewHandWindow);
			confirmWindows.push(deckWindow);
			confirmWindows.push(discardDeckWindow);
			confirmWindows.push(custom45Window);
			confirmWindows.push(custom46Window);
			confirmWindows.push(custom77Window);
			confirmWindows.push(custom67Window);
			confirmWindows.push(custom50Window);
			confirmWindows.push(custom94Window);
			confirmWindows.push(custom98Window);
			confirmWindows.push(custom40Window);
			confirmWindows.push(quagmireWindow);
			confirmWindows.push(custom49Window);
			confirmWindows.push(custom49RoundWindow);
			confirmWindows.push(roundDiscardWindow);
			confirmWindows.push(custom108Window);
			confirmWindows.push(scoreCardWindow);
			confirmWindows.push(custom104Window);
			confirmWindows.push(custom100Window);
			
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
		 * 设置当前的阶段变化时触发的方法
		 */
		override public function onPhaseChange(stateCode:int, active:Boolean, param:Object):void{
			super.onPhaseChange(stateCode, active, param);
			switch(stateCode){
				case TSGameCmd.GAME_CODE_HEAD_LINE: //头条
					if(active){
						this.healLineWindow.show(false);
						this.healLineWindow.clear();
						//靠屏幕中上显示
						this.healLineWindow.y = 0;
					}else{
						this.healLineWindow.cancel();
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
				TSUtil.module.disableAllInput();
			}else{
				if(TSUtil.gameManager.isLocalPosition(param.trigPlayerPosition)){
					//显示默认的按键面板
					TSUtil.getLocalPlayer().playerBoard.inputState = InputState.DEFAULT;
				}else{
					//禁用所有的输入
					TSUtil.mainBoard.disableAllInput();
				}
			}
		}
		
		/**
		 * 中断型监听器中玩家状态开始/结束监听时触发的方法
		 */
		override public function onInterruptState(stateCode:int, active:Boolean, param:Object):void{
			switch(stateCode){
				case TSGameCmd.GAME_CODE_ADJUST_INFLUENCE: //调整影响力
					this.commonInterruptWindow(this.influenceWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_ADD_INFLUENCE: //使用OP放置影响力
					this.commonInterruptWindow(this.addInfluenceWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_COUP: //政变
					this.commonInterruptWindow(this.coupWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_REALIGNMENT: //调整阵营
					this.commonInterruptWindow(this.realignmentWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_COUNTRY_ACTION: //选择国家进行行动
					this.commonInterruptWindow(this.countryWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_OP_ACTION: //使用OP行动
					this.commonInterruptWindow(this.opActionWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_CARD_ACTION: //选择手牌执行行动
					this.commonInterruptWindow(this.chooseCardWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				 case TSGameCmd.GAME_CODE_CHOICE: //选择行动
				 	this.commonInterruptWindow(this.choiceWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_VIEW_HAND: //查看手牌
				 	this.commonInterruptWindow(this.viewHandWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_VIEW_DISCARD_DECK: //查看弃牌堆
					this.commonInterruptWindowCenter(this.discardDeckWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_45: //#45-高峰会议 事件
					this.commonInterruptWindow(this.custom45Window, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_46: //#46-我如何学会不再担忧
					this.commonInterruptWindow(this.custom46Window, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_77: //#77-“不要问你的祖国能为你做什么……”
					this.commonInterruptWindow(this.custom77Window, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_67: //#67-向苏联出售谷物
					this.commonInterruptWindow(this.custom67Window, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_50: //#50-“我们会埋葬你的”
					this.commonInterruptWindowCenter(this.custom50Window, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_94: //#94-切尔诺贝利
					this.commonInterruptWindow(this.custom94Window, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_98: //#98-阿尔德里希·阿姆斯
					this.commonInterruptWindowCenter(this.custom98Window, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_40: //#40-古巴导弹危机
					this.commonInterruptWindow(this.custom40Window, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_QUAGMIRE: //困境事件
					this.commonInterruptWindow(this.quagmireWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_49: //#49-导弹嫉妒
					this.commonInterruptWindow(this.custom49Window, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_49_ROUND: //#49-导弹嫉妒 执行
					this.commonInterruptWindow(this.custom49RoundWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_ROUND_DISCARD: //回合结束时弃牌
					this.commonInterruptWindow(this.roundDiscardWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_108: //#108-我们在伊朗有人
					this.commonInterruptWindowCenter(this.custom108Window, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_PLAY_SCORE_CARD: //打计分牌
					this.commonInterruptWindow(this.scoreCardWindow, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_104: //#104-剑桥五杰
					this.commonInterruptWindowCenter(this.custom104Window, active, param);
					this.setInterruptUI(active, param);
					break;
				case TSGameCmd.GAME_CODE_100: //#100-战争游戏
					this.commonInterruptWindowCenter(this.custom100Window, active, param);
					this.setInterruptUI(active, param);
					break;
				
			}
		}
		
		/**
		 * 设置中断监听器触发时的界面
		 */
		protected function setInterruptUI(active:Boolean, param:Object):void{
			if(TSUtil.gameManager.isLocalParam(param)){
				if(active){
					//如果存在行动参数,则设置玩家的输入状态
					if(param.actionString){
						TSUtil.getLocalPlayer().playerBoard.inputState = param.actionString;
					}
				}else{
					//禁用所有输入
					TSUtil.mainBoard.disableAllInput();
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
			//如果是这些窗口,将不会调整显示位置
			switch(win){
				case this.viewHandWindow: //查看手牌的窗口
					return;
			}
			//以下代码会调整窗口的显示位置
			if(TSUtil.gameManager.isLocalParam(param)){
				//只有本地玩家才处理该事件
				if(active){
					//设置窗口的位置
					if(this.commonWindowPosition==null){
						//如果没有位置,则靠左上角显示
						this.commonWindowPosition = new Point();
						//this.commonWindowPosition.x = ApplicationUtil.application.width - win.width;
						//this.commonWindowPosition.y = ApplicationUtil.application.height - win.height;
					}
					win.x = this.commonWindowPosition.x;
					win.y = this.commonWindowPosition.y;
				}else{
					//关闭窗口时将记录窗口的位置
					if(this.commonWindowPosition!=null){
						this.commonWindowPosition.x = win.x;
						this.commonWindowPosition.y = win.y;
					}
				}
			}
		}
		
		/**
		 * 通用中断窗口(居中显示)
		 */
		protected function commonInterruptWindowCenter(win:ConfirmWindow, active:Boolean, param:Object):void{
			//隐藏所有的游戏窗口
			this.hideGameBoard();
			super.commonInterruptWindow(win, active, param);
		}
		
	}
}