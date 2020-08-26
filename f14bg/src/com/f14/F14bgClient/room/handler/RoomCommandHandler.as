package com.f14.F14bgClient.room.handler
{
	import com.f14.F14bgClient.room.RoomUtil;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.F14bgGame.bg.player.Player;
	import com.f14.f14bg.consts.CmdConst;
	import com.f14.f14bg.manager.TooltipManager;
	import com.f14.f14bg.net.CommandHandler;

	public class RoomCommandHandler extends CommandHandler
	{
		public function RoomCommandHandler()
		{
			super();
		}
		
		/**
		 * 处理系统指令
		 */
		override protected function processSystemCommand(param:Object):void{
			var i:int;
			switch(param.code){
				case CmdConst.SYSTEM_CODE_USER_ROOM_INFO: //用户及房间信息
					//调整设置窗口的按钮显示状态
					RoomUtil.stateManager.configWindow.setShowState(param.room.state, param.userState);
					RoomUtil.stateManager.showConfigWindow();
					break;
				case CmdConst.SYSTEM_CODE_USER_LIST_INFO: //房间用户列表
					RoomUtil.stateManager.configWindow.loadJoinUsers(param);
					RoomUtil.stateManager.chatWindow.loadUserList(param);
					break;
				case CmdConst.SYSTEM_CODE_ROOM_JOIN: //用户进入房间
					RoomUtil.stateManager.chatWindow.addUser(param.user);
					break;
				case CmdConst.SYSTEM_CODE_ROOM_LEAVE: //用户离开房间
					RoomUtil.stateManager.chatWindow.removeUser(param.user);
					break;
				case CmdConst.SYSTEM_CODE_ROOM_JOIN_PLAY: //用户加入游戏
					RoomUtil.stateManager.configWindow.addUser(param.user);
					RoomUtil.soundManager.play("joinGame");
					break;
				case CmdConst.SYSTEM_CODE_ROOM_LEAVE_PLAY: //用户离开游戏
					RoomUtil.stateManager.configWindow.removeUser(param.user);
					RoomUtil.soundManager.play("leaveGame");
					break;
				case CmdConst.SYSTEM_CODE_ROOM_REFRESH_USER: //刷新用户状态
					RoomUtil.stateManager.chatWindow.updateUser(param.user);
					break;
				case CmdConst.SYSTEM_CODE_ROOM_USER_BUTTON: //用户的按键状态发生变化
					RoomUtil.stateManager.configWindow.setShowState(param.roomState, param.userState);
					break;
				case CmdConst.SYSTEM_CODE_LOAD_CONFIG: //读取游戏设置
					RoomUtil.stateManager.configWindow.loadConfig(param);
					break;
				case CmdConst.SYSTEM_CODE_USER_READY: //刷新用户的准备状态
					RoomUtil.stateManager.configWindow.readyUser(param);
					break;
				/* case CmdConst.SYSTEM_CODE_USER_RANK: //读取用户积分信息
					TooltipManager.hideLoadingTip();
					RoomUtil.stateManager.showUserWindow(param);
					break; */
			}
		}
		
		/**
		 * 处理游戏指令
		 */
		override protected function processGameCommand(param:Object):void{
			var i:int;
			var player:Player;
			var code:int = param.code;
			switch(code){
				case CmdConst.GAME_CODE_LOCAL_PLAYER: //读取本地玩家信息
					DefaultManagerUtil.gameManager.createLocalPlayer(param.localPlayer);
					break;
				case CmdConst.GAME_CODE_LOAD_PLAYER: //读取玩家信息
					//创建玩家面板并添加玩家
					var players:Array = param.players;
					for(i=0;i<players.length;i++){
						DefaultManagerUtil.gameManager.createPlayer(players[i]);
					}
					DefaultManagerUtil.gameManager.refreshMainBoardSize();
					break;
				//case CmdConst.GAME_CODE_JOIN: //玩家加入游戏
					/*player = this.createNewPlayer();
					player.id = param.userId;
					player.name = param.name;
					player.position = param.sitPosition;
					player.playerState = param.playerState;
					player.playingState = param.playingState;
					RoomUtil.gameManager.addPlayer(player, param.localPlayer);*/
					//RoomUtil.soundManager.play("joinGame");
					//break;
				//case CmdConst.GAME_CODE_LEAVE: //离开游戏
				//case CmdConst.GAME_CODE_REMOVE_PLAYER: //移出游戏
					//RoomUtil.gameManager.removePlayer(param.userId);
					//RoomUtil.soundManager.play("leaveGame");
					//break;
				case CmdConst.GAME_CODE_START: //开始游戏
					//开始时隐藏设置窗口
					RoomUtil.stateManager.hideGameBoard();
					RoomUtil.soundManager.play("start");
					RoomUtil.gameModule.clear();
					DefaultManagerUtil.gameManager.resetPlayers();
					break;
				case CmdConst.GAME_CODE_END: //结束游戏
					//清除所有游戏窗口...
					RoomUtil.gameModule.disableAllInput();
					DefaultManagerUtil.stateManager.hideGameBoard();
					DefaultManagerUtil.stateManager.clear();
					RoomUtil.stateManager.showConfigWindow();
					//停止计时
					RoomUtil.stateManager.timeBoard.stop();
					break;
				/*case CmdConst.GAME_CODE_LOAD_CONFIG: //读取游戏设置
					RoomUtil.stateManager.showConfigWindow();
					RoomUtil.stateManager.configWindow.loadConfig(param);
					break;*/
				case CmdConst.GAME_CODE_GAME_TIME: //设置游戏时间
					this.setGameTime(param);
					break;
				case CmdConst.GAME_CODE_LOAD_REPORT: //读取战报
					TooltipManager.hideLoadingTip();
					RoomUtil.stateManager.scoreWindow.setReport(param);
					break;
				case CmdConst.GAME_CODE_VP_BOARD: //计分表
					RoomUtil.stateManager.showScoreWindow(param);
					break;
				case CmdConst.GAME_CODE_REPORT_MESSAGE: //战报信息
					this.onReportMessage(param);
					break;
				default:
					//其余指令转发到游戏模块中执行
					RoomUtil.gameModule.gameCommandHandler.processCommand(param);
					//this.processGameCmd(param);
					break;
			}
		}
		
		/**
		 * 设置游戏时间
		 */
		protected function setGameTime(param:Object):void{
			RoomUtil.stateManager.timeBoard.start();
			RoomUtil.stateManager.timeBoard.setTime(param.hour, param.minute);
		}
		
		/**
		 * 处理聊天指令
		 */
		override protected function processChatCommand(param:Object):void{
			var message:Object = param.message;
			if(message!=null){
				RoomUtil.stateManager.chatWindow.receiveMessage(message);
			}
		}
		
		/**
		 * 得到战报信息时调用的方法
		 */
		protected function onReportMessage(param:Object):void{
			RoomUtil.stateManager.reportWindow.addMessages(param.messages);
			DefaultManagerUtil.alertManager.alertMessages(param.messages);
		}
		
	}
}