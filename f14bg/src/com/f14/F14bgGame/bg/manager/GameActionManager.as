package com.f14.F14bgGame.bg.manager
{
	import com.f14.F14bgClient.room.RoomUtil;
	import com.f14.F14bgGame.bg.handler.InGameHandler;
	import com.f14.f14bg.consts.CmdConst;
	import com.f14.f14bg.manager.ActionManager;
	
	/**
	 * 行动指令管理器
	 */
	public class GameActionManager extends ActionManager
	{
		public function GameActionManager()
		{
			super();
		}
		
		public function get handler():InGameHandler{
			return RoomUtil.gameModule.gameCommandHandler;
		}
		
		/**
		 * 装载游戏资源信息
		 */
		public function loadResources():void{
			var code:int = CmdConst.SYSTEM_CODE_INIT_RESOURCE;
			var param:Object = createSystemCommand(code);
			//param.gameType = ApplicationManager.gameType;
			sendCommand(param);
		}
		
		/**
		 * 发送当前阶段的指令
		 */
		public function sendCurrentCommand(subact:String, param:Object = null):void{
			var code:int = handler.currentCode;
			var p:Object = createGameCommand(code);
			p.subact = subact;
			if(param!=null){
				for(var key:String in param){
					p[key] = param[key];
				}
			}
			this.sendCommand(p);
		}
		
		/**
		 * 通用确认信息
		 */
		public function commonConfirm(stepCode:String, confirm:Boolean):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.confirm = confirm;
			param.stepCode = stepCode;
			sendCommand(param);
		}
		
		/**
		 * 执行跳过动作
		 */
		public function doPass(code:int=0):void{
			//如果发送代码为0,则取当前收到的代码
			if(code==0){
				code = handler.currentCode;
			}
			var param:Object = createGameCommand(code);
			param.subact = "pass";
			sendCommand(param);
		}
		
		/**
		 * 执行取消动作
		 */
		public function doCancel(code:int=0):void{
			//如果发送代码为0,则取当前收到的代码
			if(code==0){
				code = handler.currentCode;
			}
			var param:Object = createGameCommand(code);
			param.subact = "cancel";
			sendCommand(param);
		}
	}
}