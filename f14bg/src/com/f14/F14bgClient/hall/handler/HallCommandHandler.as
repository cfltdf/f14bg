package com.f14.F14bgClient.hall.handler
{
	import com.f14.F14bgClient.hall.HallUtil;
	import com.f14.f14bg.consts.CmdConst;
	import com.f14.f14bg.manager.TooltipManager;
	import com.f14.f14bg.net.CommandHandler;

	public class HallCommandHandler extends CommandHandler
	{
		public function HallCommandHandler()
		{
			super();
		}
		
		/**
		 * 处理系统指令
		 */
		override protected function processSystemCommand(param:Object):void{
			var i:int;
			switch(param.code){
				case CmdConst.SYSTEM_CODE_USER_INFO: //读取本地用户信息
					HallUtil.module.userPart.loadParam(param);
					break;
				case CmdConst.SYSTEM_CODE_ROOM_LIST: //刷新房间列表
					HallUtil.module.roomPart.loadRooms(param);
					break;
				case CmdConst.SYSTEM_CODE_PLAYER_LIST: //刷新大厅用户列表
					HallUtil.module.userList.loadUsers(param);
					break;
				case CmdConst.SYSTEM_CODE_ROOM_ADDED: //大厅添加房间
					HallUtil.module.roomPart.addRoom(param.room);
					break;
				case CmdConst.SYSTEM_CODE_ROOM_REMOVED: //大厅移除房间
					HallUtil.module.roomPart.removeRoom(param.roomId);
					break;
				case CmdConst.SYSTEM_CODE_ROOM_CHANGED: //大厅房间状态变化
					HallUtil.module.roomPart.updateRoom(param.room);
					break;
				case CmdConst.SYSTEM_CODE_JOIN_HALL: //用户进入大厅
					HallUtil.module.userList.addUser(param.user);
					break;
				case CmdConst.SYSTEM_CODE_LEAVE_HALL: //用户离开大厅
					HallUtil.module.userList.removeUser(param.user);
					break;
				case CmdConst.SYSTEM_CODE_HALL_REFRESH_USER: //大厅用户状态变化
					HallUtil.module.userList.updateUser(param.user);
					break;
				case CmdConst.SYSTEM_CODE_RANKING_LIST: //刷新排行榜
					TooltipManager.hideLoadingTip();
					HallUtil.module.rankingList.loadRankingListParam(param);
					break;
				/* case CmdConst.SYSTEM_CODE_USER_RANK: //读取用户积分信息
					TooltipManager.hideLoadingTip();
					HallUtil.stateManager.showUserWindow(param);
					break; */
				default: //其他指令不予执行
					break;
			}
		}
		
		/**
		 * 处理游戏指令
		 */
		override protected function processGameCommand(param:Object):void{
			
		}
		
		/**
		 * 处理聊天指令
		 */
		override protected function processChatCommand(param:Object):void{
			var message:Object = param.message;
			if(message!=null){
				HallUtil.module.chatPart.receiveMessage(message);
			}
		}
		
	}
}