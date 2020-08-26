package com.f14.F14bgGame.bg.handler
{
	import com.f14.F14bgGame.bg.consts.ListenerType;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.player.Player;
	import com.f14.f14bg.consts.CmdConst;
	import com.f14.f14bg.net.CommandHandler;
	
	import mx.collections.ArrayCollection;
	

	/**
	 * 游戏中的指令接收对象
	 */
	public class InGameHandler extends CommandHandler
	{
		public function InGameHandler()
		{
			super();
		}
		
		public var currentCode:int;
		protected var _commandList:ArrayCollection = new ArrayCollection();
		protected var _isProcessing:Boolean = false;
		
		/**
		 * 判断是否正在处理指令
		 */
		public function get isProcessing():Boolean{
			return this._isProcessing;
		}
		
		/**
		 * 设置是否正在处理指令
		 */
		public function set isProcessing(isProcessing:Boolean):void{
			this._isProcessing = isProcessing;
		}
		
		/**
		 * 添加指令到待处理列表
		 */
		protected function addCommand(param:Object):void{
			this._commandList.addItem(param);
		}
		
		/**
		 * 处理游戏指令
		 */
		override protected function processGameCommand(param:Object):void{
			this.addCommand(param);
			this.checkProcessGameCommand();
		}
		
		/**
		 * 检查是否可以执行指令
		 */
		public function checkProcessGameCommand():void{
			while(this._commandList.length>0 && !this.isProcessing){
				var param:Object = this._commandList.removeItemAt(0);
				this.isProcessing = true;
				this.executeGameCommand(param);
				//如果不在执行动画效果,则设置为指令执行完成
				if(!DefaultManagerUtil.animManager.isPlaying){
					this.isProcessing = false;
				}
			}
		}
		
		/**
		 * 执行游戏指令
		 */
		protected function executeGameCommand(param:Object):void{
			var i:int;
			var player:Player;
			var code:int = param.code;
			switch(code){
				case CmdConst.GAME_CODE_SETUP: //设置游戏起始信息
					DefaultManagerUtil.gameManager.setupGame(param);
					break;
				case CmdConst.GAME_CODE_PLAYER_STATE: //刷新玩家状态
					this.refreshPlayerStates(param);
					break;
				case CmdConst.GAME_CODE_PLAYING_STATE: //刷新玩家游戏进行状态
					player = DefaultManagerUtil.gameManager.getPlayerById(param.userId);
					player.playingState = param.playingState;
					break;
				case CmdConst.GAME_CODE_START_LISTEN: //玩家行动开始
					if(param.listenerType==ListenerType.INTERRUPT){
						//中断型监听器
						DefaultManagerUtil.stateManager.onInterruptState(param.validCode, true, param);
					}else{
						this.currentCode = param.validCode;
						DefaultManagerUtil.stateManager.onStateChange(this.currentCode, true, param);
					}
					break;
				case CmdConst.GAME_CODE_PLAYER_RESPONSED: //玩家行动结束
					if(param.listenerType==ListenerType.INTERRUPT){
						//中断型监听器
						DefaultManagerUtil.stateManager.onInterruptState(param.validCode, false, param);
					}else{
						DefaultManagerUtil.stateManager.onStateChange(this.currentCode, false, param);
					}
					break;
				case CmdConst.GAME_CODE_PHASE_START: //阶段开始指令
					if(param.listenerType==ListenerType.INTERRUPT){
						//中断型监听器
						DefaultManagerUtil.stateManager.onInterruptPhase(param.validCode, true, param);
					}else{
						this.currentCode = param.validCode;
						DefaultManagerUtil.stateManager.onPhaseChange(param.validCode, true, param);
					}
					break;
				case CmdConst.GAME_CODE_PHASE_END: //阶段结束指令
					if(param.listenerType==ListenerType.INTERRUPT){
						//中断型监听器
						DefaultManagerUtil.stateManager.onInterruptPhase(param.validCode, false, param);
					}else{
						DefaultManagerUtil.stateManager.onPhaseChange(param.validCode, false, param);
					}
					break;
				case CmdConst.GAME_CODE_REFRESH_MSG: //刷新中断窗口当前信息
					if(DefaultManagerUtil.stateManager.currentConfirmWindow!=null){
						DefaultManagerUtil.stateManager.currentConfirmWindow.msg = param.msg;
					}
					break;
				case CmdConst.GAME_CODE_PLAYING_INFO: //刷新当前游戏状态
					DefaultManagerUtil.gameManager.loadPlayingInfo(param);
					break;
				case CmdConst.GAME_CODE_TIP_ALERT: //显示提示信息
					DefaultManagerUtil.stateManager.alert(param.msg, param.param);
					break;
				case CmdConst.GAME_CODE_SIMPLE_CMD: //简单指令
					this.processSimpleCmd(param);
					break;
				case CmdConst.GAME_CODE_ANIM_CMD:	//动画效果的指令
					this.processAnimCommand(param);
					break;
				default:
					this.processGameCmd(param);
					break;
			}
		}
		
		/**
		 * 处理动画效果的指令
		 */
		protected function processAnimCommand(param:Object):void{
			DefaultManagerUtil.animManager.processAnimCommand(param.animParam);
		}
		
		/**
		 * 处理游戏指令
		 */
		protected function processGameCmd(param:Object):void{
			
		}
		
		/**
		 * 刷新玩家的输入状态
		 */
		protected function refreshPlayerStates(param:Object):void{
			if(param.states){
				for each(var object:Object in param.states){
					var player:Player = DefaultManagerUtil.gameManager.getPlayerById(object.userId);
					player.playerState = object.playerState;
				}
			}
			
		}
		
		/**
		 * 处理简单指令的方法
		 */
		protected function processSimpleCmd(param:Object):void{
			
		}
		
	}
}