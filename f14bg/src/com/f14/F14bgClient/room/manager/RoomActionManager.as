package com.f14.F14bgClient.room.manager
{
	import com.f14.F14bgClient.FlashHandler.RoomHandler;
	import com.f14.core.util.ApplicationUtil;
	import com.f14.f14bg.consts.CmdConst;
	import com.f14.f14bg.manager.ActionManager;
	import com.f14.f14bg.manager.TooltipManager;
	
	public class RoomActionManager extends ActionManager
	{
		protected var handler:RoomHandler;
		
		public function RoomActionManager()
		{
			super();
			this.handler = new RoomHandler();
		}
		
		/**
		 * 准备游戏
		 */
		public function doReady():void{
			var param:Object = createSystemCommand(CmdConst.SYSTEM_CODE_USER_READY);
			sendCommand(param);
		}
		
		/**
		 * 开始游戏
		 */
		public function doStart():void{
			var param:Object = createSystemCommand(CmdConst.SYSTEM_CODE_USER_START);
			sendCommand(param);
		}
		
		/**
		 * 离开房间
		 * 
		 * @param force 是否强制退出
		 */
		public function leaveRoom(force:Boolean=false):void{
			var code:int;
			if(force){
				//强制退出的指令代码
				code = CmdConst.GAME_CODE_REMOVE_PLAYER;
			}else{
				code = CmdConst.GAME_CODE_LEAVE;
			}
			var param:Object = createGameCommand(code);
			sendCommand(param);
		}
		
		/**
		 * 设置游戏配置
		 */
		public function setConfig(config:Object):void{
			var code:int = CmdConst.GAME_CODE_SET_CONFIG;
			var param:Object = createGameCommand(code);
			param.config = config;
			sendCommand(param);
		}
		
		/**
		 * 发送消息
		 */
		public function sendMessage(msg:String):void{
			var param:Object = createChatCommand();
			param.msg = msg;
			sendCommand(param);
		}
		
		/**
		 * 刷新用户积分信息
		 */
		public function refreshUserRanking(userId:String):void{
			ApplicationUtil.commonHandler.viewUser(userId);
		}
		
		/**
		 * 装载房间信息
		 */
		public function loadRoomInfo():void{
			var code:int = CmdConst.SYSTEM_CODE_LOAD_ROOM_INFO;
			var param:Object = createSystemCommand(code);
			sendCommand(param);
		}
		
		/**
		 * 加入游戏
		 */
		public function doJoinPlay():void{
			var param:Object = createSystemCommand(CmdConst.SYSTEM_CODE_ROOM_JOIN_PLAY);
			sendCommand(param);
		}
		
		/**
		 * 离开游戏
		 */
		public function doLeavePlay():void{
			var param:Object = createSystemCommand(CmdConst.SYSTEM_CODE_ROOM_LEAVE_PLAY);
			sendCommand(param);
		}
		
		/**
		 * 离开房间
		 */
		public function doLeave():void{
			this.handler.leaveRequest(ApplicationUtil.roomId);
		}
		
		/**
		 * 发送邀请通知
		 */
		public function doInvitePlay():void{
			var param:Object = createSystemCommand(CmdConst.SYSTEM_CODE_ROOM_INVITE_NOTIFY);
			sendCommand(param);
		}
		
	}
}