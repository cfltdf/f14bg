package com.f14.F14bgGame.bg.manager
{
	import com.f14.F14bgClient.room.RoomUtil;
	import com.f14.F14bgGame.bg.consts.PlayingState;
	import com.f14.F14bgGame.bg.player.Player;
	
	/**
	 * 游戏信息相关管理器
	 */
	public class GameManager
	{
		public function GameManager()
		{
		}
		
		public var localPlayer:Player;
		public var players:Array = new Array();
		/**
		 * ID为索引的玩家数组
		 */
		public var idPlayers:Array = new Array();
		
		/**
		 * 按照用户id取得玩家对象
		 */
		public function getPlayerById(userId:String):Player{
			return idPlayers[userId];
		}
		
		/**
		 * 初始化游戏
		 */
		public function initGame():void{
			//清理所有玩家
			for each(var player:Player in this.idPlayers){
				if(player!=null){
					player.clear();
				}
			}
		}
		
		/**
		 * 游戏开始时的设置
		 */
		public function setupGame(param:Object):void{
			
		}
		
		/**
		 * 装载游戏进行中的状态
		 */
		public function loadPlayingInfo(param:Object):void{
			
		}
		
		/**
		 * 添加玩家
		 * 
		 * @param player
		 * @param local 是否当前玩家,默认为false
		 */
		public function addPlayer(player:Player, local:Boolean = false):void{
			var p:Player = this.getPlayerById(player.id);
			if(p==null){
				p = player;
				this.idPlayers[p.id] = p;
			}
			p.position = player.position;
			//加入游戏,并创建游戏面板
			if(local && this.localPlayer==null){
				localPlayer = p;
			}
			players[p.position] = p;
			RoomUtil.gameModule.createPlayerBoard(p);
		}
		
		/**
		 * 移除玩家
		 */
		public function removePlayer(userId:String):void{
			var player:Player = this.getPlayerById(userId);
			if(player!=null){
				players[player.position] = null;
				idPlayers[player.id] = null;
				//ApplicationManager.stateManager.configWindow.removePlayer(player.id);
				if(player.playingState==PlayingState.PLAYING){
					//如果玩家在游戏中,则需要移除游戏面板中的信息
				}
			}
		}
		
		/**
		 * 重置玩家信息
		 */
		public function resetPlayers():void{
			localPlayer = null;
			players = new Array();
			idPlayers = new Array();
		}
		
		/**
		 * 判断该位置是否是本地玩家的位置
		 */
		public function isLocalPosition(position:int):Boolean{
			return this.localPlayer==this.players[position];
		}
		
		/**
		 * 判断该参数是否是本地玩家的
		 */
		public function isLocalParam(param:Object):Boolean{
			if(param==null){
				return false;
			}
			return isLocalPosition(param.position);
		}
		
		/**
		 * 判断该玩家是否是本地玩家
		 */
		public function isLocalPlayer(player:Player):Boolean{
			return isLocalPosition(player.position);
		}
		
		/**
		 * 创建并添加玩家
		 */
		public function createPlayer(object:Object):Player{
			var player:Player = null;
			//检查该玩家是否是本地玩家
			if(object.localPlayer){
				player = this.localPlayer;
			}else{
				player = RoomUtil.gameModule.createPlayer();
				player.id = object.userId;
				player.name = object.name;
				player.position = object.position;
			}
			this.addPlayer(player, object.localPlayer);
			return player;
		}
		
		/**
		 * 创建本地玩家
		 */
		public function createLocalPlayer(object:Object):Player{
			var player:Player = RoomUtil.gameModule.createPlayer();
			player.id = object.userId;
			player.name = object.name;
			player.position = object.position;
			this.localPlayer = player;
			return player;
		}
		
		/**
		 * 判断当前用户是否在游戏中
		 */
		public function isPlayingGame():Boolean{
			return localPlayer==null?false:true;
		}
		
		/**
		 * 刷新主游戏界面的大小
		 */
		public function refreshMainBoardSize():void{
			RoomUtil.gameModule.refreshMainBoardSize();
		}
		
		/**
		 * 取得玩家数量
		 */
		public function getPlayerNumber():int{
			return this.players.length;
		}
		
	}
}