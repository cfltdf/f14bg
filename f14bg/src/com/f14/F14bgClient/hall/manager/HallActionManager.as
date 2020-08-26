package com.f14.F14bgClient.hall.manager
{
	import com.f14.F14bgClient.FlashHandler.HallHandler;
	import com.f14.F14bgClient.hall.HallUtil;
	import com.f14.core.util.ApplicationUtil;
	import com.f14.f14bg.consts.CmdConst;
	import com.f14.f14bg.manager.ActionManager;
	import com.f14.f14bg.manager.TooltipManager;
	
	import mx.collections.ArrayCollection;
	
	public class HallActionManager extends ActionManager
	{
		public function HallActionManager(){
			super();
		}
		
		/**
		 * 取得大厅指令接收器
		 */
		public function get hallHandler():HallHandler{
			return HallUtil.module.hallHandler;
		}
		
		/**
		 * 退出大厅
		 */
		public function exit():void{
			this.hallHandler.exit();
		}
		
		/**
		 * 刷新房间列表
		 */
		public function refreshRoomList():void{
			//TooltipManager.showLoadingTip("刷新列表...");
			var code:int = CmdConst.SYSTEM_CODE_ROOM_LIST;
			var param:Object = createSystemCommand(code);
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
		 * 读取本地用户信息
		 */
		public function loadLocalUser():void{
			var code:int = CmdConst.SYSTEM_CODE_USER_INFO;
			var param:Object = createSystemCommand(code);
			sendCommand(param);
		}
		
		/**
		 * 刷新用户列表
		 */
		public function refreshUserList():void{
			//TooltipManager.showLoadingTip("刷新用户列表...");
			var code:int = CmdConst.SYSTEM_CODE_PLAYER_LIST;
			var param:Object = createSystemCommand(code);
			sendCommand(param);
		}
		
		/**
		 * 创建房间
		 */
		public function createRoom(gameType:String, name:String, password:String, descr:String):void{
			TooltipManager.showLoadingTip("创建房间中...", 0);
			this.hallHandler.createRoom(gameType, name, password, descr);
			/*var code:int = CmdConst.SYSTEM_CODE_CREATE_ROOM;
			var param:Object = createSystemCommand(code);
			param.name = name;
			param.gameType = gameType;
			param.password = password;
			param.descr = descr;
			sendCommand(param);*/
			
		}
		
		/**
		 * 加入房间的检查
		 */
		public function joinRoomCheck(roomId:String, password:String):void{
			TooltipManager.showLoadingTip("加入房间中...", 0);
			var code:int = CmdConst.SYSTEM_CODE_JOIN_CHECK;
			var param:Object = createSystemCommand(code);
			param.id = roomId;
			param.password = password;
			sendCommand(param);
		}
		
		/**
		 * 装载所有游戏类型
		 */
		public function loadGameTypes():void{
			var codes:ArrayCollection = ApplicationUtil.commonHandler.getCodes("BOARDGAME");
			HallUtil.stateManager.createRoomWindow.gameTypes = codes;
			HallUtil.module.rankingList.gameTypes = codes;
		}
		
		/**
		 * 检查用户是否需要断线重连
		 */
		public function reconnectCheck():void{
			var code:int = CmdConst.SYSTEM_CODE_RECONNECT;
			var param:Object = createSystemCommand(code);
			sendCommand(param);
		}
		
		/**
		 * 刷新排行榜
		 */
		public function refreshRankingList(boardGameId:String):void{
			TooltipManager.showLoadingTip("读取排行榜信息...");
			var code:int = CmdConst.SYSTEM_CODE_RANKING_LIST;
			var param:Object = createSystemCommand(code);
			param.boardGameId = boardGameId;
			sendCommand(param);
		}
		
		/**
		 * 刷新用户积分信息
		 */
		public function refreshUserRanking(userId:String):void{
			/*TooltipManager.showLoadingTip("读取用户信息...");
			var code:int = CmdConst.SYSTEM_CODE_USER_RANK;
			var param:Object = createSystemCommand(code);
			param.userId = userId;
			sendCommand(param);*/
			ApplicationUtil.commonHandler.viewUser(userId);
		}
		
		/**
		 * 读取大厅公告信息
		 */
		public function loadHallNotice():void{
			var code:int = CmdConst.SYSTEM_CODE_HALL_NOTICE;
			var param:Object = createSystemCommand(code);
			sendCommand(param);
		}
		
	}
}