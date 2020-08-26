package com.f14.f14bg.net
{
	import com.f14.core.util.ApplicationUtil;
	import com.f14.f14bg.consts.CmdConst;
	import com.f14.f14bg.manager.TooltipManager;

	public class CommandHandler
	{
		
		/**
		 * 处理指令
		 */
		public function processCommand(param:Object):void{
			switch(param.type){
				case CmdConst.SYSTEM_CMD:
					//系统指令
					this.processSystemCommand(param);
					break;
				case CmdConst.CHAT_CMD:
					//聊天指令
					this.processChatCommand(param);
					break;
				case CmdConst.GAME_CMD:
					//游戏指令
					this.processGameCommand(param);
					break;
			}
		}
		
		/**
		 * 处理系统指令
		 */
		protected function processSystemCommand(param:Object):void{
			
		}
		
		/**
		 * 处理游戏指令
		 */
		protected function processGameCommand(param:Object):void{
			
		}
		
		/**
		 * 处理聊天指令
		 */
		protected function processChatCommand(param:Object):void{

		}
		
	}
}