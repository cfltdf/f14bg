package com.f14.f14bg.manager
{
	import com.adobe.serialization.json.JSON;
	import com.f14.core.util.ApplicationUtil;
	import com.f14.f14bg.consts.CmdConst;
	import com.f14.f14bg.manager.TooltipManager;
	
	public class ActionManager
	{
		
		public function ActionManager(){
			this.init();
		}
		
		/**
		 * 初始化
		 */
		public function init():void{
			
		}
		
		/**
		 * 发送指令
		 */
		public function sendCommand(param:Object):void{
			ApplicationUtil.sendCommand(JSON.encode(param));
		}
		
		/**
		 * 创建游戏指令
		 */
		public function createSystemCommand(code:int):Object{
			var param:Object = {};
			param.type = CmdConst.SYSTEM_CMD;
			param.code = code;
			return param;
		}
		
		/**
		 * 创建游戏指令
		 */
		public function createGameCommand(code:int):Object{
			var param:Object = {};
			param.type = CmdConst.GAME_CMD;
			param.code = code;
			return param;
		}
		
		/**
		 * 创建聊天指令
		 */
		public function createChatCommand():Object{
			var param:Object = {};
			param.type = CmdConst.CHAT_CMD;
			param.code = -1;
			return param;
		}
		
	}
}